package mc.gouv.xaf.back.service.itg.logon;

import java.util.List;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LogonClient {

    private final RestClient restClient;

    public LogonClient(@Value("${mc.gouv.logon.api.url}") String logonApiUrl) {
        this.restClient = RestClient.builder().requestFactory(getClientHttpRequestFactory()).baseUrl(logonApiUrl)
                .build();
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        // Le timeout par défaut étant de 10s on est obligé de monter à 30s car l'api met environ 15s à répondre pour getListUserByCodeAppli
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(30000);
        factory.setConnectTimeout(30000);
        return factory;
    }

    public User getLoggedUser(String sessionId) {
        return restClient.get().uri("/user/logged/{sessionId}", sessionId).accept(MediaType.APPLICATION_JSON).retrieve()
                .body(User.class);
    }

    public List<User> getListUserByCodeAppli(String codeAppli) {
        return restClient.get().uri("/user/appli/{codeAppli}", codeAppli).accept(MediaType.APPLICATION_JSON).retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {

                });
    }

    public User getUserByMatricule(String matricule) {
        return restClient.get().uri("/user/mat/{matricule}", matricule).accept(MediaType.APPLICATION_JSON).retrieve()
                .body(User.class);
    }
}
