package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.servlet.dto.DocHolderConsentDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static mc.gouv.xaf.servlet.util.DocHolderUtils.*;

public class DocHolderConsentServlet extends AbstractAfServlet {
    private static final long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderSearchServlet.class);

    /**
     * Renvoie si l'utilisateur connecté a consenti ou non <b>côté TS</b> à l'usage du porte-documents
     * <b>Retour :</b>
     * <ul>
     *     <li><em>204</em> si l'utilisateur a consenti</li>
     *     <li><em>404</em> si l'utilisateur n'a pas consenti, ou si il n'y a pas de champ "docholderConsent" dans le contenu des données d'accès</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doGet()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Récupération des données d'accès");
        AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
        if (access == null) {
            LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
            return;
        }

        LOGGER.info("Déserialisation des données d'accès");
        JsonNode docholderConsentNode = access.getContenu().get(DOCHOLDER_CONSENT_NODE);
        try {
            if (docholderConsentNode == null) {
                LOGGER.info("Aucun noeud json " + DOCHOLDER_CONSENT_NODE + " trouvé");
                resp.setStatus(404);
            } else {
                DocHolderConsentDTO docholderConsent = mapper.readValue(docholderConsentNode.toString(), DocHolderConsentDTO.class);
                if (docholderConsent == null) {
                    resp.setStatus(404);
                } else {
                    resp.setStatus(docholderConsent.isConsenting() ? 204 : 404);
                }
            }
        } catch (IOException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
            LOGGER.error("Erreur lors de la déserialisation de la requête", e);
            return;
        }

        LOGGER.info("====================== Fin {} doGet()", req.getServletPath());
    }

    /**
     * Définis la valeur du consentement <b>côté TS</b> au porte-document de l'usager connecté
     * <b>Entrée :</b> exemple : { "consenting": true}
     * <b>Retour :</b>
     * <ul>
     *     <li><em>204</em> si la valeur du consentement a changé</li>
     *     <li><em>304</em> si la valeur du consentement n'a pas changé</li>
     * </ul>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LOGGER.info("Vérification usager connecté");
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        LOGGER.info("Déserialisation des données de la requête");
        DocHolderConsentDTO requestConsentDTO;
        try {
            requestConsentDTO = mapper.readValue(req.getInputStream(), DocHolderConsentDTO.class);
            if (requestConsentDTO == null) {
                LOGGER.error("Erreur lors de la déserialisation de la requête, mapper.readValue renvoi null");
                AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
                return;
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la déserialisation de la requête", e);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
            return;
        }

        LOGGER.info("Récupération des données d'accès");
        AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
        if (access == null) {
            LOGGER.error("Impossible de récupérer l'AccessDTO pour l'utilisateur id {}", usagerInfosDTO.getId());
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
            return;
        }

        LOGGER.info("Déserialisation des données d'accès");
        try {
            JsonNode docholderConsentNode = access.getContenu().get(DOCHOLDER_CONSENT_NODE);

            // L'usager n'a jamais consenti, on ajoute l'objet de consentement
            if (docholderConsentNode == null) {
                LOGGER.info("Création du consentement dans les données d'accès");
                DocHolderConsentDTO docholderConsent = new DocHolderConsentDTO();
                docholderConsent.setConsenting(requestConsentDTO.isConsenting());
                docholderConsent.setDateCreation(Date.from(Instant.now()));

                AccessInputDTO accessInputDTO = new AccessInputDTO();
                JsonNode accessInputNode = ((ObjectNode) access.getContenu()).putPOJO(DOCHOLDER_CONSENT_NODE, docholderConsent);
                accessInputDTO.setContenu(accessInputNode);

                LOGGER.info("Mise à jour des données d'accès");
                getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInputDTO);

                resp.setStatus(204);
            } else { // L'usager avait déjà consenti
                JsonNode accessContent = mapper.readValue(docholderConsentNode.toString(), JsonNode.class);
                boolean docholderConsent = accessContent.findPath(CONSENTING_NODE).asBoolean();

                // Le consentement n'a pas été modifié
                if (requestConsentDTO.isConsenting() == docholderConsent) {
                    LOGGER.info("Aucun changement de consentement");
                    resp.setStatus(304);

                } else { // Le consentement a été modifié
                    LOGGER.info("Modification du consentement dans les données d'accès");
                    AccessInputDTO accessInputDTO = new AccessInputDTO();

                    ((ObjectNode) access.getContenu().findPath(DOCHOLDER_CONSENT_NODE))
                            .put(CONSENTING_NODE, requestConsentDTO.isConsenting())
                            .put(DATE_CREATION_NODE, new SimpleDateFormat(JSON_DATE_FORMAT).format(Date.from(Instant.now())));

                    accessInputDTO.setContenu(access.getContenu());

                    LOGGER.info("Mise à jour des données d'accès");
                    getAfApiClient().createOrUpdateAccess(usagerInfosDTO.getId(), accessInputDTO);

                    resp.setStatus(204);
                }
            }
        } catch (IOException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_INTERNAL_SERVER_ERROR, SharedMessages.ERREUR_INTERNE);
            LOGGER.error("Erreur lors de la déserialisation de la requête", e);
            return;
        }


        LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
    }
}
