package mc.gouv.xaf.servlet;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet mettant à disposition le service /annulation avec la méthode POST, permettant
 * d'annuler une demande depuis le Front.
 *
 * @author qdeme
 */
public class AnnulationServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnulationServlet.class);

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== /annulation doDelete()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        // Récupération de l'ID de la demande à annuler
        String pathInfo = request.getPathInfo();
        String demandeId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathElems = pathInfo.split("/");
            demandeId = pathElems[1];
        }
        if (demandeId == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "DemandeID non spécifié");
            return;
        }
        int demandeIdParsed;
        try {
            demandeIdParsed = Integer.parseInt(demandeId);
        } catch (NumberFormatException e) {
            LOGGER.error("Problème lors du parsing du demandeId");
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Problème lors du parsing du demandeId");
            return;
        }

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("DemarcheID={}, UsagerID={}, DemandeID={}", demarcheId, usagerId, demandeId);

        LOGGER.info("Appel à la démarche...");
        AfApiClient afApiClient = getAfApiClient();

        // Vérification si l'usager à le droit d'annuler cette demande
        try {
            afApiClient.getDemande(usagerId, demandeIdParsed);
        } catch (Exception exception) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        // Annulation de la demande
        try {
            afApiClient.annulerDemande(Integer.parseInt(demandeId), usagerId);
            LOGGER.info("Retour au client...");
            response.setStatus(HttpStatus.SC_OK);
        } catch (Exception exception) {
            LOGGER.error("AnnulationServlet - Une erreur est survenue lors de l'appel à la méthode DELETE", exception);
            int codeErreur = getCodeErreur(exception);
            response.setStatus(codeErreur);
        }

        LOGGER.info("====================== Fin /annulation doDelete()");
    }
}
