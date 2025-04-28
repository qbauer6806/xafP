package mc.gouv.xaf.apiclient.paiement.mwpaymt;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.exception.ExceptionManager;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.constants.MwpaymtConstant;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import org.springframework.stereotype.Service;

public class MwpaymtApiClient extends AfApiClient {

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *         URL du WS à appeler
     * @param bearerToken
     *         JWT à utiliser pour l'authentification
     */
    public MwpaymtApiClient(String serviceUrl, String bearerToken) {
        super(serviceUrl, bearerToken);
    }

    public RegisterOutputDTO getToken(RegisterInputDTO input) {
        Response res = getTarget().path(MwpaymtConstant.REGISTER_PATH)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(input, MediaType.APPLICATION_JSON));


        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(RegisterOutputDTO.class);
    }

    public InfoOutputDTO getInfo(InfoCancelInputDTO input) {
        Response res = getTarget().path(MwpaymtConstant.INFO_PATH)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(input, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(InfoOutputDTO.class);
    }

    public DebitOutputDTO debit(DebitInputDTO input) {
        Response res = getTarget().path(MwpaymtConstant.DEBIT_PATH)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(input, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(DebitOutputDTO.class);
    }

}
