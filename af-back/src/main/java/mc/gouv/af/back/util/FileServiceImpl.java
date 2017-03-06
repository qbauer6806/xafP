package mc.gouv.af.back.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apishared.model.DemandeDTO;
import mc.gouv.file.apiclient.FileClient;

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

    private FileClient fileClient = null;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void getFile(String filename, HttpServletResponse response) throws ClientProtocolException, IOException {

        LOGGER.info("FileService.getFile(" + filename + ")");

        String appfactoryId = AfBackUtils.getAppFactoryId();
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("FileClient.getFile(" + appfactoryId + "," + demarcheId + "," + filename + ")");
        getFileClient().getFile(appfactoryId, demarcheId, filename, response);

    }

    @Override
    public String saveFile(DemandeDTO demande, String filename, String contentType, InputStream inputStream,
            OutputStream outputStream) throws Exception {

        LOGGER.info("FileService.saveFile(" + demande.getPkDemandes() + "," + filename + "," + contentType + ")");

        // Définition de la meta pour le demande ID
        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDEID, demande.getPkDemandes().toString());
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDESTATUT, demande.getDernierStatut().getLibelle().name());

        filename = demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/" + filename;

        LOGGER.info("Filename à donner à FILE : " + filename);

        String appfactoryId = AfBackUtils.getAppFactoryId();
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("FileClient.saveFile(" + appfactoryId + "," + demarcheId + "," + filename + ")");
        return getFileClient().saveFile(appfactoryId, demarcheId, inputStream, filename, contentType, customHeaders,
                outputStream);

    }

    @Override
    public String saveFile(DemandeDTO demande, Part part, HttpServletResponse response)
            throws IOException, ServletException {

        LOGGER.info("FileService.saveFile(" + demande.getPkDemandes() + "," + part.getSubmittedFileName() + ")");

        String filename = demande.getFkAccess() + "/" + AfBackUtils.generateUUID() + "/"
                + URLEncoder.encode(part.getSubmittedFileName(), "UTF-8");

        LOGGER.info("Filename à donner à FILE : " + filename);

        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDEID, demande.getPkDemandes().toString());
        customHeaders.put(AfBackUtils.FILE_METADATA_DEMANDESTATUT, demande.getDernierStatut().getLibelle().name());

        String appfactoryId = AfBackUtils.getAppFactoryId();
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("FileClient.saveFile(" + appfactoryId + "," + demarcheId + "," + filename + ")");
        return getFileClient().saveFile(appfactoryId, demarcheId, part, filename, customHeaders, response);

    }

    private FileClient getFileClient() {
        if (fileClient == null) {
            fileClient = new FileClient(gouvPropertiesResolver.getFileUrl(), gouvPropertiesResolver.getFileUser(),
                    gouvPropertiesResolver.getFilePwd());
        }
        return fileClient;
    }

}
