package mc.gouv.xaf.back.service.itg.ulis;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import lombok.Getter;
import mc.gouv.xaf.apiclient.authentication.AuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.authentication.impl.BasicAuthorizationHeaderProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;

@Getter
public class UlisClientApi {


    public static final String CONTEXT_USER_HEADER = "Context-User";
    public static final String CONTEXT_SOCIETY_HEADER = "Context-Society";
    public static final String CONTEXT_APP_ORIGINE_HEADER = "AppOrigine";
    public static final String CONTEXT_PATH_RECHERCHE_FACTURE = "resultats-recherche/GLFAR042/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_TIERS = "resultats-recherche/TOPPR001/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_PROPOSITION = "resultats-recherche/ACGLR007/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_DEMANDE = "resultats-recherche/ACGLR006/API_LOGDOM";
    public static final String CONTEXT_PATH_SYNTHESE =
            "synthese-metier/ACGLY001/API_LOGDOM/ACDEM_NUM/%s/ACDOS_NUM/%s";
    public static final String CONTEXT_PATH_RECHERCHE_CONTRAT_LOCATIF =
            "resultats-recherche/GLCOR017/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_CONTRAT_LOCATIF_DETAILS =
            "synthese-metier/GLCOY020/API_LOGDOM/GLCON_NUM/%s/GLCON_NUMVER/%s";
    public static final String CONTEXT_PATH_PERSONNE_PHYSIQUE = "tiers/personnes-physiques/%s";
    public static final String CONTEXT_PATH_CODIFICATION = "codifications";
    public static final String CONTEXT_PATH_INDEXATION_GED = "indexation-ged";

    private final AuthorizationHeaderProvider authorizationHeaderProvider;
    private final WebTarget target;

    public UlisClientApi(String serviceUrl, String user, String password) {
        this.authorizationHeaderProvider = new BasicAuthorizationHeaderProvider(user, password);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new Apache5ConnectorProvider());

        try (Client client = ClientBuilder.newClient(clientConfig).register(UlisObjectMapperProvider.class)
                .register(UlisLogFilter.class)) {
            this.target = client.target(serviceUrl);
        }
    }

}
