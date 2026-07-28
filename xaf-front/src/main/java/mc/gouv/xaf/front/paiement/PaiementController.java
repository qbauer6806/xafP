package mc.gouv.xaf.front.paiement;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Servlet permettant à Monetico d'enregistrer un paiement
 *
 * @author mboutelier.ext
 */
@Controller
@RequiredArgsConstructor
public class PaiementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementController.class);

    private final XafFrontserverUtils xafFrontserverUtils;

    /**
     * Interface Retour
     */
    @PostMapping(value = { "/paiement" })
    public ResponseEntity<String> processPaiement(HttpServletRequest request) {
        LOGGER.info("====================== /paiement doPost()");
        return processPaiementMonetico(request);
    }

    private ResponseEntity<String> processPaiementMonetico(HttpServletRequest request) {
        try {
            LOGGER.info("Vérification de la présence de la clé MAC...");
            if (request.getParameter("MAC") == null) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                        "Il manque le paramètre de clé MAC");
            }

            LOGGER.info("Vérification de la présence du code-retour...");
            if (request.getParameter("code-retour") == null) {
                return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                        "Il manque le paramètre de code-retour");
            }

            String codeRetour = request.getParameter("code-retour");
            String safeCodeRetour = codeRetour.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
            LOGGER.info("codeRetour : {}", safeCodeRetour);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode paiementNode = mapper.createObjectNode();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue()[0];
                paiementNode.put(key, value);
                String safeKey = key.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
                String safeValue = value != null ? value.replaceAll(SharedMessages.UNSAFE_CHARS, "_") : null;
                LOGGER.info("{}={}", safeKey, safeValue);
            }
            MoneticoResponseDTO moneticoResponseDTO = mapper.treeToValue(paiementNode, MoneticoResponseDTO.class);
            moneticoResponseDTO.setCodeRetour(codeRetour);
            String texteLibre = request.getParameter("texte-libre");
            if (StringUtils.isNotEmpty(texteLibre)) {
                moneticoResponseDTO.setTexteLibre(texteLibre);
            }
            String sResult = xafFrontserverUtils.getMoneticoApiClient().updatePaiementStatus(moneticoResponseDTO);
            LOGGER.info("sResult = {}", sResult);
            LOGGER.info("response = version=2\ncdr={}", sResult);
            LOGGER.info("====================== Fin /paiement doPost()\n");

            return ResponseEntity.ok().header("Pragma", "no-cache").header("Cache-Control", "no-cache")
                    .contentType(MediaType.TEXT_PLAIN).body("version=2\ncdr=" + sResult);
        } catch (Exception e) {
            LOGGER.error("La mise à jour du Paiement Monetico à échouée.", e);
            return ResponseEntity.internalServerError().build();
        }
    }




}
