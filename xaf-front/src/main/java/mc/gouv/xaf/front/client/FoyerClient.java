package mc.gouv.xaf.front.client;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FoyerClient {

    private final RestClient restClient;

    public FoyerClient(RestClient.Builder builder,
            @Value("${mc.gouv.gichuni.api.url}") String gichuniApiUrl) {
        this.restClient = builder
                .baseUrl(gichuniApiUrl + "/household")
                .build();
    }

    public ResponseEntity<Object> get(String accessToken) {
        return restClient.get().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).retrieve()
                .toEntity(Object.class);
    }

    public ResponseEntity<Object> post(String accessToken, Object body) {
        return restClient.post().uri("/member").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).body(body)
                .retrieve().toEntity(Object.class);
    }

}
