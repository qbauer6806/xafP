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

/**
 * Pour que GouvAuthenticationProvider.authenticate() puisse être appelé et puisse donc analyser le header
 * "ksession" pour appeler Logon afin d'effectuer l'authentification, il faut d'abord fournir un user/mdp à Spring
 * en premier lieu... or ce n'est pas ce que l'on souhaite. On souhaite simplement donner le ksession dans les headers
 * HTTP. Du coup, ce filter crée une authentification factice afin d'entrer dans le authenticate() qui vérifie le ksession
 * 
 * @author qdeme
 *
 */
public class GouvPreAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à faire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // On passe par une MutableHttpServletRequest pour pouvoir modifier le comportement de HttpServletRequest
        // et retourner un header HTTP de plus
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest)request);
        if (StringUtils.isBlank(mutableRequest.getHeader("Authorization"))) {
            // Base 64 de 00000:00000 (matricule factice)
            mutableRequest.putHeader("Authorization", "Basic MDAwMDA6MDAwMDA=");
        }
        chain.doFilter(mutableRequest, response);
    }

    @Override
    public void destroy() {
        // Rien à faire
    }

}
