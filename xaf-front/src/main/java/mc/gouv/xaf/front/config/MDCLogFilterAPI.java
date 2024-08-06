package mc.gouv.xaf.front.config;

import org.slf4j.MDC;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author qdeme
 */
public class MDCLogFilterAPI implements Filter {

    private static final String JSESSIONID_KEY = "JSESSIONID";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à faire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        boolean successfulSessionRegistration = false;

        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(JSESSIONID_KEY)) {
                    MDC.put(JSESSIONID_KEY, c.getValue());
                    successfulSessionRegistration = true;
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (successfulSessionRegistration) {
                MDC.remove(JSESSIONID_KEY);
            }
        }
    }

    @Override
    public void destroy() {
        // Rien à faire
    }

}
