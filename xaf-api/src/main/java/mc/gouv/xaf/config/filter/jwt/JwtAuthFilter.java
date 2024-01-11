package mc.gouv.xaf.config.filter.jwt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
