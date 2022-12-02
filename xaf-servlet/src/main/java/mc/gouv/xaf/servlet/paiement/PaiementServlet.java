package mc.gouv.xaf.servlet.paiement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.apiclient.paiement.PaiementApiClient;
import mc.gouv.xaf.servlet.AbstractAfServlet;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet permettant à Monetico d'enregistrer un paiement
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
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue()[0];
                paiementNode.put(key, value);
                LOGGER.info("{}={}", key, value);
            }
            MoneticoResponseDTO moneticoResponseDTO = mapper.treeToValue(paiementNode, MoneticoResponseDTO.class);
            moneticoResponseDTO.setCodeRetour(codeRetour);
            String texteLibre = request.getParameter("texte-libre");
            if (StringUtils.isNotEmpty(texteLibre)) {
                moneticoResponseDTO.setTexteLibre(texteLibre);
            }
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
