package mc.gouv.xaf.back.service.pdf;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

import java.io.File;
import java.io.IOException;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de génération de PDF (implémenté
 * dans la démarche cible et de stocker le résultat de cette génération.
 *
 * @author qdeme
 */
public interface PdfGenerationService {

    void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta)
            throws IOException;

    void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta, File tempFile)
            throws IOException;

    File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
            String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType);

	File generateToFile(DemandeDTO demande, PdfTemplateAndModelDTO dto);

	void saveFichier(String fileName, String url, DemandeDTO demande, String meta);
    
}
