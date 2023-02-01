package mc.gouv.xaf.servlet;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet permettant de rediriger l'usager sur le Back-Office
 *
 * @author qdeme
 */
public class RedirectToBackOfficeServlet extends AbstractAfServlet {

    /**
     *
     */
    private static final long serialVersionUID = -9158173804027496532L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectToBackOfficeServlet.class);

    private static final String TOKEN_ID_DEMANDE = "<id>";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /redirect-to-backoffice doGet()");

        try {
            UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
            if (usagerInfosDTO == null) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                        "Utilisateur non autorisé");
                return;
            }

            if (!usagerInfosDTO.isUsagerCourrier()) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                        "Utilisateur non autorisé car non usager courrier");
                return;
            }

            //redirection par default sur l'accueil car le lien abandon a été cliqué
            String urlDemande = AfServletGouvPropertiesResolver.getBackOfficeUrl();
            //dans le cas de la fin de la création
            String idDemandeStr = request.getParameter("id");

            if (StringUtils.isNotBlank(idDemandeStr)) {
                int idDemande = Integer.parseInt(idDemandeStr);
                urlDemande = AfServletGouvPropertiesResolver.getBackOfficeDemandeUrl();
                urlDemande = StringUtils.replace(urlDemande, TOKEN_ID_DEMANDE, idDemande + "");
            }

            response.sendRedirect(urlDemande);
            LOGGER.info("Redirection vers : {}", urlDemande);
        } catch (Exception e) {
            LOGGER.error("RedirectToBackOfficeServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            int codeStatut = getCodeErreur(e);
            response.setStatus(codeStatut);
        }

        LOGGER.info("====================== Fin /redirect-to-backoffice doGet()");
    }
}
