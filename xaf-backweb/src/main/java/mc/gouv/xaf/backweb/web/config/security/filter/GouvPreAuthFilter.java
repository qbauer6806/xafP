package mc.gouv.xaf.backweb.web.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.security.LogonAuthenticationToken;
import mc.gouv.xaf.backweb.web.config.security.LogonBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Pour que GouvAuthenticationProvider.authenticate() puisse être appelé et puisse donc analyser le header "ksession"
 * pour appeler Logon afin d'effectuer l'authentification, il faut d'abord fournir un user/mdp à Spring en premier
 * lieu... or ce n'est pas ce que l'on souhaite. On souhaite simplement donner le ksession dans les headers HTTP. Du
 * coup, ce filter crée une authentification factice afin d'entrer dans le authenticate() qui vérifie le ksession
 *
 * @author qdeme
 */
public class GouvPreAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPreAuthFilter.class);

    private BackGouvPropertiesResolver propertiesResolver;

    public GouvPreAuthFilter(BackGouvPropertiesResolver propertiesResolver) {
        this.propertiesResolver = propertiesResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            FilterChain filterChain) throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        String gouvSession = httpRequest.getParameter(LogonBean.GOUV_SESSION_REQUEST_PARAM);

        //On vérifie si GOUV_SESSION_REQUEST_PARAM n'est pas vide car ça se peut qu'une autre personne veuille se connecter sans déconnexion de la précédente au préalable
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated() && StringUtils.isBlank(
                gouvSession)) {
            // do nothing
        } else {

            if (StringUtils.isBlank(gouvSession)) {
                LOGGER.error("Session Logon inexistante dans la requête");
                // redirection sur la page de logon
                httpResponse.sendRedirect(propertiesResolver.getGouvSharedLogonUrl() + "/logout.jsp");
                return;
            }

            String appRoot = httpRequest.getParameter(LogonBean.GOUV_APP_ROOT_REQUEST_PARAM);

            if (StringUtils.isBlank(gouvSession)) {
                LOGGER.error("appRoot inexistant dans la requête");
                throw new BadCredentialsException("Invalid appRoot");
            }

            String appId = httpRequest.getParameter(LogonBean.GOUV_APP_ID_REQUEST_PARAM);

            if (StringUtils.isBlank(appId)) {
                LOGGER.error("appId inexistante dans la requête");
                throw new BadCredentialsException("Invalid appId");
            }

            LogonBean logonBean = new LogonBean(gouvSession, appRoot, appId);

            LogonAuthenticationToken auth = new LogonAuthenticationToken(logonBean, null);
            // Gestion de l'authentification via le provider (si à true, le système pense que l'utilisateur est admis
            auth.setAuthenticated(false);
            context.setAuthentication(auth);
        }

        filterChain.doFilter(httpRequest, httpResponse);
    }

}
