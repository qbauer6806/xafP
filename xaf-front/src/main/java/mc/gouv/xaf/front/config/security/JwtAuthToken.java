package mc.gouv.xaf.front.config.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public class JwtAuthToken extends AbstractAuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = -4751312158728240194L;

    /**
     * Exemple de payload
     * {
    "sub": "INTRANET",
    "gouv" : {
    "shared" : {
       "roles": [
       "READER"
       ]},
    "tgf": {
       "benefEmets" : ["test","test1"]  
    }
    },
    "aud": "NOTIF",
    "iat": "2017-07-20T16:56:07+0200",
    "jti": "2f8e3fb3-8b94-4fc2-bf5a-87acaee8b9d3"
    }
     */
    private static final String GOUV_PROPERTY = "gouv";

    private Object principal;

    private Jws<Claims> jws;

    //Le token reçu en String
    private String token;

    //Le code de l'application lowercase ("tgf")
    private String applicationName;

    public JwtAuthToken(String token) {
        super(null);
        this.token = token;
    }

    public JwtAuthToken(String principal, Jws<Claims> jwt, Collection<? extends GrantedAuthority> authorities,
            String applicationName) {
        super(authorities);
        this.principal = principal;
        this.jws = jwt;
        this.applicationName = applicationName;
        super.setAuthenticated(true); // must use super, as we override
    }

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public Jws<Claims> getJwt() {
        return jws;
    }

    public void setJwt(Jws<Claims> jwt) {
        this.jws = jwt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return jws;
    }

    /**
     * 
     * @return une map comprenant les clés des champs contenu au sein du payload spécial pour le gouv
     * Exemple : "gouv" : {
    * "shared" : {
    *    "roles": [
    *   "READER"
    *   ]},
    *  "tgf": {
    *   "benefEmets" : ["test","test1"]  
    * }
    * retourne une map avec les clés "shared" et "tgf"
     */
    public Map<?, ?> getGouvProperties() {
        return (Map<?, ?>) jws.getBody().get(GOUV_PROPERTY);
    }

    /**
     * 
     * @return une map comprenant les clés des champs contenu au sein du payload spécial pour l'application
      * Exemple : "gouv" : {
    * "shared" : {
    *    "roles": [
    *   "READER"
    *   ]},
    *  "tgf": {
    *   "benefEmets" : ["test","test1"]  
    * }
    * retourne une map avec la clé "benefEmets"
     */
    public Map<?, ?> getApplicationProperties() {
        return (Map<?, ?>) getGouvProperties().get(applicationName);
    }

}
