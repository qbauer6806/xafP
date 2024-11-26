package mc.gouv.xaf.front.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.MDC;

/**
 * @author qdeme
 */
public class MDCLogFilterAPI implements Filter {

    private static final String JSESSIONID_KEY = "JSESSIONID";

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

}
