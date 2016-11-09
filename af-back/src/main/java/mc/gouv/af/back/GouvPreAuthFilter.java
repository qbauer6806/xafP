package mc.gouv.af.back;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * Filter qui récupère le header HTTP "ksession" de Logon afin de créer un UsernamePasswordAuthenticationToken
 * contenant comme Credential un LogonBean contenant ce ksession.
 * Ce ksession sera en suite lu par GouvAuthenticationProvider.authenticate() pour appeler Logon et s'assurer
 * que cette session correspond bien à un utilisateur connecté.
 * 
 * @author qdeme
 *
 */
public class GouvPreAuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvPreAuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à faire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        SecurityContext context = SecurityContextHolder.getContext();

        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            // do nothing
        } else {

            String gouvSession = httpRequest.getParameter(LogonBean.GOUV_SESSION_REQUEST_PARAM);

            if (StringUtils.isBlank(gouvSession)) {
                LOGGER.error("Session Logon inexistante dans la requête");
                throw new BadCredentialsException("Invalid kSession");
            }

            String appRoot = httpRequest.getParameter(LogonBean.GOUV_APP_ROOT_REQUEST_PARAM);

            if (StringUtils.isBlank(appRoot)) {
                LOGGER.error("appRoot inexistant dans la requête");
                throw new BadCredentialsException("Invalid appRoot");
            }

            String appId = httpRequest.getParameter(LogonBean.GOUV_APP_ID_REQUEST_PARAM);

            if (StringUtils.isBlank(appId)) {
                LOGGER.error("appId inexistante dans la requête");
                throw new BadCredentialsException("Invalid appId");
            }

            LogonBean logonBean = new LogonBean(gouvSession, appRoot, appId);

            String name = "user";
            // On se sert de UsernamePasswordAuthenticationToken pour arriver dans GouvAuthenticationProvider et
            // récupérer le token dans le password
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(name, logonBean, null);
            // Gestion de l'authentification via le provider (si à true, le système pense que l'utilisateur est admis)
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
