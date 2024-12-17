package mc.gouv.xaf.back.dsp.service.itg.resid.impl;

import lombok.Getter;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidPropertiesResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ResidPropertiesResolverImpl implements ResidPropertiesResolver {

    @Value("${mc.gouv.${application.name}.shared.backapi.resid.api.jwt}")
    private String residApiJwt;

    @Value("${mc.gouv.resid.api.url}")
    private String residApiUrl;

    @Value("${mc.gouv.resid.back.url}")
    private String residBackUrl;
}
