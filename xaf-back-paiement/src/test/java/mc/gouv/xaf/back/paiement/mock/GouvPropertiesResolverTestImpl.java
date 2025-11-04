package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.stereotype.Component;

@Component
public class GouvPropertiesResolverTestImpl implements GouvPropertiesResolver {

    @Override
    public String getFileUrl() {
        return null;
    }

    @Override
    public String getFileJwt() {
        return null;
    }

    @Override
    public String getMailUrl() {
        return null;
    }

    @Override
    public String getMailJwt() {
        return null;
    }

    @Override
    public String getDemarcheId() {
        return null;
    }

    @Override
    public String getContainerId() {
        return null;
    }

    @Override
    public long getUsagersCacheDuration() {
        return 0;
    }

    @Override
    public long getUtilisateursCacheDuration() {
        return 0;
    }

    @Override
    public String getGouvSharedLogonUrl() {
        return null;
    }

    @Override
    public String getGouvSharedLogonRestUrl() {
        return null;
    }

    @Override
    public String getVScanUrl() {
        return null;
    }

    @Override
    public boolean isVscanActivated() {
        return true;
    }

    @Override
    public String getVscanJwt() {
        return null;
    }

    @Override
    public Integer getUsagersPageSize() {
        return null;
    }

    @Override
    public boolean isApiserver() {
        return false;
    }

    @Override
    public boolean isBackserver() {
        return false;
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public String getGichkeyUrl() {
        return null;
    }

    @Override
    public String getGichuniUrl() {
        return null;
    }

    @Override
    public boolean isPaiementEnabled() {
        return false;
    }

    @Override
    public String getPorteDocUrl() {
        return null;
    }

    @Override
    public String getFrontUrl() {
        return null;
    }

    @Override
    public String getBackUrl() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public String get2TiersBoUrl() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public String get2TiersBoJwt() {
        return null;
    }

    @Override
    public String getMaxFileSize() {
        return "";
    }

    @Override
    public String getExtensionsWhitelist() {
        return "";
    }

    @Override
    public String getSmsUrl() {
        return "";
    }

    @Override
    public String getSmsJwt() {
        return "";
    }

    @Override
    public boolean getSmsEnabled() {
        return false;
    }

    @Override
    public String getMwpaymtUrl() {
        return "";
    }

    @Override
    public String getNomenUrl() {
        return null;
    }

    @Override
    public String getNomenJwt() {
        return null;
    }

    @Override
    public long getPaysCacheDuration() {
        return 0;
    }

}
