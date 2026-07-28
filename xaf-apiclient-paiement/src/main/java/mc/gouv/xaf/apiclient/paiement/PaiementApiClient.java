package mc.gouv.xaf.apiclient.paiement;

import java.util.List;

import mc.gouv.xaf.apiclient.AfApiClient;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

public class PaiementApiClient extends AfApiClient {

    public PaiementApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, jwtToken);
    }

    public List<TableauDTO> getTableauPaiement(String objectIds, String objectType, Integer usagerId) {
        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/paiement/tableaupaiement")
                        .queryParam(RequestConstant.ID_PARAM, objectIds)
                        .queryParam(RequestConstant.TYPE_PATH, objectType)
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<TableauDTO>>() {});
    }

    public InfoFacturationResponseDTO getInfoFacturation(GichuniUsagerDTO gichuniUsager) {
        return getRestClient().post()
                .uri("/paiement/getinfofacturation")
                .contentType(MediaType.APPLICATION_JSON)
                .body(gichuniUsager)
                .retrieve()
                .body(InfoFacturationResponseDTO.class);
    }

    public PaymentMethodInformationDTO getMoyenPaiement(InfoCancelInputDTO input, String usagerToken) {
        return getRestClient().post()
                .uri(uriBuilder -> uriBuilder
                        .path("/paiement/moyenpaiement/info")
                        .queryParam("usagerToken", usagerToken)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(input)
                .retrieve()
                .body(PaymentMethodInformationDTO.class);
    }

    public boolean createMoyenPaiement(
            String demandeIds,
            GichuniUsagerDTO usager,
            String orderId,
            String usagerToken,
            String raisonSociale,
            String langue) {

        Boolean result = getRestClient().post()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/paiement/moyenpaiement")
                            .queryParam("demandeIds", demandeIds)
                            .queryParam("orderId", orderId)
                            .queryParam("usagerToken", usagerToken)
                            .queryParam("langue", langue);

                    if (raisonSociale != null && !raisonSociale.isBlank()) {
                        builder.queryParam("raisonSociale", raisonSociale);
                    }

                    return builder.build();
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(usager)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(result);
    }

    public void updateMoyenPaiement(MoyenPaiementInputDTO moyenPaiementInputDTO, String usagerToken) {
        getRestClient().put()
                .uri(uriBuilder -> uriBuilder
                        .path("/paiement/moyenpaiement")
                        .queryParam("usagerToken", usagerToken)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(moyenPaiementInputDTO)
                .retrieve()
                .toBodilessEntity();
    }

    public List<PaymentMethodReferenceDTO> getReferences(String usagerToken) {
        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/paiement/references")
                        .queryParam("usagerToken", usagerToken)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<PaymentMethodReferenceDTO>>() {});
    }

    public RegisterOutputDTO postInfoPaiement(RegisterInputDTO registerInputDTO, String usagerToken) {
        return getRestClient().post()
                .uri(uriBuilder -> uriBuilder
                        .path("/paiement/infopaiement")
                        .queryParam("usagerToken", usagerToken)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerInputDTO)
                .retrieve()
                .body(RegisterOutputDTO.class);
    }
}
