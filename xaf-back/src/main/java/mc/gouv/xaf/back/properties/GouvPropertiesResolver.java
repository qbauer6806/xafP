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
    
    String getSmsUrl();
    
    String getSmsJwt();

    String getNomenUrl();

    String getNomenJwt();

    String getDemarcheId();

    String getContainerId();

    long getUsagersCacheDuration();

    long getUtilisateursCacheDuration();

    long getPaysCacheDuration();

    String getGouvSharedLogonUrl();

    String getGouvSharedLogonRestUrl();

    String getVScanUrl();

    boolean isVscanActivated();

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

    String getApiRioUrl();

    String getApiRioJwt();

    String getApiRioCodeAppli();

    String getApiRioCodeNotice();

    boolean isPaiementEnabled();

    String getPorteDocUrl();

    String getApiUlisMoyensGenerauxUrl();

    String getApiUlisTiersOrganisationUrl();

    String getApiUlisWorkflowUrl();

    String getApiUlisCommercialisationUrl();

    String getApiUlisAuthenticationUser();

    String getApiUlisAuthenticationPassword();

    String getApiUlisFunctionalUser();

    String getFrontUrl();

    String getBackUrl();

    String get2TiersBoUrl();

    String get2TiersBoJwt();

    String getMaxFileSize();

    String getExtensionsWhitelist();
    
    boolean getSmsEnabled();

    String getMwpaymtUrl();

}
