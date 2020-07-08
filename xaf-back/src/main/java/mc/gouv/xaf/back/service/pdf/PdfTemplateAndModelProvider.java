package mc.gouv.xaf.back.service.pdf;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

/**
 * 
 * @author qdeme
 * 
 * Permet à la démarche d'indiquer à xaf-back quel template utiliser pour générer un PDF pour une
 * certaine demande, ainsi que le modèle associé à ce template.
 *
 */
public interface PdfTemplateAndModelProvider {

    public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType);

    public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType);
    
    public PdfOptions getPdfOptions();
    
}
