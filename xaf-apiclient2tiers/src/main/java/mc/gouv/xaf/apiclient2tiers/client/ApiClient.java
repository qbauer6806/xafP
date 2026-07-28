package mc.gouv.xaf.apiclient2tiers.client;

import lombok.Getter;
import mc.gouv.xaf.apiclient2tiers.authentication.AuthorizationHeaderProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * @author qdeme
 */
@Getter
public abstract class ApiClient {

    private final String serviceUrl;
    private final AuthorizationHeaderProvider authorizationHeaderProvider;
    private final RestClient restClient;

    protected ApiClient(String serviceUrl, AuthorizationHeaderProvider authorizationHeaderProvider) {
        this.serviceUrl = serviceUrl;
        this.authorizationHeaderProvider = authorizationHeaderProvider;

        this.restClient = RestClient.builder().baseUrl(serviceUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderProvider.getHeaderValue()).build();
    }

}

