package mc.gouv.xaf.front.filter;

import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.FrontControllerPropertiesCache;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtre de servlet pour les fonctionnalités du porte-document (doc-holder)
 * Il fait les vérifications suivantes :
 * <ul>
 *     <li>La fonctionnalité est-elle activée (isDocHolderEnabled)</li>
 *     <li>L'usager est-il-connecté ?</li>
 * </ul>
 */
@Component
public class DocHolderFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFilter.class);
    private static final String XAF_PORTE_DOCUMENT_ACTIF = "XAF_PORTE_DOCUMENT_ACTIF";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontControllerPropertiesCache propertiesCache;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.contains("/doc-holder");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest servletRequest,
                                    @NonNull HttpServletResponse servletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {
        PropertiesDTO docHolderEnabled = propertiesCache.getFrontProperty(XAF_PORTE_DOCUMENT_ACTIF);
        if (docHolderEnabled == null) {
            LOGGER.error("La propriété obligatoire " + XAF_PORTE_DOCUMENT_ACTIF + " ne semble pas définie");
            servletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return;
        }

        boolean isDocHolderEnabled = Boolean.parseBoolean(docHolderEnabled.getValue());
        if (isDocHolderEnabled) {
            LOGGER.info("Vérification usager connecté.");
            UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(servletRequest);
            if (usagerInfosDTO == null || usagerInfosDTO.getTokenInfo() == null) {
                LOGGER.info(SharedMessages.UTILISATEUR_NON_AUTORISE);
                servletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());

            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            LOGGER.info("Appel alors que le porte document est désactivé.");
            servletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }
}
