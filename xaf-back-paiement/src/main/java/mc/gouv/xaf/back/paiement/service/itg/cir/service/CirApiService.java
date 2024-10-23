package mc.gouv.xaf.back.paiement.service.itg.cir.service;

import org.apache.http.client.HttpResponseException;

import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.RegistreDTO;

public interface CirApiService {

    PermisDTO getPermis(String numPermis, int pkDemande, String identifiantDemande) throws HttpResponseException;

    RegistreDTO getRegistre(Integer registre, int pkDemande, String identifiantDemande) throws HttpResponseException;

}
