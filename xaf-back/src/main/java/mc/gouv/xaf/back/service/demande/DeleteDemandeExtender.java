package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.exception.DemarcheException;

@FunctionalInterface
public interface DeleteDemandeExtender {

    void executeExtraDeleteBeforeDemandeDeletion(DemandeBO demandeBO) throws DemarcheException;

}
