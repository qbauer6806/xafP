package mc.gouv.xaf.back.properties;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement. Proxy vers Static.getValue()
 * permettant via Spring de mocker les appels à Static.getValue().
 * 
 * @author qdeme
 *
 */
public interface GouvPropertiesResolver {

    String getFileUrl();

    String getFileJwt();

    String getMailUrl();

    String getMailJwt();

    String getProcessDefinitionKey();

    String getDemarcheId();

    String getUsagersRestUrl();

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

    Integer getJmsPort();

    String getJmsDataDir();

    String getJmsRedeliveryDelay();

    String getJmsRedeliveryMultiplier();

    String getJmsRedeliveryMaxAttemps();

    String getJmsDlq();

    String getJmsTopic();

    String getJmsHost();

    String getJmsSenderUser();

    String getJmsSenderPassword();

    String getJmsConsumerUser();

    String getJmsConsumerPassword();

    String getEsClusterName();

    String getEsHost();

    Integer getEsPort();

    String getEsNodeName();

    String getSubscriptionKey();

    Integer getEsReindexBulkSize();

    boolean getNovalidate();

	String getGouvSharedLogonUrl();

}
