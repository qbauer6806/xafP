package mc.gouv.xaf.apiclient.paiement;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import mc.gouv.xaf.shared.stc.utils.StcUtils;
import mc.gouv.xboot.apiclient.exception.ExceptionManager;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PaiementApiClient extends AfApiClient {

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl URL du WS à appeler
     * @param jwtToken   JWT à utiliser pour l'authentification
     */
    public PaiementApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, jwtToken);
    }

    public PaiementDTO getPaiement(String demandesId, String langue, Integer usagerId) {
        Response res = getTarget().path("/paiement")
                .queryParam("demandesId", demandesId)
                .queryParam(StcUtils.LANGUE_PARAM, langue)
                .queryParam(StcUtils.USAGERID_PARAM, usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(PaiementDTO.class);
    }

    public void updatePaiementStatus(String reference, String status) {
        Response res = getTarget().path("/paiement/" + reference + "/status/"+status)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue())
                .get();

        ExceptionManager.checkExceptionResponse(res);
    }


}
