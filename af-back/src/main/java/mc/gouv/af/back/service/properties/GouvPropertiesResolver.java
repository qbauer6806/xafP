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
    
    String getDemJwt();

    String getFileUrl();

    String getFileJwt();

    String getMailUrl();

    String getMailJwt();

    String getProcessDefinitionKey();

    String getDemarcheId();

    String getUsagersRestUrl();

    String getPaysRestUrl();

    String getFrontUrl();

    String getContainerId();

    String getGouvSharedEnv();

    String getGouvSharedEnvColor();

    String getContactSupportUrl();

    String getFrontSharedKey();

    String getHelpUrl();

}
