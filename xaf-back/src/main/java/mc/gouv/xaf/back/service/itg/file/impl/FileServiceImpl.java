package mc.gouv.xaf.back.service.itg.file.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.exception.VScanException;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * 
 * Service d'appel à FILE pour les démarches
 * 
 * @author qdeme
 *
 */
@Component
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PropertiesService propertiesService;

    @Override
    public void getFile(String filename, String containerId, HttpServletResponse response) throws ClientProtocolException, IOException {

        LOGGER.info("FileService.getFile(" + filename + ")");

        String accountId = gouvPropertiesResolver.getDemarcheId();
        
        // Remplacement des espaces par des "+"...
        filename = filename.replace(" ", "+");
        LOGGER.info("FileClient.getFile(" + accountId + "," + containerId + "," + filename + ")");
        afBackUtils.getFileClient().getFile(accountId, containerId, filename, response);

    }

    @Override
    public String saveFile(DemandeDTO demande, String filename, String containerId, String contentType, InputStream inputStream,
            OutputStream outputStream) throws Exception {

        LOGGER.info("FileService.saveFile(" + demande.getPkDemandes() + "," + filename + "," + contentType + ")");

        // Définition de la meta pour le demande ID
        Map<String, String> customHeaders = createCustomHeaders(demande);

        filename = demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/" + filename;

        LOGGER.info("Filename à donner à FILE : " + filename);

        String accountId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("FileClient.saveFile(" + accountId + "," + containerId + "," + filename + ")");
        return afBackUtils.getFileClient().saveFile(accountId, containerId, inputStream, filename, contentType, customHeaders,
                outputStream);

    }

    @Override
    public String saveFile(DemandeDTO demande, String containerId, MultipartFile file, HttpServletResponse response)
            throws Exception {

        LOGGER.info("FileService.saveFile(" + demande.getPkDemandes() + "," + file.getOriginalFilename() + ")");

        // Vérification de l'extension du fichier
        if (file.getOriginalFilename() != null && !estExtensionDansWhitelist(file.getOriginalFilename())) {
            LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({}), pas d'upload dans FILE", getExtensionsWhitelist());
            throw new FileUploadException("Erreur: le type du fichier soumis n'est pas valide");
        }

        // Appel à VSCAN pour vérifier la virulance du fichier
        ScanDTO scanDTO = verificationVSCAN(file);
        if (!scanDTO.isResult()) {
            LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
            throw new VScanException("Erreur: le fichier soumis semble corrompu");
        }

        LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");

        String filename = "/" + demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/"
                + URLEncoder.encode(file.getOriginalFilename(), "UTF-8");

        LOGGER.info("Filename à donner à FILE : " + filename);

        Map<String, String> customHeaders = createCustomHeaders(demande);

        String accountId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("FileClient.saveFile(" + accountId + "," + containerId + "," + filename + ")");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        return afBackUtils.getFileClient().saveFile(accountId, containerId, file.getInputStream(), filename, file.getContentType(), customHeaders, outputStream);

    }

    private boolean estExtensionDansWhitelist (String filename) {
        String[] filenameSplit = filename.split("\\.");
        String fileExtension = filenameSplit[filenameSplit.length-1];
        return getExtensionsWhitelist().contains(fileExtension);
    }


    private List<String> getExtensionsWhitelist() {
        PropertiesDTO extensionsProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), EXTENSIONS_WHITELIST);
        List<String> extensions = new ArrayList<>();

        if(extensionsProperty != null) {
            String propertyString = extensionsProperty.getValue().replace("*.","").replace(" ","");
            String[] types = propertyString.split(",");
            Collections.addAll(extensions, types);
        }

        return extensions;
    }

    private Map<String, String> createCustomHeaders(DemandeDTO demande) {
        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDEID, demande.getPkDemandes().toString());
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDESTATUT, demande.getDernierStatut().getLibelle());
        return customHeaders;
    }

    public ScanDTO verificationVSCAN(MultipartFile file) throws IOException {
        LOGGER.info("Appel à VSCAN...");

        ObjectMapper mapper = new ObjectMapper();
        String urlVscan = gouvPropertiesResolver.getVScanUrl();
        LOGGER.info("URL = " + urlVscan);
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
        builderVscan.addPart("scanRequest", new StringBody(scanRequestStr));
        HttpEntity multipartVscan = builderVscan.build();
        HttpPost postRequestVscan = new HttpPost(urlVscan.toString());
        postRequestVscan.setEntity(multipartVscan);
        postRequestVscan.addHeader("Authorization", "Bearer " + gouvPropertiesResolver.getVscanJwt());
        HttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
        String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent());
        LOGGER.info("VSCAN Response : " + postResponseVscan.getStatusLine() + "(" + vscanResp + ")");

        return mapper.readValue(vscanResp, ScanDTO.class);
    }

}
