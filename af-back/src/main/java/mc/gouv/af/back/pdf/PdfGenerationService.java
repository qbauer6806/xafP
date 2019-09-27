package mc.gouv.af.back.pdf;

import java.io.File;

import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * @author qdeme
 * 
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de génération de PDF (implémenté
 * dans la démarche cible et de stocker le résultat de cette génération.
 *
 */
public interface PdfGenerationService {

    public void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta) throws Exception;

    public File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
            String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType);
    
}
