package mc.gouv.xaf.api.config.filter.jwt;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import mc.gouv.xaf.back.config.utils.XafSpringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Filter pour l'authorisation via JWT
 * @author fgaujous
 */
public class JwtAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO document why this method is empty
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String authorization = servletRequest.getHeader("Authorization");
        if (authorization != null) {
            var token = new JwtAuthToken(authorization.replace(XafSpringUtils.JWT_PREFIX, ""));
            //Nous n'avons pas encore vérifié le token
            token.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(token);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // TODO document why this method is empty
    }

}
