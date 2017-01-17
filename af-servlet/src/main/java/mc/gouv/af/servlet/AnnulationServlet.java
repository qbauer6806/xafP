package mc.gouv.af.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeStatutEnum;
import mc.gouv.dem.apishared.model.StatutInputDTO;

/**
 * Servlet mettant à disposition le service /annulation avec la méthode POST, permettant
 * d'annuler une demande depuis le Front.
 * 
 * @author qdeme
 *
 */
public class AnnulationServlet extends HttpServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(AnnulationServlet.class);
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        LOGGER.info("====================== /annulation doDelete()");
        
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED, "Utilisateur non autorisé");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        String demandeId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathElems = pathInfo.split("/");
            demandeId = pathElems[1];
        }
        
        if (demandeId == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "DemandeID non spécifié");
            return;
        }
        
        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        
        String codeMotifAnnulation = getServletContext().getInitParameter(AppFactoryServletUtils.CODE_MOTIF_ANNULATION_KEY);
        
        Integer usagerId = usagerInfosDTO.getId();
        
        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId + ", DemandeID=" + demandeId);
        
        LOGGER.info("Appel à DEM...");
        StatutInputDTO statutInput = new StatutInputDTO();
        statutInput.setUsagerId(usagerId);
        statutInput.setStatut(DemandeStatutEnum.ANNULEE);
        statutInput.setCodeMotif(codeMotifAnnulation);
        DemClient demClient = new DemClient(AppFactoryServletUtils.DEM_URL, AppFactoryServletUtils.DEMARCHES_USER,AppFactoryServletUtils.DEMARCHES_PWD);
        
        demClient.changerStatutDemande(demarcheId, Integer.parseInt(demandeId), statutInput);
        
        LOGGER.info("Retour au client...");
        
        // TODO Gestion des erreurs ?
        response.setStatus(HttpStatus.SC_OK);
        
        LOGGER.info("====================== Fin /annulation doDelete()");
    }
}
