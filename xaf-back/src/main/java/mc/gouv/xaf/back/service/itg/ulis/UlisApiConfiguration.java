package mc.gouv.xaf.back.service.itg.ulis;

import mc.gouv.xaf.back.properties.UlisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/*
 * Composant qui gère la configuration du client API vers ULIS pour chaque opération: tiers, factures ou proposition.
 */
@Component
public class UlisApiConfiguration {

    private static String MOYENS_GENERAUX_URL = "/moyens-generaux/api/v1/";
    private static String TIERS_ORGANISATION_V1_URL = "/tiers-organisation/api/v1/";
    private static String WORKFLOW_URL = "/workflow/api/v1/";
    private static String ACCUEIL_COMMERCIALISATION_URL = "/accueil-commercialisation/api/v1/";

    @Autowired
    private UlisProperties ulisProperties;

    /*
     * Ces 4 beans sont injectés dans LogdomcUlisApiService pour effectuer les différentes opérations sur ULIS que ce
     * soit sur des propositions, des tiers ou des factures.
     */

    @Bean
    @Qualifier("ulisMoyensGenerauxClientApi")
    public UlisClientApi ulisMoyensGenerauxClientApi() {
        return new UlisClientApi(ulisProperties.getApiUlisUrl() + MOYENS_GENERAUX_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    @Qualifier("ulisTiersOrganisationClientApi")
    public UlisClientApi ulisTiersOrganisationClientApi() {
        return new UlisClientApi(ulisProperties.getApiUlisUrl() + TIERS_ORGANISATION_V1_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    @Qualifier("workflowClientApi")
    public UlisClientApi ulisWorkflowClientApi() {
        return new UlisClientApi(
                ulisProperties.getApiUlisUrl() + WORKFLOW_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }

    @Bean
    @Qualifier("commercialisationClientApi")
    public UlisClientApi ulisCommercialisationClientApi() {
        return new UlisClientApi(
                ulisProperties.getApiUlisUrl() + ACCUEIL_COMMERCIALISATION_URL,
                ulisProperties.getApiUlisAuthentUser(),
                ulisProperties.getApiUlisAuthentPassword());
    }    
}
