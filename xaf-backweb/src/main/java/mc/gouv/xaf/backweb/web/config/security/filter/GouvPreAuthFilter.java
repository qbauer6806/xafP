package mc.gouv.xaf.backweb.web.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.security.GouvAuthenticationProvider;
import mc.gouv.xaf.backweb.web.config.security.LogonAuthenticationToken;
import mc.gouv.xaf.backweb.web.config.security.LogonBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre pré-authentification pour la passerelle Logon.
 * Ce filtre intercepte chaque requête afin de vérifier la présence du paramètre de session
 * Gouv ("ksession"). Si celui-ci est présent, il crée un LogonAuthenticationToken
 * et délègue l’authentification au GouvAuthenticationProvider.
 * Cela permet d’utiliser un mécanisme d’authentification basé uniquement sur le "ksession"
 * transmis dans la requête, sans demander à Spring Security de gérer un couple utilisateur/mot de passe.
 * En cas d’absence ou d’échec d’authentification, l’utilisateur est redirigé vers la page
 * de déconnexion du Logon partagé.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class GouvPreAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPreAuthFilter.class);

    private final BackGouvPropertiesResolver propertiesResolver;
    private final GouvAuthenticationProvider gouvAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var context = SecurityContextHolder.getContext();
        var gouvSession = request.getParameter(LogonBean.GOUV_SESSION_REQUEST_PARAM);

        // Déjà authentifié et pas de nouvelle session => on continue
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated() && StringUtils.isBlank(
                gouvSession)) {
            chain.doFilter(request, response);
            return;
        }

        // Paramètre session manquant
        if (StringUtils.isBlank(gouvSession)) {
            LOGGER.error("Session Logon inexistante dans la requête");
            redirectToLogout(response);
            return;
        }

        // Paramètres appRoot / appId requis
        var appRoot = request.getParameter(LogonBean.GOUV_APP_ROOT_REQUEST_PARAM);
        var appId = request.getParameter(LogonBean.GOUV_APP_ID_REQUEST_PARAM);

        if (StringUtils.isAnyBlank(appRoot, appId)) {
            LOGGER.error("Paramètres appRoot/appId invalides");
            throw new BadCredentialsException("Invalid appRoot or appId");
        }

        // Authentification via ksession
        var logonBean = new LogonBean(gouvSession, appRoot, appId);
        var authToken = new LogonAuthenticationToken(logonBean, null);

        try {
            var authenticated = gouvAuthenticationProvider.authenticate(authToken);
            context.setAuthentication(authenticated);
            
            // Redirection vers URL sans paramètres
            response.sendRedirect(request.getContextPath() + "/");
        } catch (AuthenticationException e) {
            LOGGER.error("Authentication failed", e);
            SecurityContextHolder.clearContext();
            redirectToLogout(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void redirectToLogout(HttpServletResponse response) throws IOException {
        response.sendRedirect(propertiesResolver.getGouvSharedLogonUrl() + "/logout.jsp");
    }
}
