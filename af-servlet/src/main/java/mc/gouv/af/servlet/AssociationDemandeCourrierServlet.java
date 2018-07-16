package mc.gouv.af.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.af.apiclient.AfApiClient;
import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;

/**
 * Servlet permettant d'associer une demande courrier à un usager téléservice.
 * 
 * @author qdeme
 *
 */
public class AssociationDemandeCourrierServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -5171815930561560391L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(AssociationDemandeCourrierServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        LOGGER.info("====================== /associerDemandeCourrier doPost()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        String identifiant = request.getParameter("identifiant");
        String nomProprio = request.getParameter("nomProprio");

        if (StringUtils.isBlank(identifiant)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Identifiant de la demande non spécifié");
            return;
        }
        if (StringUtils.isBlank(nomProprio)) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Nom du propriétaire non spécifié");
            return;
        }

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

        Integer usagerId = usagerInfosDTO.getId();

        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId + ", IdentifiantDemande=" + identifiant + ", NomProprio=" + nomProprio);

        LOGGER.info("Appel à la démarche...");

        AfApiClient afApiClient = getAfApiClient();
                
        afApiClient.associerDemandeCourrier(identifiant, nomProprio, usagerId);

        LOGGER.info("Retour au client...");

        LOGGER.info("====================== Fin /associerDemandeCourrier doPost()");
    }
}
