package mc.gouv.xaf.back.service.motifs;

import java.util.Map;

import mc.gouv.xaf.back.shared.dto.DemandeDTO;

public interface MotifsTemplateModelProvider {

    /**
     * Crée le modèle à utiliser pour populer les modifs et commentaires préremplis
     */
    Map<String, Object> getModel(DemandeDTO demande);
}
