package mc.gouv.xaf.api.config.filter.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

/**
 * ServletFilter servant à intercepter le header d'override de méthode HTTP, afin de
 * pouvoir permettre à certains clients d'envoyer des PATCH via des méthodes POST
 * 
 * @author qdeme
 *
 */
public class HttpMethodFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMethodFilter.class);

    private static final String HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à faire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            String header = ((HttpServletRequest) request).getHeader(HTTP_METHOD_OVERRIDE_HEADER);
            if (header != null && HttpMethod.PATCH.name().equals(header)
                    && HttpMethod.POST.name().equals(((HttpServletRequest) request).getMethod())) {
                
                LOGGER.info("HttpMethodFilter: POST intercepté en tant que PATCH");
                // On ne peut pas modifier directement l'attribut "method" de la request courante, il faut
                // passer par un RequestWrapper que l'on donne au FilterChain
                ServletRequest requestModified = new HttpServletRequestWrapper((HttpServletRequest) request) {
                    @Override
                    public String getMethod() {
                        return HttpMethod.PATCH.name();
                    }
                };
                chain.doFilter(requestModified, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Rien à faire
    }

}
