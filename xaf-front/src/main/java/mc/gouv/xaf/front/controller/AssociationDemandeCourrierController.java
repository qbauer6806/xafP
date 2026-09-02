package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Servlet permettant d'associer une demande courrier à un usager téléservice.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/associerDemandeCourrier")
@RequiredArgsConstructor
public class AssociationDemandeCourrierController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssociationDemandeCourrierController.class);

    private final XafFrontserverUtils xafFrontserverUtils;

    @PostMapping
    public ResponseEntity doPost(HttpServletRequest request) {

        LOGGER.info("====================== /associerDemandeCourrier doPost()");

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        String identifiant = request.getParameter("identifiant");
        if (StringUtils.isBlank(identifiant)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_BAD_REQUEST,
                    "Identifiant de la demande non spécifié");
        }

        String nomProprio = request.getParameter("nomProprio");
        if (StringUtils.isBlank(nomProprio)) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.SC_BAD_REQUEST,
                    "Nom du propriétaire non spécifié");
        }

        try {
            Integer usagerId = usagerInfosDTO.getId();
            String safeIdentifiant = identifiant.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
            String safeNomProprio = nomProprio.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
            LOGGER.info("UsagerID={}, IdentifiantDemande={}, NomProprio={}", usagerId, safeIdentifiant, safeNomProprio);
            LOGGER.info("Appel à la démarche...");
            AfApiClient afApiClient = xafFrontserverUtils.getAfApiClient();
            afApiClient.associerDemandeCourrier(identifiant, nomProprio, usagerId);
            LOGGER.info("Retour au client...");
            LOGGER.info("====================== Fin /associerDemandeCourrier doPost()");

            return ResponseEntity.ok().build();
        } catch (HttpStatusCodeException exception) {
            LOGGER.error("AssociationDemandeCourrierController - Erreur fonctionnelle lors de l'appel POST", exception);
            return ResponseEntity.status(
                            xafFrontserverUtils.getCodeErreurHttpClient(
                                    exception,
                                    HttpStatus.SC_BAD_REQUEST,
                                    HttpStatus.SC_NOT_FOUND))
                    .build();
        } catch (Exception exception) {
            LOGGER.error(
                    "AssociationDemandeCourrierController - Une erreur est survenue lors de l'appel POST",
                    exception);
            return ResponseEntity.status(xafFrontserverUtils.getCodeErreur(exception)).build();
        }
    }
}
