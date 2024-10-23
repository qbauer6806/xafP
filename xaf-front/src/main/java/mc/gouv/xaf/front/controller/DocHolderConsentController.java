package mc.gouv.xaf.front.controller;

import static mc.gouv.xaf.front.util.DocHolderUtils.CONSENTING_NODE;
import static mc.gouv.xaf.front.util.DocHolderUtils.DATE_CREATION_NODE;
import static mc.gouv.xaf.front.util.DocHolderUtils.DOCHOLDER_CONSENT_NODE;
import static mc.gouv.xaf.front.util.DocHolderUtils.JSON_DATE_FORMAT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import mc.gouv.xaf.front.dto.DocHolderConsentDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder/consent")
public class DocHolderConsentController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderConsentController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    /**
     * Renvoie si l'utilisateur connecté a consenti ou non <b>côté TS</b> à l'usage du porte-documents
     * <b>Retour :</b>
     * <ul>
     *     <li><em>204</em> si l'utilisateur a consenti</li>
     *     <li><em>404</em> si l'utilisateur n'a pas consenti, ou si il n'y a pas de champ "docholderConsent" dans le contenu des données d'accès</li>
     * </ul>
     */
    @GetMapping
    protected ResponseEntity doGet(HttpServletRequest req) throws IOException {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info("Récupération des données d'accès");
        AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
        if (access == null) {
            LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    SharedMessages.ERREUR_INTERNE);
        }

        LOGGER.info("Déserialisation des données d'accès");
        JsonNode docholderConsentNode = access.getContenu().get(DOCHOLDER_CONSENT_NODE);
        try {
            if (docholderConsentNode == null) {
                LOGGER.info("Aucun noeud json " + DOCHOLDER_CONSENT_NODE + " trouvé");
                return ResponseEntity.notFound().build();
            } else {
                DocHolderConsentDTO docholderConsent = mapper.readValue(docholderConsentNode.toString(),
                        DocHolderConsentDTO.class);
                if (docholderConsent == null) {
                    return ResponseEntity.notFound().build();
                } else {
                    LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
                    return ResponseEntity.status(docholderConsent.isConsenting() ? 204 : 404).build();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la déserialisation de la requête", e);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    SharedMessages.ERREUR_INTERNE);
        }

    }

    /**
     * Créé la valeur du consentement <b>côté TS</b> au porte-document de l'usager connecté
     * <b>Retour :</b>
     * <ul>
     *     <li><em>204</em> si la valeur du consentement a été créé</li>
     * </ul>
     */
    @PostMapping
    protected ResponseEntity doPost(HttpServletRequest req) {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info("Récupération des données d'accès");
        AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
        if (access == null) {
            LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    SharedMessages.ERREUR_INTERNE);
        }

        LOGGER.info("Déserialisation des données d'accès");
        JsonNode docholderConsentNode = access.getContenu().get(DOCHOLDER_CONSENT_NODE);

        // L'usager n'a jamais consenti, on ajoute l'objet de consentement
        if (docholderConsentNode == null) {
            LOGGER.info("Création du consentement dans les données d'accès");
            DocHolderConsentDTO docholderConsent = new DocHolderConsentDTO();
            docholderConsent.setConsenting(true);
            docholderConsent.setDateCreation(Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).toInstant()));

            AccessInputDTO accessInputDTO = new AccessInputDTO();
            JsonNode accessInputNode = ((ObjectNode) access.getContenu()).putPOJO(DOCHOLDER_CONSENT_NODE,
                    docholderConsent);
            accessInputDTO.setContenu(accessInputNode);

            LOGGER.info("Mise à jour des données d'accès");
            getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInputDTO);

            LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
            return ResponseEntity.status(204).build();
        } else { // L'usager avait déjà consenti
            LOGGER.info("Modification du consentement dans les données d'accès");
            AccessInputDTO accessInputDTO = new AccessInputDTO();

            ((ObjectNode) access.getContenu().findPath(DOCHOLDER_CONSENT_NODE)).put(CONSENTING_NODE, true)
                    .put(DATE_CREATION_NODE, new SimpleDateFormat(JSON_DATE_FORMAT).format(
                            Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).toInstant())));

            accessInputDTO.setContenu(access.getContenu());

            LOGGER.info("Mise à jour des données d'accès");
            getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInputDTO);

            LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
            return ResponseEntity.status(204).build();
        }
    }
}
