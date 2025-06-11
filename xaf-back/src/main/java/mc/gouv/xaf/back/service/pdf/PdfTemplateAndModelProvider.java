package mc.gouv.xaf.back.service.pdf;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

/**
 * Permet à la démarche d'indiquer à xaf-back quel template utiliser pour générer un PDF pour une certaine demande,
 * ainsi que le modèle associé à ce template.
 *
 * @author qdeme
 */
public interface PdfTemplateAndModelProvider {

    void setTemplateAndModel(PdfTemplateAndModelDTO pdfTemplateAndModelDTO, DemandeDTO demande, String statutSuivant,
            PdfTypeEnum pdfTypeEnum, String codeMotif);

}
