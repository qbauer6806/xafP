package mc.gouv.xaf.back.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.util.AfBackUtils;
import mc.gouv.dem.shared.model.DemandeDTO;

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

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void getFile(String filename, HttpServletResponse response) throws ClientProtocolException, IOException {

        LOGGER.info("FileService.getFile(" + filename + ")");

        String accountId = gouvPropertiesResolver.getDemarcheId();
        String containerId = gouvPropertiesResolver.getContainerId();
        
        // Remplacement des espaces par des "+"...
        filename = filename.replace(" ", "+");
        LOGGER.info("FileClient.getFile(" + accountId + "," + containerId + "," + filename + ")");
        afBackUtils.getFileClient().getFile(accountId, containerId, filename, response);

    }

    @Override
    public String saveFile(DemandeDTO demande, String filename, String contentType, InputStream inputStream,
            OutputStream outputStream) throws Exception {

        LOGGER.info("FileService.saveFile(" + demande.getPkDemandes() + "," + filename + "," + contentType + ")");

        // Définition de la meta pour le demande ID
        Map<String, String> customHeaders = createCustomHeaders(demande);

        filename = demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/" + filename;

        LOGGER.info("Filename à donner à FILE : " + filename);

        String accountId = gouvPropertiesResolver.getDemarcheId();
        String containerId = gouvPropertiesResolver.getContainerId();
        LOGGER.info("FileClient.saveFile(" + accountId + "," + containerId + "," + filename + ")");
        return afBackUtils.getFileClient().saveFile(accountId, containerId, inputStream, filename, contentType, customHeaders,
                outputStream);

    }

    @Override
    public String saveFile(DemandeDTO demande, MultipartFile file, HttpServletResponse response)
            throws Exception {

        LOGGER.info("FileService.saveFile(" + demande.getPkDemandes() + "," + file.getOriginalFilename() + ")");

        String filename = "/" + demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/"
                + URLEncoder.encode(file.getOriginalFilename(), "UTF-8");

        LOGGER.info("Filename à donner à FILE : " + filename);

        Map<String, String> customHeaders = createCustomHeaders(demande);

        String accountId = gouvPropertiesResolver.getDemarcheId();
        String containerId = gouvPropertiesResolver.getContainerId();
        LOGGER.info("FileClient.saveFile(" + accountId + "," + containerId + "," + filename + ")");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        return afBackUtils.getFileClient().saveFile(accountId, containerId, file.getInputStream(), filename, file.getContentType(), customHeaders, outputStream);

    }

    private Map<String, String> createCustomHeaders(DemandeDTO demande) {
        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDEID, demande.getPkDemandes().toString());
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDESTATUT, demande.getDernierStatut().getLibelle());
        return customHeaders;

    }

}
