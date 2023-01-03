package mc.gouv.xaf.back.properties;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement. Proxy vers Static.getValue()
 * permettant via Spring de mocker les appels à Static.getValue().
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

    String getFrontUrl();

    String getBackUrl();

    String getContainerId();

    String getGouvSharedEnv();

    String getGouvSharedEnvColor();

    String getContactSupportUrl();

    String getFrontSharedKey();

    String getHelpUrl();

    long getUsagersCacheDuration();

    String getFrontFormStartPage();

    String getSearchHighlightPreTags();

    String getSearchHighlightPostTags();

    String getEsUser();

    String getEsPassword();

    String getEsClusterHosts();

    Integer getEsPort();

    Integer getEsReindexBulkSize();

    Integer getEsConnectTimeout();

    Integer getEsSocketTimeout();

    boolean getNovalidate();

    String getGouvSharedLogonUrl();

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

    String getGichkeyClientId();

    String getGichkeyClientSecret();

    String getApplicationPrefix();

    String getProxyUrl();

    String getProxyPort();

    String getApiRioUrl();

    String getApiRioJwt();

    String getApiRioCodeAppli();

    String getApiRioCodeNotice();

}
