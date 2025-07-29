package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.shared.dto.DemandeDTO;

@FunctionalInterface
public interface IUpdateDemandeFinalizer {

    void finalizeDemandeUpdate(final DemandeDTO demandeDTO);
}
