package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;

public interface PermisService {

    PermisDTO getPermis(String numPermis, int pkDemande, String identifiantDemande) throws Exception;
}
