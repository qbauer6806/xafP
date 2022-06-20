package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.apiclient.stc.StcApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import mc.gouv.xaf.shared.stc.utils.StcUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servlet permettant au Front de récupérer les données afin de générer le formulaire de paiement
 *
 * @author mboutelier.ext
 */
public class PaiementServlet extends AbstractAfServlet {


    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementServlet.class);
    private static final long serialVersionUID = -8411918728807352534L;

    protected StcApiClient getStcApiClient() {
        return new StcApiClient(AfServletGouvPropertiesResolver.getApiUrl(),
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
        String demandeIdStr = request.getParameter("demandesId");
        int demandeId = 0;
        try {
            if (StringUtils.isNotEmpty(demandeIdStr)) {
                demandeId = Integer.parseInt(demandeIdStr);
            }
        } catch (NumberFormatException e) {
            LOGGER.error("PaiementServlet - Impossible de parser le paramètre demandeId", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        String langue = request.getParameter(StcUtils.LANGUE_PARAM);

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        LOGGER.info("Récuppération des données de paiement pour la demande {}...", demandeId);
        PaiementDTO paiement = getStcApiClient().getPaiement(demandeId, langue, usagerId);

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


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /paiement doPost()");
        try {
            MoneticoPaiement_Ept oEpt = new MoneticoPaiement_Ept();
            MoneticoPaiement_Hmac oMac = new MoneticoPaiement_Hmac(oEpt);
            LOGGER.info("Check TPE");
            if (!request.getParameter("TPE").equals(oEpt.sNumero))
                throw mismatch("TPE", oEpt.sNumero, request.getParameter("TPE"));



            /*-----------------------------------------------------------------------*
             * Dynamic construction of data string for hmac
             *-----------------------------------------------------------------------*/
            StringBuilder sChaineMACBuilder = new StringBuilder();
            boolean premierElement = true;
            TreeMap<String, String[]> treeMap = new TreeMap<>(request.getParameterMap());
            for (Map.Entry<String, String[]> entry : treeMap.entrySet()) {
                if (!premierElement) {
                    sChaineMACBuilder.append("*");
                }
                if (!"MAC".equals(entry.getKey())) {
                    sChaineMACBuilder.append(entry.getKey());
                    sChaineMACBuilder.append("=");
                    sChaineMACBuilder.append(entry.getValue()[0]);
                    premierElement = false;
                }
            }

            String sChaineMAC = sChaineMACBuilder.toString();

            String MAC = (request.getParameter("MAC") != null) ? request.getParameter("MAC") : "";
            String codeRetour = (request.getParameter("code-retour") != null) ? request.getParameter("code-retour") : "";
            LOGGER.info("codeRetour : " + codeRetour);


            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();

            String sResult;
            LOGGER.info("oMac.isValidHmac(sChaineMAC, MAC) :" + oMac.isValidHmac(sChaineMAC, MAC));
            if (oMac.isValidHmac(sChaineMAC, MAC)) {
// =============================================================================================================================================================
// FIN SECTION CODE
//
// END CODE SECTION
// =============================================================================================================================================================

// =============================================================================================================================================================
// SECTION IMPLEMENTATION : Vous devez modifier ce code afin d'y mettre votre propre logique mÃ©tier
//
// IMPLEMENTATION SECTION : You must adapt this code with your own application logic.
// =============================================================================================================================================================
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode paiementNode = mapper.createObjectNode();
                for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                    paiementNode.put(entry.getKey().toLowerCase(), entry.getValue()[0]);
                }
                MoneticoPaiement paiement = mapper.treeToValue(paiementNode, MoneticoPaiement.class);

                getStcApiClient().updatePaiementStatus(paiement.getReference(), codeRetour);

// =============================================================================================================================================================
// FIN SECTION IMPLEMENTATION
//
// END IMPLEMENTATION SECTION
// =============================================================================================================================================================

// =============================================================================================================================================================
// SECTION CODE 2 : Cette section ne doit pas Ãªtre modifiÃ©e
//
// CODE SECTION 2 : This section must not be modified
// =============================================================================================================================================================
                LOGGER.info("result = 0");
                sResult = "0";
            } else {
                /*
                 * traitement en cas de HMAC incorrect
                 * your code if the HMAC doesn't match
                 */
                sResult = "1\n" + sChaineMAC;

            }

            /*-----------------------------------------------------------------------*
             * Acknowledgment message
             *-----------------------------------------------------------------------*/
            LOGGER.info("sResult = " + sResult);
            LOGGER.info("response = " + "version=2\ncdr=" + sResult);
            out.println("version=2\ncdr=" + sResult);
            out.close();
        } catch (Exception e) {

            LOGGER.error("Monetico Paiement failed.");
            LOGGER.error(e.getMessage());
            // Here you may use 'e' to get more information about the
            // incident, but be careful not showing it to the user since
            // it may contain confidential informations.
        }
        LOGGER.info("====================== Fin /paiement doPost()\n");
    }

    private ServletException mismatch(String field, String expected, String actual) {
        return new ServletException("Missmatching value for field " + field + " expected : " + expected + " actual : " + actual);
    }
}
