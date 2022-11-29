package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import org.apache.http.client.HttpResponseException;

public interface PermisService {

    PermisDTO getPermis(String numPermis, int pkDemande, String identifiantDemande) throws HttpResponseException;

}
