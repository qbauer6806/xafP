package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Injection de code depuis le TS. Permet de ne pas surcharger la méthode de creation de demande pour chaque
 * comportement custom
 */

@FunctionalInterface
public interface CreateDemandeFinalizer {

    void finalizeDemandeCreation(final DemandeDTO demandeDTO);
}
