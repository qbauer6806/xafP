package mc.gouv.xaf.back.service.keycloak;

import java.util.Map;
import mc.gouv.xaf.shared.RequestConstant;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KeycloakTokenService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ClientRegistration clientRegistration;
    private final RestTemplate restTemplate;

    public KeycloakTokenService(OAuth2AuthorizedClientManager authorizedClientManager,
            ClientRegistrationRepository clientRegistrationRepository, RestTemplate restTemplate) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId("gichuni");
        this.restTemplate = restTemplate;
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(RequestConstant.GRANT_TYPE_PARAM, "urn:ietf:params:oauth:grant-type:token-exchange");
        params.add(RequestConstant.CLIENT_ID_PARAM, clientRegistration.getClientId());
        params.add(RequestConstant.CLIENT_SECRET_PARAM, clientRegistration.getClientSecret());
        params.add(RequestConstant.SUBJECT_TOKEN_PARAM, userToken);
        params.add(RequestConstant.SUBJECT_TOKEN_TYPE_PARAM, "urn:ietf:params:oauth:token-type:access_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                clientRegistration.getProviderDetails().getTokenUri(),
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = response.getBody();
            if (body != null && body.get("access_token") != null) {
                return (String) body.get("access_token");
            }
            throw new IllegalStateException("Le corps de la réponse ou le jeton d'accès est nul");
        } else {
            throw new IllegalStateException("Impossible d'échanger le token usager : " + response.getStatusCode());
        }
    }
}
