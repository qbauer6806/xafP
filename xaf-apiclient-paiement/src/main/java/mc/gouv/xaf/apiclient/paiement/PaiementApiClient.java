package mc.gouv.xaf.apiclient.paiement;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.shared.stc.MoyenPaiementDTO;
import mc.gouv.xboot.apiclient.exception.ExceptionManager;

import javax.ws.rs.client.Entity;
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

    public PaiementDTO getPaiement(String demandesId, String langue, Integer usagerId, boolean iframe) {
        Response res = getTarget().path("/paiement")
                .queryParam("demandesId", demandesId)
                .queryParam(PaiementConstant.LANGUE_PARAM, langue)
                .queryParam(PaiementConstant.USAGERID_PARAM, usagerId)
                .queryParam(PaiementConstant.IFRAME_PARAM, iframe)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(PaiementDTO.class);
    }

    public void updatePaiementStatus(MoyenPaiementDTO moyenPaiementDTO) {
        Response res = getTarget().path("/paiement")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(moyenPaiementDTO, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
    }


}
