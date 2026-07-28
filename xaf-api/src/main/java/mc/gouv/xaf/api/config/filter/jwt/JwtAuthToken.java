package mc.gouv.xaf.api.config.filter.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.Serial;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

@EqualsAndHashCode(callSuper = false)
public class JwtAuthToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -4751312158728240194L;

    @Setter
    @Getter
    private transient Object principal;

    private transient Jws<Claims> jws;

    //Le token reçu en String
    @Setter
    @Getter
    private String token;

    //Le code de l'application lowercase ("tgf")
    private String applicationName;

    public JwtAuthToken(String token) {
        super(AuthorityUtils.NO_AUTHORITIES);
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

    @Override
    public Object getCredentials() {
        return jws;
    }
}
