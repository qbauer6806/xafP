package mc.gouv.xaf.back.service.itg.gichuni.api;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.ReferencePostOutputDTO;
import mc.gouv.xaf.shared.paiement.mongichet.PaymentMethodReferenceDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Classe permettant d'appeler l'API GICHUNI
 *
 * @author qdeme
 */
@Service
@RequiredArgsConstructor
public class GichuniApiClient {

    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final RestTemplate restTemplate;

    public GichuniUsagerDTO getUsager(Integer id) {
        GichuniUsagerDTO[] usagers = restTemplate.getForObject(
                gouvPropertiesResolver.getGichuniUrl() + "/profiles/profile-ids/" + id, GichuniUsagerDTO[].class);
        if (usagers == null || usagers.length == 0) {
            return null;
        }
        return usagers[0];
    }

    public List<GichuniUsagerDTO> getUsagers(List<Integer> ids) {
        // Concaténation des ids fournis pour le WS
        StringBuilder builder = new StringBuilder();
        for (Integer id : ids) {
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(id);
        }

        GichuniUsagerDTO[] usagers = restTemplate.getForObject(
                gouvPropertiesResolver.getGichuniUrl() + "/profiles/profile-ids/" + builder, GichuniUsagerDTO[].class);

        if (usagers == null || usagers.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.asList(usagers);
    }

    public ReferencePostOutputDTO saveReference(@NotNull String paymentMethodType, @NotNull String paymentMethodToken,
            String paymentSupplier, String demarcheId, String paymentMethodName, String usagerSub) {
        ReferencePostOutputDTO outputDTO = new ReferencePostOutputDTO();
        outputDTO.setPaymentSupplier(paymentSupplier);
        outputDTO.setPaymentMethodToken(paymentMethodToken);
        outputDTO.setPaymentMethodName(paymentMethodName);
        outputDTO.setTokenSupplier(demarcheId);
        outputDTO.setPaymentMethodType(paymentMethodType);
        outputDTO.setProfileId(usagerSub);
        return restTemplate.postForObject(
                gouvPropertiesResolver.getGichuniUrl() + "/payment-methods/reference", outputDTO,
                ReferencePostOutputDTO.class);
    }

    public List<PaymentMethodReferenceDTO> getReferences(String usagerToken) {
        RestTemplate restTemplateWithToken = restTemplateWithToken(usagerToken);
        ResponseEntity<List<PaymentMethodReferenceDTO>> response = restTemplateWithToken.exchange(
                gouvPropertiesResolver.getGichuniUrl() + "/payment-methods/references",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    private RestTemplate restTemplateWithToken(String token) {
        RestTemplate restTemplateWithToken = new RestTemplate();
        restTemplateWithToken.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setBearerAuth(token);
            return execution.execute(request, body);
        });
        return restTemplateWithToken;
    }
}
