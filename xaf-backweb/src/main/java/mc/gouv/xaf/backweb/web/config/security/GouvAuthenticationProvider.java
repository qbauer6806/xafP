package mc.gouv.xaf.backweb.web.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import mc.gouv.logon.apiclient.LogonApiClient;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.shared.Droit;
import mc.gouv.logon.shared.Role;
import mc.gouv.logon.shared.User;

/**
 * Composant Spring permettant de lier l'application à Logon pour la sécurité
 * 
 * @author qdeme
 *
 */
@Component
public class GouvAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvAuthenticationProvider.class);
    private static final String AUCUN_UTILISATEUR_ERREUR = "Aucun utilisateur n'a pu être récupéré à partir de la session";

    @Autowired
    private BackGouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private AuthenticationEventPublisher eventPublisher;

    @Value("${display.name}")
    private String displayName;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        LOGGER.info("GouvAuthenticationProvider.authenticate({})", authentication);

        var auth = (LogonAuthenticationToken) authentication;

        var logonBean = (LogonBean) auth.getCredentials();

        var sessionId = logonBean.getSessionId();

        User user;

        try {
            var logonApiClient = new LogonApiClient(gouvPropertiesResolver.getGouvSharedLogonRestUrl());
            user = logonApiClient.getRessUser().getLoggedUser(sessionId);
        } catch (RestException e) {
            LOGGER.error("Une erreur s'est produite lors de l'appel à Logon", e);
            throw new AccessDeniedException(AUCUN_UTILISATEUR_ERREUR);
        }

        if (user == null) {
            LOGGER.info(AUCUN_UTILISATEUR_ERREUR);
            throw new AccessDeniedException(AUCUN_UTILISATEUR_ERREUR);
        }
        LOGGER.info("Utilisateur retrouvé suite à l'appel à Logon : {}", user);

        // Constitution de l'Authentication Spring à l'aide des données obtenues de Logon

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
            // Il faut que ces droits concernent l'application en question
            if (role.getAppli().getCode().equals(displayName)) {
                Set<Droit> droits = role.getDroits();
                for (Droit droit : droits) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + droit.getCode()));
                }
            }
        }

        Authentication authFinal = new UsernamePasswordAuthenticationToken(user, logonBean, grantedAuthorities);
        //Publication de l'événement
        eventPublisher.publishAuthenticationSuccess(authFinal);
        return authFinal;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(LogonAuthenticationToken.class);
    }
}
