package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * * Injection de code depuis le TS. Permet de ne pas surcharger la méthode de repondreDemandeComplements pour chaque *
 * comportement custom
 */

@FunctionalInterface
public interface ReponseDemandeInfoComplFinalizer {

    void finaliserReponseDemandeInfoCompl(final DemandeDTO demandeDTO,
            final DemandeComplementsDTO demandeComplementsDto);
}
