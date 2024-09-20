package mc.gouv.xaf.front.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class NoCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    )
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // pour certaines versions d'IE, no-cache veut dire no-store et donc si on
        // l'utilise, on n'aura pas de cache des CSS/IMG non plus.
        // Par contre, le Pragma HTTP 1.0 en no-cache ne pose pas de soucis et est
        // équivalent à must-revalidate plus ou moins.
        response.setHeader("Cache-Control", "must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {}
}
