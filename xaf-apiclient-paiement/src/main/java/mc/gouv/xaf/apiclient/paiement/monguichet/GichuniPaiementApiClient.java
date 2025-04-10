package mc.gouv.xaf.apiclient.paiement.monguichet;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.paiement.monguichet.dto.ReferencePostInputDTO;
import mc.gouv.xaf.apiclient.paiement.monguichet.dto.ReferencePostOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.constants.MwpaymtConstant;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;

public class GichuniPaiementApiClient extends AfApiClient {

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *         URL du WS à appeler
     * @param bearerToken
     *         JWT à utiliser pour l'authentification
     */
    public GichuniPaiementApiClient(String serviceUrl, String bearerToken) {
        super(serviceUrl, bearerToken);
    }


    public ReferencePostOutputDTO saveMoyenPaiement(ReferencePostInputDTO referenceInputDTO) {
        Response res = getTarget().path("/payment-methods/reference")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(referenceInputDTO, MediaType.APPLICATION_JSON));
        return res.readEntity(ReferencePostOutputDTO.class);
    }

}
