package mc.gouv.xaf.front.filter;

import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.DocHolderUtils;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ttorreze.ext
 * Filtre permettant de vérifier si l'usager a consenti (côté TS) à l'utilisation du porte-documents.
 */
@Component
public class DocHolderConsentFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderConsentFilter.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private DocHolderUtils docHolderUtils;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/doc-holder/file");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest servletRequest,
                                    @NonNull HttpServletResponse servletResponse, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(servletRequest);

        if (usagerInfosDTO != null && docHolderUtils.isConsenting(usagerInfosDTO.getId())) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            LOGGER.info("L'usager n'a pas consenti et a appellé {}", servletRequest.getRequestURI());
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
