package mc.gouv.xaf.back.service.pdf;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de génération de PDF (implémenté
 * dans la démarche cible et de stocker le résultat de cette génération.
 *
 * @author qdeme
 */
public interface PdfGenerationService {

    void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta)
            throws IOException, TikaException, SAXException;

    File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
            String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType);

	File generateToFile(DemandeDTO demande, PdfTemplateAndModelDTO dto);

	void saveFichier(String fileName, String url, DemandeDTO demande, String meta);
    
}
