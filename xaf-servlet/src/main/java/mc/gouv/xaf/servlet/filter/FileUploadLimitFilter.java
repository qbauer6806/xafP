package mc.gouv.xaf.servlet.filter;

import mc.gouv.xaf.servlet.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.FileServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileUploadLimitFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadLimitFilter.class);
    private static final Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = new HashMap<>();
    private static int compteurCleanSessions;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        if (servletRequest.getMethod().equalsIgnoreCase(HttpMethod.POST) || servletRequest.getMethod().equalsIgnoreCase(HttpMethod.PATCH)) {
            LOGGER.info("Vérification du nombre de fichiers déjà uploadés...");
            HttpSession session = servletRequest.getSession();
            if (FileServletUtils.limiteUploadAtteinte(usagersFileUploadCompteurs, session)) {
                AppFactoryServletUtils.logAndSendError(LOGGER, servletResponse, HttpStatus.SC_TOO_MANY_REQUESTS, SharedMessages.FICHIER_LIMITE_UPLOAD_ATTEINTE);
                return;
            }

            LOGGER.info(usagersFileUploadCompteurs.toString());
            LOGGER.info("compteurCleanSessions = {}", compteurCleanSessions);

            chain.doFilter(request, response);

            // Supression des sessions inutilisées chaque 50 requêtes d'upload
            if (compteurCleanSessions > 50) {
                FileServletUtils.reinitialierSessionsInutilisees(usagersFileUploadCompteurs);
                compteurCleanSessions = 0;
            }

            // Ajout dans l'historique par session
            FileServletUtils.ajouterCompteurUpload(usagersFileUploadCompteurs, session);
            compteurCleanSessions++;
        } else {
            chain.doFilter(request, response);
        }
    }

}
