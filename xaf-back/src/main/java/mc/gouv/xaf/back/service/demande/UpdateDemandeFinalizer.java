package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.shared.dto.DemandeDTO;

@FunctionalInterface
public interface UpdateDemandeFinalizer {

    void finalizeDemandeUpdate(final DemandeDTO demandeDTO);
}
