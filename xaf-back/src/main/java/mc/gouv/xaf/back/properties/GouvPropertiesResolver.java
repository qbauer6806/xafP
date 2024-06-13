package mc.gouv.xaf.back.properties;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement.
 *
 * @author qdeme
 */
public interface GouvPropertiesResolver {

    String getFileUrl();

    String getFileJwt();

    String getMailUrl();

    String getMailJwt();

    String getProcessDefinitionKey();

    String getDemarcheId();

    String getPaysRestUrl();

    String getContainerId();

    long getUsagersCacheDuration();

    String getSearchHighlightPreTags();

    String getSearchHighlightPostTags();

    String getEsUser();

    String getEsPassword();

    String getEsClusterHosts();

    Integer getEsPort();

    Integer getEsReindexBulkSize();

    Integer getEsConnectTimeout();

    Integer getEsSocketTimeout();

    String getGouvSharedLogonUrl();

    String getGouvSharedLogonRestUrl();

    String getVScanUrl();

    String getVscanJwt();

    Integer getUsagersPageSize();

    boolean isApiserver();

    boolean isBackserver();

    String getGUKafkaBootstrapServersConfig();

    String getApplicationName();

    boolean getGUKafkaSSLEnabled();

    String getGUKafkaSSLTrustStoreLocation();

    String getGUKafkaSSLTrustStorePassword();

    String getGUKafkaSSLKeyStoreLocation();

    String getGUKafkaSSLKeyStorePassword();

    boolean getKafkaEnabled();

    String getGUKafkaProducerMaxRequestSize();

    String getGUKafkaConsumerFetchMaxBytes();

    String getGUKafkaConsumerMaxPartitionFetchBytes();

    String getGichkeyUrl();

    String getGichuniUrl();

    String getApplicationPrefix();

    String getProxyUrl();

    String getProxyPort();

    String getApiRioUrl();

    String getApiRioJwt();

    String getApiRioCodeAppli();

    String getApiRioCodeNotice();

    boolean isPaiementEnabled();

    String getPorteDocUrl();
    
    String getApiUlisMoyensGenerauxUrl();

    String getApiUlisTiersOrganisationUrl();

    String getApiUlisAuthenticationUser();

    String getApiUlisAuthenticationPassword();

    String getApiUlisFunctionalUser();

    String getFrontUrl();

    String getBackUrl();

	String get2TiersBoUrl();

	String get2TiersBoJwt();

}
