package mc.gouv.xaf.backweb.web.config.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Token d'identification via Logon
 * @author fgaujous
 *
 */
public class LogonAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 6738908439868866050L;

    public LogonAuthenticationToken(LogonBean logonBean, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.logonBean = logonBean;
    }

    LogonBean logonBean;

    @Override
    public Object getCredentials() {

        return logonBean;
    }

    public LogonBean getLogonBean() {
        return logonBean;
    }

    public void setLogonBean(LogonBean logonBean) {
        this.logonBean = logonBean;
    }

    @Override
    public Object getPrincipal() {
        return logonBean;
    }

}
