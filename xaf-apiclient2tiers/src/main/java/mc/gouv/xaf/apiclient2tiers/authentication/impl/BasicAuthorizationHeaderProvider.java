package mc.gouv.xaf.apiclient2tiers.authentication.impl;

import mc.gouv.xaf.apiclient2tiers.authentication.AuthorizationHeaderProvider;
import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author qdeme
 *
 */
public class BasicAuthorizationHeaderProvider implements AuthorizationHeaderProvider {

    private String user;
    private String password;

    public BasicAuthorizationHeaderProvider(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public String getHeaderValue() {
        return "Basic " + new String(Base64.encodeBase64(new String(user + ":" + password).getBytes()));
    }

}
