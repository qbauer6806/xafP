package mc.gouv.xaf.front.paiement;

import mc.gouv.xaf.front.controller.AbstractXafController;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.apiclient.paiement.PaiementApiClient;
import mc.gouv.xaf.apiclient.paiement.PaiementConstant;
import mc.gouv.xaf.apiclient.paiement.monetico.dto.MoneticoDTO;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Servlet permettant au Front de récupérer les données afin de générer le formulaire de paiement
 *
 * @author mboutelier.ext
 */
@Controller
@RequestMapping("/info-paiement")
public class PaiementInfoController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementInfoController.class);


    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver propertiesResolver;

    protected PaiementApiClient getStcApiClient() {
        return new PaiementApiClient(propertiesResolver.getApiUrl(), propertiesResolver.getApiJwt());
    }

    /**
     * Interface Aller
     */
    @GetMapping
    public ResponseEntity doGet(HttpServletRequest request) {
        LOGGER.info("====================== /paiement-info doGet()");
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        LOGGER.info("Récupération des paramètres...");
        String demandeIds = request.getParameter(RequestConstant.DEMANDES_ID_PARAM);
        String langue = request.getParameter(RequestConstant.LANGUE_PARAM);
        boolean iframe = Boolean.parseBoolean(request.getParameter(PaiementConstant.IFRAME_PARAM));

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        String safeIds = demandeIds != null ? demandeIds.replaceAll(SharedMessages.UNSAFE_CHARS, "_") : null;
        LOGGER.info("Récupération des données de paiement pour la demande {}...", safeIds);
        MoneticoDTO paiement = getStcApiClient().getPaiement(demandeIds, langue, usagerId, iframe);

        LOGGER.info("====================== Fin /paiement-info doGet()");
        return ResponseEntity.ok(paiement);
    }
}
