package mc.gouv.xaf.back.service.itg.ulis;

import mc.gouv.xaf.back.properties.UlisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import tools.jackson.databind.module.SimpleModule;
import java.time.OffsetDateTime;

/*
 * Composant qui gère la configuration du client API vers ULIS pour chaque opération: tiers, factures ou proposition.
 */
@Component
public class UlisApiConfiguration {

    private static String MOYENS_GENERAUX_URL = "/moyens-generaux/api/v1/";
    private static String MOYENS_GENERAUX_V2_URL = "/moyens-generaux/api/v2/";
    private static String TIERS_ORGANISATION_V1_URL = "/tiers-organisation/api/v1/";
    private static String WORKFLOW_URL = "/workflow/api/v1/";
    private static String ACCUEIL_COMMERCIALISATION_URL = "/accueil-commercialisation/api/v1/";
    private static String GESTION_RELATION_TIERS_URL = "/gestion-relation-tiers/api/v1/";
    private static String PATRIMOINE_V1_URL = "/patrimoine/api/v1/";

    @Autowired
    private UlisProperties ulisProperties;

    /*
     * Ces 6 beans sont injectés dans LogdomcUlisApiService pour effectuer les différentes opérations sur ULIS que ce
     * soit sur des propositions, des tiers ou des factures.
     */

    @Bean
    public UlisClientApi ulisMoyensGenerauxClientApi() {
        return new UlisClientApi(ulisProperties.getApiUlisUrl() + MOYENS_GENERAUX_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    public UlisClientApi ulisMoyensGenerauxClientV2Api() {
        return new UlisClientApi(ulisProperties.getApiUlisUrl() + MOYENS_GENERAUX_V2_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    public UlisClientApi ulisTiersOrganisationClientApi() {
        return new UlisClientApi(ulisProperties.getApiUlisUrl() + TIERS_ORGANISATION_V1_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    public UlisClientApi workflowClientApi() {
        return new UlisClientApi(
                ulisProperties.getApiUlisUrl() + WORKFLOW_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    public UlisClientApi commercialisationClientApi() {
        return new UlisClientApi(
                ulisProperties.getApiUlisUrl() + ACCUEIL_COMMERCIALISATION_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    public UlisClientApi gestionRelationTiersClientApi() {
        return new UlisClientApi(
                ulisProperties.getApiUlisUrl() + GESTION_RELATION_TIERS_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    public UlisClientApi patrimoineClientApi() {
        return new UlisClientApi(
                ulisProperties.getApiUlisUrl() + PATRIMOINE_V1_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    JsonMapperBuilderCustomizer ulisDateCustomizer() {
        return builder -> builder.addModule(new SimpleModule()
                .addSerializer(OffsetDateTime.class, new UlisOffsetDateTimeSerializer()));
    }
}
