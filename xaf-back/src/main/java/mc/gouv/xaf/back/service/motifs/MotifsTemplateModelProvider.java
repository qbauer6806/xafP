package mc.gouv.xaf.back.service.motifs;

import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.Map;

public interface MotifsTemplateModelProvider {

    /**
     * Crée le modèle à utiliser pour populer les modifs et commentaires préremplis
     */
    Map<String, Object> getModel(DemandeDTO demande);

    Map<String, Object> getGenericModel();
}
