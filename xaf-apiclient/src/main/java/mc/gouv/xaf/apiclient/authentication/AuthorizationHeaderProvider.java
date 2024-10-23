package mc.gouv.xaf.apiclient.authentication;

public interface AuthorizationHeaderProvider {

    /**
     * @return La valeur à setter dans le Header "Authorization" afin de s'authentifier sur le serveur
     */
    String getHeaderValue();

}
