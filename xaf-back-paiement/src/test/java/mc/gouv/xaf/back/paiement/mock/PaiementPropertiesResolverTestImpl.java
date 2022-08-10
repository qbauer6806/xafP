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
        return 0;
    }

    @Override
    public String getVersionAller() {
        return null;
    }

    @Override
    public String getVersionCapture() {
        return "3.0";
    }

    @Override
    public String getCaptureUrl() {
        return "https://payment-api.e-i.com/test/capture_paiement.cgi";
    }

    @Override
    public String getSuccesUrl() {
        return null;
    }

    @Override
    public String getEchecUrl() {
        return null;
    }


    @Override
    public String getTpe() {
        return "7527409";
    }

    @Override
    public String getPaiementClef() {
        return "0123456789012345678901234567890123456789";
    }

    @Override
    public String getCodeSiteStandard() {
        return null;
    }

    @Override
    public String getXafMoneticoCodeSiteIframe() {
        return null;
    }

    @Override
    public String getXafMoneticoTexteAller() {
        return null;
    }

    @Override
    public String getCurrency() {
        return "EUR";
    }

    @Override
    public String getAdresseParDefaut() {
        return null;
    }

    @Override
    public String getVilleParDefaut() {
        return null;
    }

    @Override
    public String getCodePostalParDefaut() {
        return null;
    }

    @Override
    public String getCodePaysParDefaut() {
        return null;
    }

    @Override
    public int getXafRetryInitialDelay() {
        return 0;
    }

    @Override
    public int getXafRetryCount() {
        return 0;
    }

    @Override
    public int getXafRetryMultiplier() {
        return 0;
    }

    @Override
    public String getXafMonetico3dsv2Scenario() {
        return "null";
    }

    public String getApiRioUrl() {
        return null;
    }

    @Override
    public String getApiRioJwt() {
        return null;
    }

    @Override
    public String getProxyUrl() {
        return null;
    }

    @Override
    public String getProxyPort() {
        return null;
    }
}
