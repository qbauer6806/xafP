package mc.gouv.xaf.back.service.itg.file.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.back.exception.FileUploadException;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.http.client.ClientProtocolException;
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

}
