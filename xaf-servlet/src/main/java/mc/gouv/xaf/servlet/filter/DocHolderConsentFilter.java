package mc.gouv.xaf.servlet.filter;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.DocHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ttorreze.ext
 * Filtre permettant de vérifier si l'usager a consenti (côté TS) à l'utilisation du porte-documents.
 */
public class DocHolderConsentFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderConsentFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // ...
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(servletRequest);

        if (usagerInfosDTO != null && DocHolderUtils.isConsenting(usagerInfosDTO.getId())) {
            chain.doFilter(request, response);
        } else {
            LOGGER.info("L'usager n'a pas consenti et a appellé " + servletRequest.getRequestURI());
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {
        // ...
    }
}
