package mc.gouv.candifp.frontserver.movetoxaf.controller;

import mc.gouv.candifp.frontserver.movetoxaf.dto.UsagerInfosDTO;
import mc.gouv.candifp.frontserver.movetoxaf.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet permettant d'associer une demande courrier à un usager téléservice.
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/associerDemandeCourrier")
public class AssociationDemandeCourrierController extends AbstractXafController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssociationDemandeCourrierController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

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
            // Récupération de l'ID de la démarche dans le Context-Param
            String demarcheId = propertiesResolver.getDemarcheId();
            Integer usagerId = usagerInfosDTO.getId();
            LOGGER.info("DemarcheID={}, UsagerID={}, IdentifiantDemande={}, NomProprio={}", demarcheId, usagerId, identifiant, nomProprio);
            LOGGER.info("Appel à la démarche...");
            AfApiClient afApiClient = getAfApiClient();
            afApiClient.associerDemandeCourrier(identifiant, nomProprio, usagerId);
            LOGGER.info("Retour au client...");
            LOGGER.info("====================== Fin /associerDemandeCourrier doPost()");

            return ResponseEntity.ok().build();
        } catch (Exception exception) {
            LOGGER.error("AssociationDemandeCourrierServlet - Une erreur est survenue lors de l'appel à la méthode POST",
                    exception);
            return ResponseEntity.status(getCodeErreur(exception)).build();
        }
    }
}
