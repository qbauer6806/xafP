package mc.gouv.xaf.backweb.web.config.security.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.security.LogonAuthenticationToken;
import mc.gouv.xaf.backweb.web.config.security.LogonBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Pour que GouvAuthenticationProvider.authenticate() puisse être appelé et puisse donc analyser le header "ksession"
 * pour appeler Logon afin d'effectuer l'authentification, il faut d'abord fournir un user/mdp à Spring en premier
 * lieu... or ce n'est pas ce que l'on souhaite. On souhaite simplement donner le ksession dans les headers HTTP. Du
 * coup, ce filter crée une authentification factice afin d'entrer dans le authenticate() qui vérifie le ksession
 * 
 * @author qdeme
 *
 */
public class GouvPreAuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPreAuthFilter.class);

    private GouvPropertiesResolver propertiesResolver;

    public GouvPreAuthFilter(GouvPropertiesResolver propertiesResolver) {
        this.propertiesResolver = propertiesResolver;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à faire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        var context = SecurityContextHolder.getContext();
        String gouvSession = httpRequest.getParameter(LogonBean.GOUV_SESSION_REQUEST_PARAM);

        //On vérifie si GOUV_SESSION_REQUEST_PARAM n'est pas vide car ça se peut qu'une autre personne veuille se connecter sans déconnexion de la précédente au préalable
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()
                && StringUtils.isBlank(gouvSession)) {
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

            var logonBean = new LogonBean(gouvSession, appRoot, appId);

            // On se sert de UsernamePasswordAuthenticationToken pour arriver dans GouvAuthenticationProvider et
            // récupérer le token dans le password
            var auth = new LogonAuthenticationToken(logonBean, null);
            // Gestion de l'authentification via le provider (si à true, le système pense que l'utilisateur est admis
            auth.setAuthenticated(false);
            context.setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Rien à faire
    }

}
