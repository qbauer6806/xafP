package mc.gouv.xaf.back.dem;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Classe servant à produire un user de test
 * 
 * @author qdeme
 *
 */
public class MockSecurityContext implements SecurityContext {

    private static final long serialVersionUID = -1386535243513362694L;

    private Authentication authentication;

    public MockSecurityContext(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    protected static UsernamePasswordAuthenticationToken getPrincipal(final String user, final String pwd) {

        UserDetails ud = new UserDetails() {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public String getUsername() {
                return user;
            }

            @Override
            public String getPassword() {
                return pwd;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                GrantedAuthority ga = new GrantedAuthority() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getAuthority() {
                        if (user.equals(TestUtils.FRONT_USER_NAME)) {
                            return "ROLE_" + TestUtils.FRONT_USER_ROLE;
                        }
                        else if (user.equals(TestUtils.BACK_USER_NAME)) {
                            return "ROLE_" + TestUtils.BACK_USER_ROLE;
                        }
                        return "USER";
                    }
                };
                ArrayList<GrantedAuthority> l = new ArrayList<GrantedAuthority>();
                l.add(ga);
                return l;
            }
        };

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(ud,
                ud.getPassword(), ud.getAuthorities());

        return authentication;
    }
}