package mc.gouv.af.back.pdf;

import java.util.Map;
import java.util.Map.Entry;

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

    public Entry<String,Map<String,Object>> getTemplateAndModel(DemandeDTO demande);

    public Entry<String, Map<String, Object>> getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire);
    
}
