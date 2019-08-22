package mc.gouv.af.back.pdf;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import mc.gouv.af.back.dto.PdfTemplateAndModelDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * @author qdeme
 * 
 * Permet à la démarche d'indiquer à af-back quel template utiliser pour générer un PDF pour une
 * certaine demande, ainsi que le modèle associé à ce template.
 *
 */
public interface PdfTemplateAndModelProvider {

    public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType);

    public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire, PdfTypeEnum pdfType);
    
    public PdfOptions getPdfOptions();
    
}
