package mc.gouv.xaf.back.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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

    public String getApiUlisMoyensGenerauxUrl() {
        return apiUlisMoyensGenerauxUrl;
    }

    public void setApiUlisMoyensGenerauxUrl(String apiUlisMoyensGenerauxUrl) {
        this.apiUlisMoyensGenerauxUrl = apiUlisMoyensGenerauxUrl;
    }

    public String getApiUlisTiersOrganisationUrl() {
        return apiUlisTiersOrganisationUrl;
    }

    public void setApiUlisTiersOrganisationUrl(String apiUlisTiersOrganisationUrl) {
        this.apiUlisTiersOrganisationUrl = apiUlisTiersOrganisationUrl;
    }

    public String getApiUlisAuthentUser() {
        return apiUlisAuthentUser;
    }

    public void setApiUlisAuthentUser(String apiUlisAuthentUser) {
        this.apiUlisAuthentUser = apiUlisAuthentUser;
    }

    public String getApiUlisAuthentPassword() {
        return apiUlisAuthentPassword;
    }

    public void setApiUlisAuthentPassword(String apiUlisAuthentPassword) {
        this.apiUlisAuthentPassword = apiUlisAuthentPassword;
    }

    public String getUlisFunctionalAccount() {
        return ulisFunctionalAccount;
    }

    public void setUlisFunctionalAccount(String ulisFunctionalAccount) {
        this.ulisFunctionalAccount = ulisFunctionalAccount;
    }
}
