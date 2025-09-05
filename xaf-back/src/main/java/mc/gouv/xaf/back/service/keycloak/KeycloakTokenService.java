package mc.gouv.xaf.back.service.keycloak;

import java.util.Map;
import mc.gouv.xaf.shared.RequestConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KeycloakTokenService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ClientRegistration clientRegistration;
    private final WebClient webClient;

    private static final String TOKEN_URL =
            "https://gichuni-front-dev.monaco-gouvernement.mc/auth/realms/gichuni/protocol/openid-connect/token";

    @Autowired
    public KeycloakTokenService(WebClient.Builder webClientBuilder,
            OAuth2AuthorizedClientManager authorizedClientManager,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId("gichuni");
        this.webClient = webClientBuilder.build();
    }

    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("gichuni")
                .principal("gichuni-client")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException("Impossible de récupérer un token OAuth2 depuis Keycloak.");
        }

        return authorizedClient.getAccessToken().getTokenValue();
    }

    /**
     * Token Exchange : échange un token usager contre un token technique
     */
    public String exchangeUserToken(String userToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(RequestConstant.GRANT_TYPE_PARAM, "urn:ietf:params:oauth:grant-type:token-exchange");
        params.add(RequestConstant.CLIENT_ID_PARAM, clientRegistration.getClientId());
        params.add(RequestConstant.CLIENT_SECRET_PARAM, clientRegistration.getClientSecret());
        params.add(RequestConstant.SUBJECT_TOKEN_PARAM, userToken);
        params.add(RequestConstant.SUBJECT_TOKEN_TYPE_PARAM, "urn:ietf:params:oauth:token-type:access_token");

        return webClient.post()
                .uri(clientRegistration.getProviderDetails().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> (String) body.get("access_token"))
                .block();
    }
}
