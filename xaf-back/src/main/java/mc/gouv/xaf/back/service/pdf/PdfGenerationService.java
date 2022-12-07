package mc.gouv.xaf.back.service.pdf;

import java.io.File;
import java.io.IOException;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

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

	public File generateToFile(DemandeDTO demande, PdfTemplateAndModelDTO dto);

	public void saveFichier(String fileName, String url, DemandeDTO demande, String meta) throws Exception;

	public String sendToFile(File tempFile, DemandeDTO demande, String fileName) throws IOException;
    
}
