package mc.gouv.af.backweb.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mc.gouv.af.back.pdf.PdfGenerationService;
import mc.gouv.af.back.pdf.PdfTypeEnum;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.backweb.controller.AbstractController;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * Controller pour le service de génération d'aperçu PDF
 * 
 * @author qdeme
 * 
 */
@Controller
@Secured("ROLE_TRAITEMENT")
@RequestMapping("/ws/pdf")
public class ApercuPdfController extends AbstractController {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    private static final String COURRIER_TYPE = "COURRIER";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApercuPdfController.class);

    @RequestMapping(method = RequestMethod.GET, value = "/apercu")
    public void apercuPdf(HttpServletResponse response,
                          @RequestParam(required = true) String pkDemande,
                          @RequestParam(required = false) String commentaire,
                          @RequestParam(required = false) String texteAEnvoyer,
                          @RequestParam(required = true) String statut, @RequestParam(required = false) String langue,
                          @RequestParam(required = false) String codeMotif,
                          @RequestParam(required = false, defaultValue = COURRIER_TYPE) PdfTypeEnum pdfType) {

        LOGGER.info("======================= /pdf/apercu Génération d'aperçu PDF");

        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                Integer.valueOf(pkDemande));

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=" + demande.getIdentifiant() + ".pdf");

        LOGGER.info("Appel au service de génération de PDF...");
        File file = pdfGenerationService.generatePdfPreview(demande, statut, codeMotif, demande.getLangue(),
                commentaire, texteAEnvoyer, pdfType);

        try {
            LOGGER.info("Écriture du PDF dans l'OutputStream...");
            IOUtils.copy(new FileInputStream(file), response.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'écriture du PDF dans l'OutputStream", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        LOGGER.info("======================= Fin /pdf/apercu");
    }

}
