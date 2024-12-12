package mc.gouv.xaf.backweb.ws;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.excel.AfExcelExportModelProvider;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller pour l'extraction des données des demandes (export excel)
 *
 * @author qdeme
 */
@GouvRestController
@Secured("ROLE_EXPORT")
@RequestMapping("/ws/export")
public class DemandeExportController extends AbstractController {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private AfExcelExportModelProvider excelExportModelProvider;

    @Autowired
    private DemarchesService demarchesService;

    @Autowired
    private DemandesService demandesService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeExportController.class);

    @GetMapping(value = "/excel")
    public void exportExcel(HttpServletResponse response, @RequestParam(required = false) String creationStartDate,
            @RequestParam(required = false) String creationEndDate) {

        LOGGER.info("======================= Appel du controller /ws/export/excel");
        String safeCreationStart = AfBackUtils.logSafe(creationStartDate);
        String safeCreationEnd = AfBackUtils.logSafe(creationEndDate);
        LOGGER.info("Paramètres de l'export [creationStartDate={}, creationEndDate={}]", safeCreationStart,
                safeCreationEnd);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition",
                    "attachment; filename=" + demarchesService.getDemarche().getIdentifiantPrefixe() + "_Donnees_Stat_"
                            + AfBackUtils.generateFileDateAndTimeSuffix() + ".xlsx");

            ExcelRechercheDTO excelRechercheDTO = new ExcelRechercheDTO();
            excelRechercheDTO.setCreationStartDate(creationStartDate);
            excelRechercheDTO.setCreationEndDate(creationEndDate);

            LOGGER.info("Constitution du modèle pour la génération Excel...");
            Map<String, Object> model = getModel(excelRechercheDTO);

            LOGGER.info("Appel export Excel...");
            excelExportService.exportExcel("demandes.xlsx", model, response.getOutputStream());

        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans exportExcel", e);
        }

        LOGGER.info("======================= Fin /ws/export/excel");
    }

    private Map<String, Object> getModel(ExcelRechercheDTO excelRecherche) {
        LOGGER.info("DemandeExportController.getModel()");

        Map<String, Object> model = new HashMap<>();
        List<AfDemandeExcelFlatDTO> demandesFlat = demandesService.retrieveDemandesFiltered(
                        excelRecherche.getCreationStartDate(), excelRecherche.getCreationEndDate(), excelRecherche.getStatut()).stream()
                .map(excelExportModelProvider::getDemandeFlat).toList();

        model.put("demandes", demandesFlat);

        LOGGER.info("Fin DemandeExportController.getModel()");

        return model;

    }
}
