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
    public int getPermisParDefaut() {
        return 0;
    }

    @Override
    public int getImmatParDefaut() {
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
    public String getAllerUrl() {
        return null;
    }

    @Override
    public String getRetourUrl() {
        return null;
    }

    @Override
    public String getMenuUrl() {
        return null;
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
    public int getValiditeMaxMoyenPaiement() {
        return 0;
    }

    @Override
    public String getPaiementKey() {
        return "key";
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
    public String getAdressesMailAdminMetier() {
        return null;
    }

    @Override
    public String getAdressesMailSupportTechniqueCir() {
        return null;
    }

    @Override
    public String getAdressesMailSupportTechniqueRio() {
        return null;
    }

    @Override
    public String getXafRetryInitialDelay() {
        return null;
    }

    @Override
    public String getXafRetryCount() {
        return null;
    }

    @Override
    public String getXafRetryMultiplier() {
        return null;
    }

    @Override
    public String getXafPaiementImmediatHeureDiffere() {
        return null;
    }
}
