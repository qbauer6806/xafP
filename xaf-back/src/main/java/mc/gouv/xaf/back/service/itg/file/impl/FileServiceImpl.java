package mc.gouv.xaf.back.service.itg.file.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.exception.VScanException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'appel à FILE pour les démarches
 *
 * @author qdeme
 */
@Component
public class FileServiceImpl implements FileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

	private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";
	private static final String VSCAN_ACTIVATION = "VSCAN_ACTIVATION";
	private static final String MC_METADATA_PREFIX = "X-MC-";
	private static final String AUTHORIZATION_PREFIX = "Bearer ";

	private RestTemplate restTemplate;

	@Autowired
	private AfBackUtils afBackUtils;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private PropertiesService propertiesService;

	@Override
	public void getFile(String filename, String containerId, HttpServletResponse response) throws IOException {
		LOGGER.info("FileService.getFile({})", filename);
		String accountId = gouvPropertiesResolver.getDemarcheId();
		// Remplacement des espaces par des "+"...
		filename = filename.replace(" ", "+");
		LOGGER.info("FileClient.getFile({}, {}, {})", accountId, containerId, filename);
		afBackUtils.getFileClient().getFile(accountId, containerId, filename, response);
	}

	@Override
	public InputStream getFile(String filename, String containerId) throws IOException {
		LOGGER.info("FileService.getFile({})", filename);
		String accountId = gouvPropertiesResolver.getDemarcheId();
		// Remplacement des espaces par des "+"...
		filename = filename.replace(" ", "+");
		InputStream is = afBackUtils.getFileClient().getFile(accountId, containerId, filename);
		LOGGER.info("FileClient.getFile({}, {}, {})", accountId, containerId, filename);
		return is;
	}

	@Override
	public InputStream getFile(String url) throws IOException {
		LOGGER.info("FileService.getFile({})", url);
		InputStream is = afBackUtils.getFileClient().getFile(url);
		LOGGER.info("FileClient.getFile({})", url);
		return is;
	}

	@Override
	public String saveFile(DemandeDTO demande, String filename, String containerId, String contentType, InputStream inputStream,
						   OutputStream outputStream) throws Exception {

		LOGGER.info("FileService.saveFile({}, {}, {})", demande.getPkDemandes(), filename, contentType);

		// Définition de la meta pour le demande ID
		// On part du principe que le fichier a été généré côté back et n'est pas malicieux
		Map<String, String> customHeaders = createCustomHeaders(demande, true);

		filename = demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/" + filename;

		LOGGER.info("Filename à donner à FILE : {}", filename);

		String accountId = gouvPropertiesResolver.getDemarcheId();
		LOGGER.info("FileClient.saveFile({}, {}, {})", accountId, containerId, filename);
		return afBackUtils.getFileClient().saveFile(accountId, containerId, inputStream, filename, contentType, customHeaders,
				outputStream);
	}

	@Override
	public String saveFile(DemandeDTO demande, String containerId, MultipartFile file, HttpServletResponse response)
			throws Exception {

		LOGGER.info("FileService.saveFile({}, {})", demande.getPkDemandes(), file.getOriginalFilename());

		// Vérification de l'extension du fichier
		if (file.getOriginalFilename() != null && !estExtensionDansWhitelist(file.getOriginalFilename())) {
			LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({}), pas d'upload dans FILE", getExtensionsWhitelist());
			throw new FileUploadException("Erreur: le type du fichier soumis n'est pas valide");
		}

		// Appel à VSCAN pour vérifier la virulance du fichier
		PropertiesDTO vscanActivationProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), VSCAN_ACTIVATION);
		boolean vscanActivation = Boolean.parseBoolean(vscanActivationProp.getValue());

		LOGGER.info("Activation de VSCAN: {}", vscanActivation);
		if (vscanActivation) {
			ScanDTO scanDTO = verificationVSCAN(file);
			if (!scanDTO.isResult()) {
				LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
				throw new VScanException("Erreur: le fichier soumis semble corrompu");
			}
			LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
		}

		String filename = "/" + demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/"
				+ URLEncoder.encode(file.getOriginalFilename(), "UTF-8");

		LOGGER.info("Filename à donner à FILE : {}", filename);

		Map<String, String> customHeaders = createCustomHeaders(demande, vscanActivation);

		String accountId = gouvPropertiesResolver.getDemarcheId();
		LOGGER.info("FileClient.saveFile({}, {}, {})", accountId, containerId, filename);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		return afBackUtils.getFileClient().saveFile(accountId, containerId, file.getInputStream(), filename, file.getContentType(), customHeaders, outputStream);

	}

	private boolean estExtensionDansWhitelist(String filename) {
		String[] filenameSplit = filename.split("\\.");
		String fileExtension = filenameSplit[filenameSplit.length - 1].toLowerCase();
		return getExtensionsWhitelist().contains(fileExtension);
	}


	private List<String> getExtensionsWhitelist() {
		PropertiesDTO extensionsProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), EXTENSIONS_WHITELIST);
		List<String> extensions = new ArrayList<>();

		if (extensionsProperty != null) {
			String propertyString = extensionsProperty.getValue().replace("*.", "").replace(" ", "");
			String[] types = propertyString.split(",");
			Collections.addAll(extensions, types);
		}

		return extensions;
	}

	private Map<String, String> createCustomHeaders(DemandeDTO demande, boolean scanExecute) {
		Map<String, String> customHeaders = new HashMap<>();
		customHeaders.put(FILE_METADATA_DEMANDEID, demande.getPkDemandes().toString());
		customHeaders.put(FILE_METADATA_DEMANDESTATUT, demande.getDernierStatut().getLibelle());
		customHeaders.put(FILE_METADATA_SCANEXECUTE, scanExecute + "");
		return customHeaders;
	}

	public ScanDTO verificationVSCAN(MultipartFile file) throws IOException {
		LOGGER.info("Appel à VSCAN...");

		ObjectMapper mapper = new ObjectMapper();
		String urlVscan = gouvPropertiesResolver.getVScanUrl();
		LOGGER.info("URL = {}", urlVscan);
		HttpClient clientVscan = HttpClientBuilder.create().build();
		MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
		builderVscan.addPart("file", new InputStreamBody(file.getInputStream(), ContentType.create(file.getContentType()), file.getName()));

		// Pour tester avec un fichier vérolé (EICAR)
		//builderVscan.addPart("file", new InputStreamBody(new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()), "blason.jpg"));

		ScanRequestDTO scanRequest = new ScanRequestDTO();
		scanRequest.setCodeAppli(gouvPropertiesResolver.getDemarcheId());
		scanRequest.setFilename(file.getName());
		scanRequest.setEnduserAppModule(file.getName().toLowerCase() + "-frontserver");

		String scanRequestStr = mapper.writeValueAsString(scanRequest);
		builderVscan.addPart("scanRequest", new StringBody(scanRequestStr, ContentType.DEFAULT_TEXT));
		HttpEntity multipartVscan = builderVscan.build();
		HttpPost postRequestVscan = new HttpPost(urlVscan);
		postRequestVscan.setEntity(multipartVscan);
		postRequestVscan.addHeader(org.apache.http.HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + gouvPropertiesResolver.getVscanJwt());
		HttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
		String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent());
		LOGGER.info("VSCAN Response : {} ({})", postResponseVscan.getStatusLine(), vscanResp);

		return mapper.readValue(vscanResp, ScanDTO.class);
	}

	private void initRestTemplate() {
		if (restTemplate == null) {
			LOGGER.info("Initialisation du RestTemplate...");
			try {
//                restTemplate = new RestTemplate(new AuthHttpComponentsClientHttpRequestFactory(
//                        new HttpHost(new URL(DemarchesUtils.FILE_REST_URL).getHost(),
//                                new URL(DemarchesUtils.FILE_REST_URL).getPort(), "http"),
//                        DemarchesUtils.FILE_USER, DemarchesUtils.FILE_PWD));

				restTemplate = new RestTemplate();
				List<HttpMessageConverter<?>> list = new ArrayList<>();
				MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
				List<MediaType> mediaTypes = new ArrayList<>();
				mediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
				mediaTypes.add(new MediaType("text", "html", StandardCharsets.UTF_8));
				conv.setSupportedMediaTypes(mediaTypes);
				list.add(conv);
				restTemplate.setMessageConverters(list);
			} catch (Exception e) {
				LOGGER.error("FileServiceImpl() erreur : ", e);
			}
		}
	}

	private URL getFileURL(String fileurl, String demarcheId) throws MalformedURLException {
		// file = accessId/uuid/filename (/uuid/filename inclu dans fichier.getUrl())
		if (fileurl.charAt(0) != '/') {
			fileurl = "/" + fileurl;
		}

		// Remplacer les espaces par des "+"...
		String filename = new File(fileurl).getName();
		fileurl = fileurl.replace(filename, filename.replace(" ", "+"));

		// Rajouter l'AccessID dans l'URL des fichiers

		URL url = new URL(gouvPropertiesResolver.getFileUrl() + "/" + demarcheId + "/"
				+ gouvPropertiesResolver.getContainerId() + "/" + fileurl);
		LOGGER.info("URL du fichier calculée : {}", url);

		return url;
	}

	private Map<String, String> getFileMetadata(String fileUrl) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add(org.apache.http.HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + gouvPropertiesResolver.getFileJwt());
		org.springframework.http.HttpEntity<Object> requestEntity = new org.springframework.http.HttpEntity<>(null, headers);
		ResponseEntity<Object> response = restTemplate.exchange(fileUrl, HttpMethod.HEAD, requestEntity, Object.class);
		HttpStatus httpStatus = response.getStatusCode();
		if (httpStatus != HttpStatus.OK) {
			throw new DemarchesServiceException("La requête HEAD a retourné le httpStatus " + httpStatus,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response.getHeaders().toSingleValueMap().entrySet().stream()
				// On ne retourne que les métadata du fichier
				.filter(entry -> entry.getKey().startsWith(MC_METADATA_PREFIX))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private void updateFileMetadataGeneric(String fileUrl, String metadata, String value) throws IOException {
		// Appel du getFile pour avoir les anciennes metadonnées
		Map<String, String> oldMetadatas = getFileMetadata(fileUrl);

		// Création du header avec les anciennes metadonnées
		HttpHeaders headers = new HttpHeaders();
		if (!oldMetadatas.isEmpty()) {
			oldMetadatas.forEach(headers::add);
		}

		// On met (ou remplace) dans le header la métadonnée qui contient la nouvelle metadonnée
		if (headers.containsKey(metadata)) {
			headers.set(metadata, value);
		} else {
			headers.add(metadata, value);
		}

		// Hack nécessaire parce que la méthode PATCH n'est pas forcément prise en compte par les couches sous
		// Spring (JDK 1.7)
		// Du coup on envoie en POST et FILE intercepte ce header dans un ServletFilter afin de placer le PATCH
		// qu'il faut
		headers.add(DemarchesUtils.METADATA_HTTPMETHODOVERRIDE, "PATCH");

		// Ajout de l'authentification JWT
		headers.add(org.apache.http.HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + gouvPropertiesResolver.getFileJwt());

		// Pas de corps, mais des headers en guise de métadonnées
		org.springframework.http.HttpEntity<Object> requestEntity = new org.springframework.http.HttpEntity<>(null, headers);

		LOGGER.info("Appel à {}", fileUrl);

		ResponseEntity<Object> response = restTemplate.exchange(fileUrl, HttpMethod.POST, requestEntity, Object.class);
		HttpStatus httpStatus = response.getStatusCode();

		if (httpStatus != HttpStatus.OK) {
			throw new DemarchesServiceException("La requête PATCH a retourné le httpStatus " + httpStatus,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, String demarcheId, Integer demandeId)
			throws Exception {
		LOGGER.info("Début updateFilesMetadataWithDemandeId()");
		initRestTemplate();
		for (DemandeFileDTO fichier : fichiers) {
			URL url = getFileURL(fichier.getUrl(), demarcheId);
			updateFileMetadataGeneric(url.toString(), FILE_METADATA_DEMANDEID, demandeId.toString());
		}
		LOGGER.info("Fin updateFilesMetadataWithDemandeId()");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateFileMetadata(String fichierURL, String demarcheId, String metaKey, String metaValue) throws Exception {
		initRestTemplate();
		URL url = getFileURL(fichierURL, demarcheId);
		updateFileMetadataGeneric(url.toString(), metaKey, metaValue);
	}

}
