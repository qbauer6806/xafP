package mc.gouv.xaf.back.paiement.mock;

import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

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
    public String getProcessDefinitionKey() {
        return null;
    }

    @Override
    public String getDemarcheId() {
        return "PERMC";
    }

    @Override
    public String getPaysRestUrl() {
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
    public String getSearchHighlightPreTags() {
        return null;
    }

    @Override
    public String getSearchHighlightPostTags() {
        return null;
    }

    @Override
    public String getEsUser() {
        return null;
    }

    @Override
    public String getEsPassword() {
        return null;
    }

    @Override
    public String getEsClusterHosts() {
        return null;
    }

    @Override
    public Integer getEsPort() {
        return null;
    }

    @Override
    public Integer getEsReindexBulkSize() {
        return null;
    }

    @Override
    public Integer getEsConnectTimeout() {
        return null;
    }

    @Override
    public Integer getEsSocketTimeout() {
        return null;
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
    public String getGouvSharedLogonRestUrl() {
        return null;
    }

    @Override
    public String getPiwikUrl() {
        return null;
    }

    @Override
    public String getPiwikSiteId() {
        return null;
    }

    @Override
    public String getVScanUrl() {
        return null;
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
    public String getGUKafkaBootstrapServersConfig() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return "PAIEMENT";
    }

    @Override
    public boolean getGUKafkaSSLEnabled() {
        return false;
    }

    @Override
    public String getGUKafkaSSLTrustStoreLocation() {
        return null;
    }

    @Override
    public String getGUKafkaSSLTrustStorePassword() {
        return null;
    }

    @Override
    public String getGUKafkaSSLKeyStoreLocation() {
        return null;
    }

    @Override
    public String getGUKafkaSSLKeyStorePassword() {
        return null;
    }

    @Override
    public boolean getKafkaEnabled() {
        return false;
    }

    @Override
    public String getGUKafkaProducerMaxRequestSize() {
        return null;
    }

    @Override
    public String getGUKafkaConsumerFetchMaxBytes() {
        return null;
    }

    @Override
    public String getGUKafkaConsumerMaxPartitionFetchBytes() {
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
    public String getApplicationPrefix() {
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

    @Override
    public String getApiRioUrl() {
        return null;
    }

    @Override
    public String getApiRioJwt() {
        return null;
    }

    @Override
    public String getApiRioCodeAppli() {
        return null;
    }

    @Override
    public String getApiRioCodeNotice() {
        return null;
    }

    @Override
    public boolean isPaiementEnabled() {
        return true;
    }

    @Override
    public String getPorteDocUrl() {
        return null;
    }

    @Override
    public String getApiUlisMoyensGenerauxUrl() {
        return null;
    }

    @Override
    public String getApiUlisTiersOrganisationUrl() {
        return null;
    }

    @Override
    public String getApiUlisAuthenticationUser() {
        return null;
    }

    @Override
    public String getApiUlisAuthenticationPassword() {
        return null;
    }

    @Override
    public String getApiUlisFunctionalUser() {
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
}
