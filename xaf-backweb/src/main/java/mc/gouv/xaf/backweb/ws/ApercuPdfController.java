package mc.gouv.xaf.backweb.ws;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.formbean.PdfPreviewFormBean;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Controller pour le service de génération d'aperçu PDF
 * 
 * @author qdeme
 * 
 */
@Controller
@Secured({ "ROLE_TRAITEMENT", "ROLE_VALIDATION", "ROLE_VERIFICATION" })
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


    @RequestMapping(method = RequestMethod.POST, value = "/apercu", produces = MediaType.APPLICATION_PDF_VALUE)
    public void apercuPdf(HttpServletResponse response,
                          @Valid @RequestBody PdfPreviewFormBean pdfPreviewFormBean) {

        LOGGER.info("======================= /pdf/apercu Génération d'aperçu PDF");

        String statut = pdfPreviewFormBean.getAction();
        String codeMotif = pdfPreviewFormBean.getCodeMotifChoisi();
        Integer pkDemande = pdfPreviewFormBean.getPkDemande();
        String commentaire = pdfPreviewFormBean.getCommentaire();
        String texteAEnvoyer = pdfPreviewFormBean.getTexteAEnvoyer();
        PdfTypeEnum pdfType = pdfPreviewFormBean.getPdfType();

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
