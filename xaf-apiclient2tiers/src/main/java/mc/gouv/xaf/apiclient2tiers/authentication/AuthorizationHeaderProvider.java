package mc.gouv.xaf.apiclient2tiers.authentication;

/**
 * 
 * @author qdeme
 *
 */
public interface AuthorizationHeaderProvider {

    /**
     * 
     * @return La valeur à setter dans le Header "Authorization" afin de s'authentifier sur le serveur
     */
    public String getHeaderValue();

}
