package mc.gouv.xaf.backweb.ws;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

/**
 * 
 * Proxy permettant d'accéder au service FILE depuis la démarche
 * 
 * @author qdeme
 *
 */
@GouvRestController
@RequestMapping("/ws/file")
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private FileService fileService;
    
    @Autowired
    private DemandesService demandesService;

    @RequestMapping(value = "/get/**", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK) // 200
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOGGER.info("====================== getFile()");

        String file = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        file = file.replace("/ws/file/get/", "");

        fileService.getFile(file, gouvPropertiesResolver.getContainerId(), response);

        LOGGER.info("====================== getFile() terminé, retour au client...");
    }

    /**
     * Appelle FILE afin de sauvegarder différents fichiers contenus dans la request MultiPart
     * Retourne une Map correspondant aux fichiers (fileName, fileUrl)
     * @param usagerId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public Map<String, String> saveFiles(Integer demandeId, MultipartFile[] files, HttpServletResponse response,
            Integer pkDemande) throws Exception {

        LOGGER.info("====================== saveFiles()");
        LOGGER.info("Appel de DEM afin de récupérer la demande pour le calcul...");

        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

        Map<String, String> fileNames = new HashMap<String, String>();
        
        for (MultipartFile file : files) {
            if (!StringUtils.isBlank(file.getOriginalFilename())) {
                LOGGER.info("Part à traiter : " + file.getOriginalFilename());

                LOGGER.info("Appel au FileService...");
                String filename = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(), file, response);

                fileNames.put(file.getOriginalFilename(), filename);
            }
        }

        LOGGER.info("====================== saveFiles() terminé, retour au client...");

        return fileNames;
    }

}
