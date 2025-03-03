package mc.gouv.xaf.front.paiement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.apiclient.paiement.monetico.dto.MoneticoDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoOutputDTO;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.PaymentMethodStatusEnum;
import mc.gouv.xaf.front.controller.AbstractXafController;
import mc.gouv.xaf.front.dto.PaymentMethodReferenceDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.paiement.enums.TableauPaiementTypeEnum;
import mc.gouv.xaf.front.util.MwpaymntService;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import mc.gouv.xaf.shared.paiement.enums.PSPEnum;
import mc.gouv.xaf.shared.paiement.infopaiement.InfoPaiementInputDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementOutputDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Servlet permettant à Monetico d'enregistrer un paiement
 *
 * @author mboutelier.ext
 */
@Controller
public class PaiementController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private MwpaymntService mwpaymntService;

    /**
     * Interface Retour
     */
    @GetMapping(value = { "/paiement" })
    public ResponseEntity doPost(HttpServletRequest request) {
        LOGGER.info("====================== /paiement doPost()");
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
            String sResult = getMoneticoApiClient().updatePaiementStatus(moneticoResponseDTO);
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

    @PostMapping(value = { "/info-paiement" })
    public ResponseEntity infoPaiement(@RequestBody(required = true) InfoPaiementInputDTO infoPaiementInput,
            HttpServletRequest request) {
        LOGGER.info("====================== /info-paiement POST start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LOGGER.info("Récupération du PSP...");

        // On garde cette condition car mwpaymnt ne gère pas encore monetico
        if (infoPaiementInput.getProviderName().equalsIgnoreCase(PSPEnum.MONETICO.name())) {
            LOGGER.info("====================== Fin /info-paiement doPost() monetico\n");
            return processMoneticoCall(usagerInfosDTO, infoPaiementInput);
        } else if (infoPaiementInput.getProviderName().equalsIgnoreCase(PSPEnum.LYRA.name())) {
            // TODO Stockage en db des infos de paiement en fonction du body d'entrée

            // Puis call au middleware de paiement
            return processMwpaymntCall(usagerInfosDTO);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = { "/moyen-paiement" })
    public ResponseEntity getMoyenPaiement(HttpServletRequest request) {
        //TODO
        LOGGER.info("====================== /moyen-paiement GET start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // TODO Récupération des alias dans mon guichet
        List<PaymentMethodReferenceDTO> monGuichetAliases = new ArrayList<>();
        PaymentMethodReferenceDTO reference1 = new PaymentMethodReferenceDTO();
        reference1.setPaymentMethodToken("348a52572cd14366b88e8733d4af894e");
        reference1.setPaymentMethodName("Carte 1");
        reference1.setPaymentMethodType("CB");
        monGuichetAliases.add(reference1);
        PaymentMethodReferenceDTO reference2 = new PaymentMethodReferenceDTO();
        reference2.setPaymentMethodToken("1163368f63284152b506ea47a81dd690");
        reference2.setPaymentMethodName("Carte 2");
        reference2.setPaymentMethodType("CB");
        monGuichetAliases.add(reference2);

        // Appel à lyra pour obtenir les infos
        List<MoyenPaiementOutputDTO> moyenPaiementOutputDTOs = new ArrayList<>();
        for (PaymentMethodReferenceDTO monGuichetAlias : monGuichetAliases) {
            InfoOutputDTO info = getMwpaymtApiClient(usagerInfosDTO.getTokenInfo().getAccessToken()).getInfo(
                    mwpaymntService.getInfoInput(monGuichetAlias));
            if (info.getPaymentMethodInformation().getPaymentMethodStatus().equals(PaymentMethodStatusEnum.ACTIVE)) {
                moyenPaiementOutputDTOs.add(
                        mwpaymntService.mwpaymentResponseToMoyenPaiement(info, monGuichetAlias.getPaymentMethodName()));
            }
        }
        moyenPaiementOutputDTOs.sort(new MoyenPaiementComparator());
        LOGGER.info("====================== /moyen-paiement GET end...");
        return ResponseEntity.ok(moyenPaiementOutputDTOs);
    }

    class MoyenPaiementComparator implements Comparator<MoyenPaiementOutputDTO> {

        @Override
        public int compare(MoyenPaiementOutputDTO a, MoyenPaiementOutputDTO b) {
            String nomA = a.getNom();
            String nomB = b.getNom();
            // TODO remplacer le -1 par une comparaison de la date à laquelle l'alias a été
            // crée si les nom sont identiques
            return !nomA.equals(nomB) ? nomA.compareTo(nomB) : -1;
        }
    }

    @PostMapping(value = { "/moyen-paiement" })
    public ResponseEntity saveMoyenPaiement(HttpServletRequest request) {
        //TODO
        LOGGER.info("====================== /moyen-paiement POST start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LOGGER.info("====================== /moyen-paiement POST end...");
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = { "/info-facturation" })
    public ResponseEntity getInfoFacturation(HttpServletRequest request) {
        //TODO
        LOGGER.info("====================== /info-facturation GET start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LOGGER.info("====================== /info-facturation GET end...");
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = { "/info-facturation" })
    public ResponseEntity saveInfoFacturation(HttpServletRequest request) {
        // TODO
        LOGGER.info("====================== /info-facturation POST start...");

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LOGGER.info("====================== /info-facturation POST end...");
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = { "/tableau-paiement" })
    public ResponseEntity getTableauPaiement(@RequestParam TableauPaiementTypeEnum type, @RequestParam String ids,
            HttpServletRequest request) {
        // TODO
        LOGGER.info("====================== /tableau-paiement POST start...");
        LOGGER.info("type : {}, ids : {}", type, ids);

        // Appel à la gateway de paiement pour récupérer le formToken
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            LOGGER.error(SharedMessages.UTILISATEUR_NON_AUTORISE);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // On va chercher selon les paramètres donnés (ie dans les brouillons ou dans les
        // demandes) le montant de l'opération ainsi que le total à fournir au front
        List<TableauDTO> tableauPaiementResponse = getPaiementApiClient().getTableauPaiement(ids,
                type.name(), usagerInfosDTO.getId());

        LOGGER.info("====================== /tableau-paiement GET end...");
        return ResponseEntity.ok(tableauPaiementResponse);
    }

    private ResponseEntity processMwpaymntCall(UsagerInfosDTO usagerInfosDTO) {
        RegisterOutputDTO token = getMwpaymtApiClient(usagerInfosDTO.getTokenInfo().getAccessToken()).getToken(
                mwpaymntService.getRegisterInput(usagerInfosDTO));
        LOGGER.info("====================== /info-paiement POST end...");
        return ResponseEntity.ok(token);
    }

    private ResponseEntity processMoneticoCall(UsagerInfosDTO usagerInfosDTO, InfoPaiementInputDTO infoPaiementInput) {
        List<String> demandeIds = infoPaiementInput.getDemandesId();
        String langue = infoPaiementInput.getLangue();
        boolean iframe = infoPaiementInput.isIframe();
        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        LOGGER.info("Récupération des données de paiement pour les demandes {}...", demandeIds);
        MoneticoDTO paiement = getMoneticoApiClient().getPaiement(demandeIds, langue, usagerId, iframe);
        LOGGER.info("====================== Fin /info-paiement doPost() Monetico\n");
        return ResponseEntity.ok(paiement);
    }

}
