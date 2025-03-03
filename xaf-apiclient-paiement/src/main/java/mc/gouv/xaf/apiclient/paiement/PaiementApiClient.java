package mc.gouv.xaf.apiclient.paiement;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.exception.ExceptionManager;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import mc.gouv.xaf.shared.RequestConstant;
import java.util.List;

public class PaiementApiClient extends AfApiClient {

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *         URL du WS à appeler
     * @param jwtToken
     *         JWT à utiliser pour l'authentification
     */
    public PaiementApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, jwtToken);
    }

    public List<TableauDTO> getTableauPaiement(String objectIds, String objectType, Integer usagerId) {
        Response res = getTarget().path("paiement/tableaupaiement").queryParam(RequestConstant.ID_PARAM, objectIds)
                .queryParam(RequestConstant.TYPE_PATH, objectType).queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<>() {
        });
    }
}
