package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementPropertiesResolverTestImpl implements PaiementPropertiesResolver {
    @Override
    public String getFactureUrl() {
        return "http://linuxas-dev:30450/cir/api/";
    }

    @Override
    public String getFactureToken() {
        return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJUUyIsImF1ZCI6IkNJUiIsImlhdCI6IjIwMjItMDMtMThUMTA6MTM6MDMrMDEwMCIsImp0aSI6IjIxNDdjY2VmLTYyM2EtNGJlZS1iMTAxLWIxZTlmZjBlYTcxYiIsImdvdXYiOnsic2hhcmVkIjp7InJvbGVzIjpbIlRTIl19fX0.yoS9szzDx00GXhavi6mgkyPCR26tSDiiU4khINBbTSE";
    }


    @Override
    public int getRegistre() {
        return 7777;
    }

    @Override
    public String getCodeTarif() {
        return "P1";
    }

    @Override
    public String getTpe() {
        return "7527409";
    }

    @Override
    public String getVersion() {
        return "3.0";
    }

    @Override
    public String getPaiementUrl() {
        return "https://payment-api.e-i.com/test/capture_paiement.cgi";
    }

    @Override
    public String getPaiementKey() {
        return "key";
    }

    @Override
    public String getCompanyCode() {
        return "PERMC";
    }

    @Override
    public String getCurrency() {
        return "EUR";
    }
}
