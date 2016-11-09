package mc.gouv.af.back;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.af.back.util.LogonProxy;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.model.Droit;
import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;

/**
 * Composant Spring permettant de lier l'application à Logon pour la sécurité
 * 
 * @author qdeme
 *
 */
@Component
public class GouvAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvAuthenticationProvider.class);

    @Autowired
    private LogonProxy logonProxy;

    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        LOGGER.info("GouvAuthenticationProvider.authenticate(" + authentication + ")");

        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;

        LogonBean logonBean = (LogonBean) auth.getCredentials();

        String sessionId = logonBean.getSessionId();

        User user = null;

        try {
            user = logonProxy.getLoggedUser(sessionId);
        } catch (RestException e) {
            LOGGER.error("Une erreur s'est produite lors de l'appel à Logon", e);
            return null;
        }

        if (user == null) {
            LOGGER.info("Aucun utilisateur n'a pu être récupéré à partir de la session");
            return null;
        }
        LOGGER.info("Utilisateur retrouvé suite à l'appel à Logon : " + user);

        // Constitution de l'Authentication Spring à l'aide des données obtenues
        // de Logon
        String demarcheId = afBackUtils.getDemarcheId();
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            // Il faut que ces droits concernent l'application en question
            if (role.getAppli().getCode().equals(demarcheId)) {
                Set<Droit> droits = role.getDroits();
                for (Droit droit : droits) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + droit.getCode()));
                }
            }
        }

        return new UsernamePasswordAuthenticationToken(user, logonBean, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
