package mc.gouv.xaf.back.service.keycloak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

@Service
public class KeycloakTokenService {


    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    public KeycloakTokenService(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
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

}
