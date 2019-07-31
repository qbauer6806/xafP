package mc.gouv.af.back;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.dem.service.DemGouvPropertiesResolver;
import mc.gouv.dem.service.properties.GouvProperty;

@Component
@Profile("test")
public class DemGouvPropertiesResolverImplMock implements DemGouvPropertiesResolver {

    @Override
    public String getValue(GouvProperty prop) {
        // TODO Auto-generated method stub
        return "TestDEM";
    }
}