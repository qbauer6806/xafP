package mc.gouv.xaf.servlet.filter;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtre de servlet pour les fonctionnalités du porte-document (doc-holder)
 * Il fait les vérifications suivantes :
 * <ul>
 *     <li>La fonctionnalité est-elle activée (isDocHolderEnabled)</li>
 *     <li>L'usager est-il-connecté ?</li>
 * </ul>
 */
public class DocHolderFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFilter.class);
    private boolean isDocHolderEnabled;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        isDocHolderEnabled = Boolean.parseBoolean(AfServletGouvPropertiesResolver.isPorteDocEnabled());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        if (isDocHolderEnabled) {
            LOGGER.info("Vérification usager connecté.");
            UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(servletRequest);
            if (usagerInfosDTO == null || usagerInfosDTO.getTokenInfo() == null) {
                LOGGER.info("L'usager n'est pas connecté");
                AppFactoryServletUtils.logAndSendError(LOGGER, servletResponse, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            LOGGER.info("Appel alors que le porte document est désactivé.");
            servletResponse.setStatus(HttpStatus.SC_FORBIDDEN);
        }
    }

    @Override
    public void destroy() {
        /* (Methods should not be empty java:S1186)
         * N'oubliez pas de détruire toutes ressources créés dans init ici !
         */
    }
}
