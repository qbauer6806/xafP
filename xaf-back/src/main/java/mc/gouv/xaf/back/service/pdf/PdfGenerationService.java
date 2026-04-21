package mc.gouv.xaf.back.service.pdf;

import java.io.File;
import java.io.IOException;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de génération de PDF (implémenté dans la
 * démarche cible et de stocker le résultat de cette génération.
 *
 * @author qdeme
 */
public interface PdfGenerationService {

    void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta) throws IOException;

    void generateAndStoreDoc(DemandeDTO demande, PdfTypeEnum pdfType, String meta,
            PdfTemplateAndModelDTO pdfTemplateAndModelDTO, boolean convertPdf) throws IOException;

    File generateToFile(PdfTemplateAndModelDTO dto, boolean convertPdf);

    File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
            String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType);

}
