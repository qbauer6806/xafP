package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.back.data.entity.DemandeBO;

@FunctionalInterface
public interface CloneDemandeExtender {

    void applyCloneTreatment(final DemandeBO originalDemandBO, final DemandeBO clonedDemandBO);
}
