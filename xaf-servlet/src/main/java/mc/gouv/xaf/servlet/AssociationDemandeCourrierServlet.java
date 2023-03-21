package mc.gouv.xaf.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

/**
 * Servlet permettant d'associer une demande courrier à un usager téléservice.
 *
 * @author qdeme
 */
public class AssociationDemandeCourrierServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -5171815930561560391L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AssociationDemandeCourrierServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== /associerDemandeCourrier doPost()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        String identifiant = request.getParameter("identifiant");
        String nomProprio = request.getParameter("nomProprio");

        if (StringUtils.isBlank(identifiant)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Identifiant de la demande non spécifié");
            return;
        }
        if (StringUtils.isBlank(nomProprio)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Nom du propriétaire non spécifié");
            return;
        }

        try {
            // Récupération de l'ID de la démarche dans le Context-Param
            String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

            Integer usagerId = usagerInfosDTO.getId();

            LOGGER.info("DemarcheID={}, UsagerID={}, IdentifiantDemande={}, NomProprio={}", demarcheId, usagerId, identifiant, nomProprio);

            LOGGER.info("Appel à la démarche...");

            AfApiClient afApiClient = getAfApiClient();

            afApiClient.associerDemandeCourrier(identifiant, nomProprio, usagerId);
            response.setStatus(HttpStatus.SC_OK);
            LOGGER.info("Retour au client...");
        } catch (Exception exception) {
            LOGGER.error("AssociationDemandeCourrierServlet - Une erreur est survenue lors de l'appel à la méthode POST",
                    exception);
            int codeStatut = getCodeErreur(exception);
            response.setStatus(codeStatut);
        }

        LOGGER.info("====================== Fin /associerDemandeCourrier doPost()");
    }
}
