package mc.gouv.af.back.service.properties;

/**
 * Composant permettant de récupérer des éléments de configuration propres au gouvernement. Proxy vers Static.getValue()
 * permettant via Spring de mocker les appels à Static.getValue().
 * 
 * @author qdeme
 *
 */
public interface GouvPropertiesResolver {

    String getDemUrl();

    String getDemUser();

    String getFileUrl();

    String getDemPwd();

    String getFileUser();

    String getMailUrl();

    String getFilePwd();

    String getMailUser();

    String getMailPwd();

    String getProcessDefinitionKey();

    String getDemarcheId();

    String getUsagersRestUrl();

    String getPaysRestUrl();

    String getFrontUrl();

    String getDemFrontUser();

    String getDemFrontPwd();

    String getContainerId();

    String getGouvSharedEnv();

    String getGouvSharedEnvColor();

    String getContactSupportUrl();

    String getFrontSharedKey();

    String getHelpUrl();

}
