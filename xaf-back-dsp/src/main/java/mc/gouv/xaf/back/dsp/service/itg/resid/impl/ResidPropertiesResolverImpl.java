package mc.gouv.xaf.back.dsp.service.itg.resid.impl;

import mc.gouv.xaf.back.dsp.service.itg.resid.ResidPropertiesResolver;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResidPropertiesResolverImpl implements ResidPropertiesResolver {


    @Value("${mc.gouv.${application.name}.backapi.itg.residapi.jwt}")
    private String residApiJwt;

    @Value("${mc.gouv.${application.name}.backapi.itg.residapi.url}")
    private String residApiUrlV2;

    @Value("${mc.gouv.${application.name}.backapi.itg.residback.url}")
    private String residBackUrl;

    @Override
    public String getResidApiJwt() {
        return residApiJwt;
    }

    @Override
    public String getResidApiUrlV2() {
        return residApiUrlV2;
    }

    @Override
    public String getResidBackUrl() {
        return residBackUrl;
    }

}
