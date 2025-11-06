package mc.gouv.xaf.back.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class UlisProperties {

    @Value("${mc.gouv.appli.shared.backapi.ulis.url.moyens-generaux:}")
    private String apiUlisMoyensGenerauxUrl;

    @Value("${mc.gouv.appli.shared.backapi.ulis.url.tiers-organisation:}")
    private String apiUlisTiersOrganisationUrl;

    @Value("${mc.gouv.appli.shared.backapi.ulis.authentication.user:}")
    private String apiUlisAuthentUser;

    @Value("${mc.gouv.appli.shared.backapi.ulis.authentication.password:}")
    private String apiUlisAuthentPassword;

    @Value("${mc.gouv.appli.shared.backapi.ulis.account:}")
    private String ulisFunctionalAccount;

    @Value("${mc.gouv.appli.shared.backapi.ulis.url.workflow:}")
    private String ulisUrlWorkflow;

    @Value("${mc.gouv.appli.shared.backapi.ulis.url.commercialisation:}")
    private String ulisUrlCommercialisation;

}
