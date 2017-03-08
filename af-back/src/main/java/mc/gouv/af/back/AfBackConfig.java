package mc.gouv.af.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.servicerest.pays.ReferentielPaysClient;
import mc.gouv.servicerest.usager.ReferentielUsagersClient;

@Configuration
@EnableCaching
public class AfBackConfig {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Bean
    public ReferentielPaysClient getReferentielPaysClient() {
        return new ReferentielPaysClient(gouvPropertiesResolver.getPaysRestUrl(), null, null);

    }

    @Bean
    public ReferentielUsagersClient getReferentielUsagersClient() {

        return new ReferentielUsagersClient(gouvPropertiesResolver.getUsagersRestUrl(), null, null);
    }

    @Bean
    public DemClient getDemClient() {
        return new DemClient(gouvPropertiesResolver.getDemUrl(), gouvPropertiesResolver.getDemUser(),
                gouvPropertiesResolver.getDemPwd());

    }

}
