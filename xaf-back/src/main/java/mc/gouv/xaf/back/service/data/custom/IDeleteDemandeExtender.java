package mc.gouv.xaf.back.service.data.custom;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.exception.DemarcheException;

@FunctionalInterface
public interface IDeleteDemandeExtender {

    void executeExtraDeleteBeforeDemandeDeletion(DemandeBO demandeBO) throws DemarcheException;

}
