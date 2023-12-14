package mc.gouv.xaf.back.dsp.service.itg.resid.impl;

import mc.gouv.Static;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidPropertiesResolver;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResidPropertiesResolverImpl implements ResidPropertiesResolver {

    private static final String RESID_API_JWT = "mc.gouv.%s.backapi.itg.residapi.jwt";
    private static final String RESID_API_URL_V2 = "mc.gouv.%s.backapi.itg.residapi.url";
    private static final String RESID_BACK_URL = "mc.gouv.%s.backapi.itg.residback.url";

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public String getResidApiJwt() {
        return Static.getValue(String.format(RESID_API_JWT, gouvPropertiesResolver.getDemarcheId().toLowerCase()));
    }

    @Override
    public String getResidApiUrlV2() {
        return Static.getValue(String.format(RESID_API_URL_V2, gouvPropertiesResolver.getDemarcheId().toLowerCase()));
    }

    @Override
    public String getResidBackUrl() {
        return Static.getValue(String.format(RESID_BACK_URL, gouvPropertiesResolver.getDemarcheId().toLowerCase()));
    }

}
