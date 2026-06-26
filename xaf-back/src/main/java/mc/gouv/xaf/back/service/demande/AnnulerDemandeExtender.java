package mc.gouv.xaf.back.service.demande;

import java.util.Map;

@FunctionalInterface
public interface AnnulerDemandeExtender {

    void appliquerAnnulerTraitement(final Integer pkDemande, final Map<String, Object> businessVariables);
}
