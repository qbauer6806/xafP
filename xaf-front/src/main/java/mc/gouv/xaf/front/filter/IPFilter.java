package mc.gouv.xaf.front.filter;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * Filtre permettant de bloquer la requête si les IP Remote et Local ne sont pas les mêmes
 * 
 * @author qdeme
 * 
 */
@Component
public class IPFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    	// Vide pour l'instant
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!request.getRemoteAddr().equals(request.getLocalAddr())) {
            // Si les adresses IP Remote et Local ne sont pas les mêmes, alors on ne continue pas
            LOGGER.info("Adresse IP incorrecte. Local={}, Remote={}", request.getLocalAddr(), request.getRemoteAddr());
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse)response).setStatus(HttpStatus.SC_UNAUTHORIZED);
            }
        } else {
            // Si les adresses IP sont les mêmes, alors on continue
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    	// Vide pour l'instant
    }

}
