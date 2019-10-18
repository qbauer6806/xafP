package mc.gouv.xaf.back.motifs;

import mc.gouv.dem.shared.model.DemandeDTO;

import java.util.Map;

public interface MotifsTemplateModelProvider {

    /**
     * Crée le modèle à utiliser pour populer les modifs et commentaires préremplis
     */
    Map<String, Object> getModel(DemandeDTO demande);
}
