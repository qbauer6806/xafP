package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;

@FunctionalInterface
public interface ICreateDemandExtender {

    void applyCreateTreatment(final DemandeInputDTO demandeInputDTO, final DemandeDTO demandeDTO);
}
