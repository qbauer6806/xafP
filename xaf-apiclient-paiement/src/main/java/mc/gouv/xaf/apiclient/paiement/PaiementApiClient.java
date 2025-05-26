package mc.gouv.xaf.apiclient.paiement;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.exception.ExceptionManager;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import mc.gouv.xaf.shared.paiement.mongichet.PaymentMethodReferenceDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementInputDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;

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

    public InfoFacturationResponseDTO getInfoFacturation(GichuniUsagerDTO gichuniUsager) {
        Response res = getTarget().path("paiement/getinfofacturation").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(gichuniUsager));
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<InfoFacturationResponseDTO>() {

        });
    }



    public PaymentMethodInformationDTO getMoyenPaiement(InfoCancelInputDTO input, String usagerToken) {
        Response res = getTarget().path("paiement/moyenpaiement/info").queryParam("usagerToken", usagerToken)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(input));
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<>() {

        });
    }

    public void createMoyenPaiement(String demandeIds, GichuniUsagerDTO usager, String orderId, String usagerToken, String raisonSociale, String langue) {
        WebTarget target = getTarget().path("paiement/moyenpaiement").queryParam("demandeIds", demandeIds)
                .queryParam("orderId", orderId).queryParam("usagerToken", usagerToken).queryParam("langue", langue);
        // Ajouter seulement si non null / non vide
        if (raisonSociale != null && !raisonSociale.isBlank()) {
            target = target.queryParam("raisonSociale", raisonSociale);
        }
        Response res = target.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(usager));
        ExceptionManager.checkExceptionResponse(res);
    }

    public void updateMoyenPaiement(MoyenPaiementInputDTO moyenPaiementInputDTO) {
        Response res = getTarget().path("paiement/moyenpaiement").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.json(moyenPaiementInputDTO));
        ExceptionManager.checkExceptionResponse(res);
    }

    public List<PaymentMethodReferenceDTO> getReferences(String usagerToken) {
        Response res = getTarget().path("paiement/references").queryParam("usagerToken", usagerToken)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .get();
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<>() {

        });
    }

    public RegisterOutputDTO postInfoPaiement(RegisterInputDTO registerInputDTO, String usagerToken) {
        Response res = getTarget().path("paiement/infopaiement").queryParam("usagerToken", usagerToken).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(registerInputDTO));
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<>() {
        });
    }
}
