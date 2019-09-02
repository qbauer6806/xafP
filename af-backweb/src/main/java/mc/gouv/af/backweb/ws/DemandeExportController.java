package mc.gouv.af.backweb.ws;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.service.DemarchesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import mc.gouv.af.back.xls.ExcelExportModelProvider;
import mc.gouv.af.back.xls.ExcelExportService;
import mc.gouv.af.backweb.controller.AbstractController;
import org.springframework.web.bind.annotation.RequestParam;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeExportController.class);

    @RequestMapping(method = RequestMethod.GET, value = "/excel")
    public void exportExcel(HttpServletResponse response, @RequestParam(required = false) String creationStartDate , @RequestParam(required = false) String creationEndDate) {

        LOGGER.info("======================= Appel du controller /ws/export/excel");

        try {
            String demarcheId = gouvPropertiesResolver.getDemarcheId();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition", "attachment; filename=" +
                    demarchesService.getDemarche(demarcheId).getIdentifiantPrefixe() + "_Demande_Stat_" + AfBackUtils.generateFileDateSuffix() + ".xlsx");
            
            LOGGER.info("Constitution du modèle pour la génération Excel...");
            Map<String, Object> model = excelExportModelProvider.getModel(creationStartDate, creationEndDate);
            
            LOGGER.info("Appel export Excel...");
            excelExportService.exportExcel("demandes.xlsx", model, response.getOutputStream());

        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans exportExcel", e);
        }

        LOGGER.info("======================= Fin /ws/export/excel");
    }

}
