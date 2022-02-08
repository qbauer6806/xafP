package mc.gouv.xaf.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Filtre permettant de bloquer la requête si les IP Remote et Local ne sont pas les mêmes
 * 
 * @author qdeme
 * 
 */
public class IPFilter implements Filter {

    private static Logger LOGGER = LoggerFactory.getLogger(IPFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!request.getRemoteAddr().equals(request.getLocalAddr())) {
            // Si les adresses IP Remote et Local ne sont pas les mêmes, alors on ne continue pas
            LOGGER.info("Adresse IP incorrecte. Local=" + request.getLocalAddr() + ", Remote=" + request.getRemoteAddr());
            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse)response).setStatus(HttpStatus.SC_UNAUTHORIZED);
            }
        }
        else {
            // Si les adresses IP sont les mêmes, alors on continue
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

}
