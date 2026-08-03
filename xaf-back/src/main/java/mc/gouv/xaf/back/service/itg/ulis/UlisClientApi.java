package mc.gouv.xaf.back.service.itg.ulis;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import mc.gouv.xaf.apiclient.authentication.AuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.authentication.impl.BasicAuthorizationHeaderProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Getter
public class UlisClientApi {


    public static final String CONTEXT_USER_HEADER = "Context-User";
    public static final String CONTEXT_SOCIETY_HEADER = "Context-Society";
    public static final String CONTEXT_APP_ORIGINE_HEADER = "AppOrigine";
    public static final String CONTEXT_PATH_RECHERCHE_FACTURE = "resultats-recherche/GLFAR042/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_TIERS = "resultats-recherche/TOPPR001/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_PROPOSITION = "resultats-recherche/ACGLR007/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_AFFAIRES = "resultats-recherche/PLRCR002";
    public static final String CONTEXT_PATH_RECHERCHE_DEMANDE = "resultats-recherche/ACGLR006/API_LOGDOM";
    public static final String CONTEXT_PATH_SYNTHESE =
            "synthese-metier/ACGLY001/API_LOGDOM/ACDEM_NUM/%s/ACDOS_NUM/%s";
    public static final String CONTEXT_PATH_RECHERCHE_REFERENCE_COMMISSION =
            "synthese-metier/ACGLY005/PROPATTRIB/ACDOS_NUMDIR/%s/ACPRO_NUM/%s";
    public static final String CONTEXT_PATH_RECHERCHE_COMMISSIONS =
            "resultats-recherche/ACGLR020/ACGLR020MC";
    public static final String CONTEXT_PATH_RECHERCHE_CONTRAT_LOCATIF =
            "resultats-recherche/GLCOR017/API_LOGDOM";
    public static final String CONTEXT_PATH_RECHERCHE_CONTRAT_LOCATIF_DETAILS =
            "synthese-metier/GLCOY020/API_LOGDOM/GLCON_NUM/%s/GLCON_NUMVER/%s";
    public static final String CONTEXT_PATH_PERSONNE_PHYSIQUE = "tiers/personnes-physiques/%s";
    public static final String CONTEXT_PATH_CODIFICATION = "codifications";
    public static final String CONTEXT_PATH_INDEXATION_GED = "indexation-ged";
    public static final String CONTEXT_PATH_EDITION_BUREAUTIQUE = "editions-bureautiques";
    public static final String CONTEXT_PATH_CREATION_AFFAIRE = "crm/affaires";

    private final RestClient restClient;
    private final RestClient restClientNoBodyLog;
    private final AuthorizationHeaderProvider authorizationHeaderProvider;

    public static final JsonMapper ULIS_MAPPER = JsonMapper.builder()
            .addModule(new SimpleModule()
                    .addSerializer(OffsetDateTime.class, new UlisOffsetDateTimeSerializer()))
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public UlisClientApi(String serviceUrl, String user, String password) {
        this.authorizationHeaderProvider = new BasicAuthorizationHeaderProvider(user, password);

        this.restClient = RestClient.builder().baseUrl(serviceUrl)
                .requestFactory(UlisLogInterceptor.requestFactory())
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderProvider.getHeaderValue())
                .requestInterceptor(new UlisLogInterceptor())
                .configureMessageConverters(builder ->
                        builder.withJsonConverter(new JacksonJsonHttpMessageConverter(ULIS_MAPPER)))
                .build();

        this.restClientNoBodyLog = RestClient.builder().baseUrl(serviceUrl)
                .requestFactory(UlisLogInterceptor.requestFactory())
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderProvider.getHeaderValue())
                .requestInterceptor(new UlisLogInterceptor(false))
                .configureMessageConverters(builder ->
                        builder.withJsonConverter(new JacksonJsonHttpMessageConverter(ULIS_MAPPER)))
                .build();
    }
}
