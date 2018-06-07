package mc.gouv.af.back.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * Proxy permettant d'accéder au service FILE depuis la démarche
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/file")
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
        file = file.replace("/file/get/", "");

        fileService.getFile(file, response);

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
    public Map<String, String> saveFiles(Integer demandeId, HttpServletRequest request, HttpServletResponse response,
            Integer pkDemande) throws Exception {

        LOGGER.info("====================== saveFiles()");
        LOGGER.info("Appel de DEM afin de récupérer la demande pour le calcul...");

        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

        Map<String, String> fileNames = new HashMap<String, String>();
        
        Iterator<Part> it = request.getParts().iterator();
        while (it.hasNext()) {
            Part part = it.next();

            if (!StringUtils.isBlank(part.getSubmittedFileName())) {
                LOGGER.info("Part à traiter : " + part.getSubmittedFileName());

                LOGGER.info("Appel au FileService...");
                String filename = fileService.saveFile(demande, part, response);

                fileNames.put(part.getSubmittedFileName(), filename);
            }
        }

        LOGGER.info("====================== saveFiles() terminé, retour au client...");

        return fileNames;
    }

}
