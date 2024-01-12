package mc.gouv.xaf.front.config;

import org.slf4j.MDC;

import javax.servlet.FilterConfig;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author qdeme
 */
public class MDCLogFilterAPI implements Filter {

    private static final String USER_KEY = "USER";
    private static final String JSESSIONID_KEY = "JSESSIONID";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à faire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        boolean successfulUserRegistration = false;
        boolean successfulSessionRegistration = false;

        // Dans le cas de la servlet monitor il n'y a pas d'authentification
//        if (SecurityContextHolder.getContext().getAuthentication() != null) {
//            String user = SecurityContextHolder.getContext().getAuthentication().getName();
//
//            if (!StringUtils.isBlank(user)) {
//                MDC.put(USER_KEY, user);
//                successfulUserRegistration = true;
//            }
//        }

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
            if (successfulUserRegistration) {
                MDC.remove(USER_KEY);
            }
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
