package mc.gouv.xaf.front.filter;

import mc.gouv.xaf.front.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.front.util.FileControllerUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.http.HttpStatus;
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
import javax.servlet.http.HttpSession;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileUploadLimitFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadLimitFilter.class);
    private static final Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = new HashMap<>();
    private static int compteurCleanSessions;

    @Autowired
    private FileControllerUtils fileControllerUtils;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/doc-holder/file");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest servletRequest,
                                    @NonNull HttpServletResponse servletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (servletRequest.getMethod().equalsIgnoreCase(HttpMethod.POST) || servletRequest.getMethod().equalsIgnoreCase(HttpMethod.PATCH)) {
            LOGGER.info("Vérification du nombre de fichiers déjà uploadés...");
            HttpSession session = servletRequest.getSession();
            if (fileControllerUtils.limiteUploadAtteinte(usagersFileUploadCompteurs, session)) {
                LOGGER.error(SharedMessages.FICHIER_LIMITE_UPLOAD_ATTEINTE);
                servletResponse.setStatus(HttpStatus.SC_TOO_MANY_REQUESTS);
                return;
            }

            LOGGER.info("usagersFileUploadCompteurs = {}", usagersFileUploadCompteurs);
            LOGGER.info("compteurCleanSessions = {}", compteurCleanSessions);

            filterChain.doFilter(servletRequest, servletResponse);

            // Supression des sessions inutilisées chaque 50 requêtes d'upload
            if (compteurCleanSessions > 50) {
                fileControllerUtils.reinitialierSessionsInutilisees(usagersFileUploadCompteurs);
                compteurCleanSessions = 0;
            }

            // Ajout dans l'historique par session
            fileControllerUtils.ajouterCompteurUpload(usagersFileUploadCompteurs, session);
            compteurCleanSessions++;
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

}
