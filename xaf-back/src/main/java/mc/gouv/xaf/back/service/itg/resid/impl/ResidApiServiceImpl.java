package mc.gouv.xaf.back.service.itg.resid.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.resid.ResidApiService;
import mc.gouv.xaf.back.service.itg.resid.ResidErrorResponseErrorHandler;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.itg.resid.dto.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ResidApiServiceImpl implements ResidApiService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResidApiServiceImpl.class);

	// Entrypoints
	public static final String RESID_NOUVELLE_CARTE_PATH = "/demandes/nouvelleCarte";
	public static final String RESID_RENOUVELLEMENT_CARTE_PATH = "/demandes/renouvellementCarte";
	public static final String RESID_DUPLICATA_CARTE_PATH = "/demandes/duplicataCarte";
	public static final String RESID_CHANGEMENT_SITUATION_PATH = "/demandes/changementSituation";
	public static final String RESID_CERTIFICAT_RESIDENCE_PATH = "/demandes/certificatResidence";
	public static final String RESID_ETATS_DEMANDES_BY_ID_PATH = "/demandes/etatsDemandesById";
    public static final String RESID_ETATS_DEMANDES_PATH = "/demandes/etatsDemandesUpdatedAfter";

    public static final String LAST_SUCCESSFUL_SYNCHRO_KEY = "LAST_SUCCESSFUL_SYNCHRO";

    @Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
	private PropertiesService propertiesService;

	@Autowired
	private FileService fileService;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;


	@Override
	public ResidHttpResponseDTO submitNouvelleCarteResid(ResidDemandeNouvelleCarteCompleteDTO nouvelleCarte, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception {

		LOGGER.info("Appel à l'API RESID pour la création d'une carte");

		ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(nouvelleCarte,
				new ParameterizedTypeReference<ResidHttpResponseDTO>(){}, files, url, RESID_NOUVELLE_CARTE_PATH, jwt);

		if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
			ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
			residHttpResponseDTO.setHttpStatus(201);
			return residHttpResponseDTO;
		}

		return responseEntity.getBody();
	}

	@Override
	public ResidHttpResponseDTO submitRenouvellementCarteResid(ResidDemandeRenouvellementCarteCompleteDTO renouvellement, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception {

		LOGGER.info("Appel à l'API RESID pour le renouvellement d'une carte");

        ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(renouvellement,
                new ParameterizedTypeReference<ResidHttpResponseDTO>(){}, files, url, RESID_RENOUVELLEMENT_CARTE_PATH, jwt);

		if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
			ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
			residHttpResponseDTO.setHttpStatus(201);
			return residHttpResponseDTO;
		}

		return responseEntity.getBody();
	}

	@Override
	public ResidHttpResponseDTO submitDuplicataCarteResid(ResidDemandeDuplicataCarteCompleteDTO duplicataCarte, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception {

		LOGGER.info("Appel à l'API RESID pour le duplicata d'une carte");

		ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(duplicataCarte,
                new ParameterizedTypeReference<ResidHttpResponseDTO>(){}, files, url, RESID_DUPLICATA_CARTE_PATH, jwt);

		if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
			ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
			residHttpResponseDTO.setHttpStatus(201);
			return residHttpResponseDTO;
		}

		return responseEntity.getBody();
	}

	@Override
	public ResidHttpResponseDTO submitChangementSituationResid(ResidDemandeChangementSituationCompleteDTO changementsituation, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception {

		LOGGER.info("Appel à l'API RESID pour le changement de situation");

		ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(changementsituation,
                new ParameterizedTypeReference<ResidHttpResponseDTO>(){}, files, url, RESID_CHANGEMENT_SITUATION_PATH, jwt);

		if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
			ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
			residHttpResponseDTO.setHttpStatus(201);
			return residHttpResponseDTO;
		}

		return responseEntity.getBody();
	}

	@Override
	public ResidHttpResponseDTO submitCertificatResid(ResidDemandeCertificatResidenceCompleteDTO certificatResidence, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception {

		LOGGER.info("Appel à l'API RESID pour le certificat de residence");

		ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(certificatResidence,
                new ParameterizedTypeReference<ResidHttpResponseDTO>(){}, files, url, RESID_CERTIFICAT_RESIDENCE_PATH, jwt);

		if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
			ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
			residHttpResponseDTO.setHttpStatus(201);
			return residHttpResponseDTO;
		}

		return responseEntity.getBody();
	}

	public <T, Y> ResponseEntity<Y> submitDemandeResident(T residObject, ParameterizedTypeReference<Y> type, Map<Integer, DemandeFileDTO> files, String residUrl, final String entryPoint, String jwt) throws Exception {

		MultiValueMap<String, Object> parts = createMultiparts(residObject, files);
		HttpHeaders headers = getResidMultipartRequestHeaders(jwt);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

		RestTemplate rest = restTemplateBuilder.errorHandler(new ResidErrorResponseErrorHandler()).build();
		rest.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		String requestUrl = residUrl + entryPoint;
		URI uri = UriComponentsBuilder.fromHttpUrl(requestUrl).build().encode().toUri();

		ObjectMapper mapper = new ObjectMapper();
		LOGGER.debug("-- Appel RESID submit nouvelle carte");
		LOGGER.debug("URL: {} {}", HttpMethod.POST, uri.toURL());
		LOGGER.debug("Headers: {}", headers);
		LOGGER.debug("Body: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(residObject));

		ResponseEntity<Y> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, type);

        // RESID Appel de l'API
        LOGGER.info("Fin appel RESID");

        return responseEntity;
    }

	private <T> MultiValueMap<String, Object> createMultiparts(T residObject, Map<Integer, DemandeFileDTO> files) throws Exception {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

		LOGGER.info("Création de la requête multipart");

		HttpHeaders requestHeadersJSON = new HttpHeaders();
		requestHeadersJSON.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<T> residObjectJSONEntity = new HttpEntity<>(residObject, requestHeadersJSON);
		parts.add("demande", residObjectJSONEntity);

		LOGGER.info("Ajout des fichiers");

		for(Map.Entry<Integer, DemandeFileDTO> entry : files.entrySet()) {
			InputStream isf = fileService.getFile(entry.getValue().getUrl(), gouvPropertiesResolver.getContainerId());

			HttpHeaders requestHeadersAttachment = new HttpHeaders();
			ByteArrayResource fileAsResource = new ByteArrayResource(IOUtils.toByteArray(isf)){
				@Override
				public String getFilename(){
				    // Format index-filename pour éviter les doublons de noms
                    // ex. 1-Toto.txt
					return FileUtils.formatFilenameResid(entry.getValue().getName(), entry.getKey());
				}
			};
			HttpEntity<ByteArrayResource> attachmentPart = new HttpEntity<>(fileAsResource, requestHeadersAttachment);
			parts.add("files", attachmentPart);
		}

        LOGGER.debug("Multiparts\n{}", parts);

		return parts;
	}

	private HttpHeaders getResidMultipartRequestHeaders(String jwt) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "multipart/form-data");
		headers.add("Accept", "*/*");
		headers.add("Authorization", "Bearer " + jwt);
		return headers;
	}

	protected HttpHeaders getResidRequestHeaders(String jwt) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		headers.add("Accept", "*/*");
		headers.add("Authorization", "Bearer " + jwt);
		return headers;
	}

	@Override
	public ResidStatutDemandeDTO getEtatDemande(ResidIdTSDTO idDemande, String url, String jwt) throws Exception {
		LOGGER.info("Récupération du statut RESID de {}", idDemande);

		ResidStatutDemandeDTO statut = null;
		List<ResidStatutDemandeDTO> retList = getEtatMultipleDemandes(Collections.singletonList(idDemande), url, jwt);

		// On récupère uniquement le premier élément (on s'attend à ce qu'il y en ai maximum un)
		if (retList != null && retList.size() > 0) {
			statut = retList.get(0);
		}
		return statut;
	}

	@Override
	public List<ResidStatutDemandeDTO> getEtatMultipleDemandes(List<ResidIdTSDTO> idsDemandes, String url, String jwt) throws Exception {
		LOGGER.info("Récupération des statuts RESID de {}", idsDemandes);

		// Construction du rest template
		RestTemplate rest = restTemplateBuilder.errorHandler(new ResidErrorResponseErrorHandler()).build();
		rest.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		// Headers et URL
		HttpHeaders headers = getResidRequestHeaders(jwt);
		String requestUrl = url + RESID_ETATS_DEMANDES_BY_ID_PATH;
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);

		// Construction de la requête
		ObjectMapper mapper = new ObjectMapper();
		HttpEntity<String> requestEntity = new HttpEntity<>(mapper.writeValueAsString(idsDemandes), headers);
		URI uri = builder.build().encode().toUri();

		// Logs DEBUG
		LOGGER.debug("-- Appel RESID Get état d'une demande");
		LOGGER.debug("URL: {} {}", HttpMethod.POST, uri.toString());
		LOGGER.debug("Headers: {}", headers);
		LOGGER.debug("Body: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(idsDemandes));

		// Appel et réponse
		ResponseEntity<List<ResidStatutDemandeDTO>> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity,
				new ParameterizedTypeReference<List<ResidStatutDemandeDTO>>(){});

		LOGGER.debug("Réponse de l'API {}", responseEntity.getBody());

		LOGGER.info("Fin de l'appel vers RESID pour la récupération du statut RESID de {}", idsDemandes);

		return responseEntity.getBody();
	}

    @Override
    public ResidEtatsDemandesUpdatedAfterDTO getEtatsDemandesUpdated(String updatedAfter, String url, String jwt) {

        // Construction du rest template
        RestTemplate rest = restTemplateBuilder.errorHandler(new ResidErrorResponseErrorHandler()).build();
        rest.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // Headers et URL
        HttpHeaders headers = getResidRequestHeaders(jwt);
        String requestUrl = url + RESID_ETATS_DEMANDES_PATH;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        try {
            builder.queryParam("updatedAfter", URLEncoder.encode(updatedAfter, "UTF-8"));
        } catch (Exception e) {
            LOGGER.error("Problème dans l'encodage de la date à envoyer à RESID");
        }

        // Construction de la requête
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        URI uri = builder.build(true).encode().toUri();

        // Logs DEBUG
        LOGGER.debug("-- Appel RESID Get all demandes updated after");
        LOGGER.debug("URL: {} {}", HttpMethod.GET, uri.toString());
        LOGGER.debug("Headers: {}", headers);

        // Appel et réponse API
        ResponseEntity<ResidEtatsDemandesUpdatedAfterDTO> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity, ResidEtatsDemandesUpdatedAfterDTO.class);
        LOGGER.debug("Réponse de l'API {}", responseEntity.getBody());


        LOGGER.info("Fin appel à l'API RESID pour la récupération des demandes updatées");

        return responseEntity.getBody();
    }

	protected void setLastSuccessfulSynchroProperty(String lastSuccessfulSynchroTime) {
		PropertiesDTO lastSynchroProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), LAST_SUCCESSFUL_SYNCHRO_KEY);
		lastSynchroProperty.setValue(lastSuccessfulSynchroTime);
		propertiesService.saveOrUpdateProperties(lastSynchroProperty);
	}
}
