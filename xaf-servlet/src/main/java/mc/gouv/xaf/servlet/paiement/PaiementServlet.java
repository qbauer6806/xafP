package mc.gouv.xaf.servlet.paiement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.apiclient.paiement.PaiementApiClient;
import mc.gouv.xaf.apiclient.paiement.PaiementConstant;
import mc.gouv.xaf.apiclient.paiement.monetico.dto.MoneticoDTO;
import mc.gouv.xaf.servlet.AbstractAfServlet;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet permettant au Front de récupérer les données afin de générer le formulaire de paiement
 *
 * @author mboutelier.ext
 */
public class PaiementServlet extends AbstractAfServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementServlet.class);
    private static final long serialVersionUID = -8411918728807352534L;

    protected PaiementApiClient getStcApiClient() {
        return new PaiementApiClient(AfServletGouvPropertiesResolver.getApiUrl(),
                AfServletGouvPropertiesResolver.getApiJwt());
    }

    /**
     * Interface Aller
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /paiement doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        LOGGER.info("Récupération des paramètres...");
        String demandeIds = request.getParameter(PaiementConstant.DEMANDES_ID_PARAM);

        String langue = request.getParameter(PaiementConstant.LANGUE_PARAM);
        boolean iframe = Boolean.parseBoolean(request.getParameter(PaiementConstant.IFRAME_PARAM));


        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        LOGGER.info("Récupération des données de paiement pour la demande {}...", demandeIds);
        MoneticoDTO paiement = getStcApiClient().getPaiement(demandeIds, langue, usagerId, iframe);

        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();

        try {
            String repJson = mapper.writeValueAsString(paiement);
            response.setContentType("application/json");
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        } catch (Exception e) {
            LOGGER.error("PaiementServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /paiement doGet()\n");
    }

    /**
     * Interface Retour
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /paiement doPost()");
        try {
            LOGGER.info("Vérification de la présence de la clé MAC..." );
            if (request.getParameter("MAC") == null) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                        "Il manque le paramètre de clé MAC");
                return;
            }

            LOGGER.info("Vérification de la présence du code-retour..." );
            if (request.getParameter("code-retour") == null) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                        "Il manque le paramètre de code-retour");
                return;
            }

            String codeRetour = request.getParameter("code-retour");
            LOGGER.info("codeRetour : {}", codeRetour);

            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode paiementNode = mapper.createObjectNode();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                paiementNode.put(entry.getKey().toLowerCase(), entry.getValue()[0]);
            }
            MoneticoResponseDTO moneticoResponseDTO = mapper.treeToValue(paiementNode, MoneticoResponseDTO.class);
            moneticoResponseDTO.setCodeRetour(codeRetour);
            String sResult = getStcApiClient().updatePaiementStatus(moneticoResponseDTO);
            LOGGER.info("sResult = {}", sResult);
            LOGGER.info("response = version=2\ncdr={}", sResult);
            out.println("version=2\ncdr=" + sResult);
            out.close();
        } catch (Exception e) {
            LOGGER.error("La mise à jour du Paiement Monetico à échouée.", e);
        }
        LOGGER.info("====================== Fin /paiement doPost()\n");
    }

}
