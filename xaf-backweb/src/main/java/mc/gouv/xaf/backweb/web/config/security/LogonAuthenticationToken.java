package mc.gouv.xaf.backweb.web.config.security;

import java.io.Serial;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Token d'identification via Logon
 * @author fgaujous
 *
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper=false)
public class LogonAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 6738908439868866050L;

    public LogonAuthenticationToken(LogonBean logonBean, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.logonBean = logonBean;
    }

    transient LogonBean logonBean;

    @Override
    public Object getCredentials() {

        return logonBean;
    }

    @Override
    public Object getPrincipal() {
        return logonBean;
    }

}
