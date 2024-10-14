package mc.gouv.xaf.back.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class UlisProperties {

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.url.moyens-generaux:OPTIONAL}")
    private String apiUlisMoyensGenerauxUrl;

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.url.tiers-organisation:OPTIONAL}")
    private String apiUlisTiersOrganisationUrl;

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.authentication.user:OPTIONAL}")
    private String apiUlisAuthentUser;

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.authentication.password:OPTIONAL}")
    private String apiUlisAuthentPassword;

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.account:OPTIONAL}")
    private String ulisFunctionalAccount;

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.url.workflow:OPTIONAL}")
    private String ulisUrlWorkflow;

    @Value("${mc.gouv.${application.name}.shared.backapi.ulis.url.commercialisation:OPTIONAL}")
    private String ulisUrlCommercialisation;

}
