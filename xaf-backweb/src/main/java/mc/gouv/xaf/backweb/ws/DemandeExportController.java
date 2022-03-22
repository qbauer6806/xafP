package mc.gouv.xaf.backweb.ws;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.excel.ExcelExportModelProvider;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

/**
 * Controller pour l'extraction des données des demandes (export excel)
 * 
 * @author qdeme
 * 
 */
@Controller
@Secured("ROLE_PARAMETRAGE")
@RequestMapping("/ws/export")
public class DemandeExportController extends AbstractController {

    @Autowired
    private ExcelExportService excelExportService;
    
    @Autowired
    private ExcelExportModelProvider excelExportModelProvider;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemarchesService demarchesService;
    
    @Autowired
    private AfBackUtils afBackUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeExportController.class);

    @RequestMapping(method = RequestMethod.GET, value = "/excel")
    public void exportExcel(HttpServletResponse response, @RequestParam(required = false) String creationStartDate , @RequestParam(required = false) String creationEndDate) {

        LOGGER.info("======================= Appel du controller /ws/export/excel");

        try {
            String demarcheId = gouvPropertiesResolver.getDemarcheId();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition", "attachment; filename=" +
                    demarchesService.getDemarche(demarcheId).getIdentifiantPrefixe() + "_Donnees_Stat_" + afBackUtils.generateFileDateAndTimeSuffix() + ".xlsx");

            // Création du cookie pour notifier du téléchargement terminé (2 minutes max age)
            Cookie telechargementCookie = new Cookie("exportEnCours", "0");
            telechargementCookie.setMaxAge(60 * 2);
            telechargementCookie.setSecure(false);
            telechargementCookie.setPath("/");
            response.addCookie(telechargementCookie);

            ExcelRechercheDTO excelRechercheDTO = new ExcelRechercheDTO();
            excelRechercheDTO.setCreationStartDate(creationStartDate);
            excelRechercheDTO.setCreationEndDate(creationEndDate);
            
            LOGGER.info("Constitution du modèle pour la génération Excel...");
            Map<String, Object> model = excelExportModelProvider.getModel(excelRechercheDTO);
            
            LOGGER.info("Appel export Excel...");
            excelExportService.exportExcel("demandes.xlsx", model, response.getOutputStream());

        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans exportExcel", e);
        }

        LOGGER.info("======================= Fin /ws/export/excel");
    }

}
