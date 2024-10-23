package mc.gouv.xaf.backweb.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.formbean.PdfPreviewFormBean;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Controller pour le service de génération d'aperçu PDF
 *
 * @author qdeme
 */
@GouvRestController
@Secured("ROLE_LECTURE")
@RequestMapping("/ws/pdf")
public class ApercuPdfController extends AbstractController {

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApercuPdfController.class);

    @PostMapping(value = "/apercu", produces = MediaType.APPLICATION_PDF_VALUE)
    public void apercuPdf(HttpServletResponse response, @Valid @RequestBody PdfPreviewFormBean pdfPreviewFormBean) {

        LOGGER.info("======================= /pdf/apercu Génération d'aperçu PDF");

        String statut = pdfPreviewFormBean.getAction();
        String codeMotif = pdfPreviewFormBean.getCodeMotifChoisi();
        Integer pkDemande = pdfPreviewFormBean.getPkDemande();
        String commentaire = pdfPreviewFormBean.getCommentaire();
        String texteAEnvoyer = pdfPreviewFormBean.getTexteAEnvoyer();
        PdfTypeEnum pdfType = pdfPreviewFormBean.getPdfType();

        DemandeDTO demande = demandesService.getDemande(pkDemande);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=" + demande.getIdentifiant() + ".pdf");

        LOGGER.info("Appel au service de génération de PDF...");
        File file = pdfGenerationService.generatePdfPreview(demande, statut, codeMotif, demande.getLangue(),
                commentaire, texteAEnvoyer, pdfType);

        try (FileInputStream fis = new FileInputStream(file)) {
            LOGGER.info("Écriture du PDF dans l'OutputStream...");
            IOUtils.copy(fis, response.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'écriture du PDF dans l'OutputStream", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // Supprimer le fichier temporaire car il n'est plus utile
        LOGGER.info("Suppression du fichier temporaire...");
        try {
            Files.delete(Paths.get(file.getPath()));
        } catch (IOException e) {
            LOGGER.warn("La suppression du fichier temporaire a échoué", e);
        }

        LOGGER.info("======================= Fin /pdf/apercu");
    }

}
