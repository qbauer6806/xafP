package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.MwpaymtApiClient;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.TransactionActionDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.ActionDebitEnum;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeArticleRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.InformationFacturationRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.entity.InformationFacturationBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;
import mc.gouv.xaf.back.paiement.data.transformer.InfoFacturationTransformer;
import mc.gouv.xaf.back.paiement.dto.DebitDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.enums.StatutDebitEnum;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import mc.gouv.xaf.back.paiement.service.TableauPaiementService;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import mc.gouv.xaf.back.paiement.service.kafka.GUKafkaPaiementProducer;
import mc.gouv.xaf.back.paiement.service.kafka.dto.AffichagePaiementMessage;
import mc.gouv.xaf.back.paiement.service.kafka.dto.PaymentTypeEnum;
import mc.gouv.xaf.back.paiement.transformer.MwpaymtTransformer;
import mc.gouv.xaf.back.paiement.utils.PaiementUtils;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.gichuni.api.GichuniApiClient;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.keycloak.KeycloakTokenService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.AdresseFacturationDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import mc.gouv.xaf.shared.paiement.MwpaymtGenericCallbackDTO;
import mc.gouv.xaf.shared.paiement.PaymentMethodInformationDTO;
import mc.gouv.xaf.shared.paiement.enums.PSPEnum;
import mc.gouv.xaf.shared.paiement.infofacturation.AdresseDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.VousDTO;
import mc.gouv.xaf.shared.paiement.mongichet.PaymentMethodReferenceDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementInputDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(rollbackFor = Exception.class)
public class PaiementServiceImpl implements PaiementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementServiceImpl.class);
    private static final String EN_COURS_PAIEMENT_STATUT_KEY = "EN_COURS_PAIEMENT";
    private static final String TARIF_CR_DEMAT_KEY = "XAF_TARIF_CR_DEMAT";
    private static final String SLEEP_TIME_ECRITURE_DONNEES_MONETIQUES = "XAF_SLEEP_TIME_ECRITURE_DONNEES_MONETIQUES";
    private static final String MAIL_DEBIT_ECHEC_AGENT_CODE = "MAIL_DEBIT_ECHEC_AGENT";
    private static final String MAIL_NOTIFICATION_DEMANDE_PAYEE_AGENT_CODE = "MAIL_NOTIFICATION_DEMANDE_PAYEE_AGENT";
    private static final String MAIL_RATTRAPAGE_DEBIT_ECHEC_CODE = "MAIL_RATTRAPAGE_DEBIT_ECHEC_TECHNIQUE";
    public static final String OBJET = "_OBJET";
    public static final String CORPS = "_CORPS";

    @Autowired
    private TableauPaiementService tableauPaiementService;

    @Autowired
    private BrouillonsService brouillonsService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeArticleRepository commandeDemandeArticleRepository;

    @Autowired
    private InformationFacturationRepository infoFacturationRepository;

    @Autowired
    private MontantService montantService;

    @Autowired
    private CommandesDemandesService commandesDemandesService;

    @Autowired
    private DemandesStatutsService demandesStatutsService;

    @Autowired
    private DemandesTransformer demandesTransformer;

    @Autowired
    private GichuniApiClient gichuniApiClient;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private MwpaymtTransformer mwpaymtTransformer;

    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    @Autowired
    private PaiementsDataProvider paiementsDataProvider;

    @Autowired
    private GUKafkaPaiementProducer guKafkaPaiementProducer;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private KeycloakTokenService keycloakTokenService;

    @Autowired
    private FactureService factureService;

    @Autowired
    private MailService mailService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Autowired
    private PaiementHistoriqueService paiementHistoriqueService;

    @Override
    public List<TableauDTO> getTableauPaiement(String ids, String objectType, Integer usagerId) {
        List<TableauDTO> result = new ArrayList<>();
        List<String> idsList = Arrays.asList(ids.replace("[", "").replace("]", "").split(","));
        if (objectType.equals(RequestConstant.BROUILLONS_PATH)) {
            for (String currentId : idsList) {
                BrouillonDTO brouillon = brouillonsService.getBrouillon(Integer.valueOf(currentId), usagerId);
                JsonNode contenu = brouillon.getContenu();
                // On va chercher l'objet dans l'implémentation de TableauPaiementService propre à chaque TS
                TableauDTO itemTableauPaiement = tableauPaiementService.getItemTableauPaiement(contenu, brouillon.getPkBrouillons());
                if (null != itemTableauPaiement) {
                    result.add(itemTableauPaiement);
                }
            }
        } else if (objectType.equals(RequestConstant.DEMANDES_PATH)) {
            for (String currentId : idsList) {
                DemandeDTO demande = demandesService.getDemande(Integer.valueOf(currentId), usagerId);
                JsonNode contenu = demande.getContenu();
                // On va chercher l'objet dans l'implémentation de TableauPaiementService propre à chaque TS
                TableauDTO itemTableauPaiement = tableauPaiementService.getItemTableauPaiement(contenu, demande.getPkDemandes());
                if (null != itemTableauPaiement) {
                    result.add(itemTableauPaiement);
                }
            }
        }
        return result;
    }

    @Override
    public InfoFacturationResponseDTO getInfoFacturation(GichuniUsagerDTO usager) {
        InfoFacturationResponseDTO result = new InfoFacturationResponseDTO();
        VousDTO vous = new VousDTO();
        // Identité
        vous.setNom(usager.getNom());
        vous.setPrenom(usager.getPrenom());
        vous.setTitre(usager.getTitre());
        result.setVous(vous);
        result.setEmail(usager.getEmail());

        // Adresse dans adresseFacturation
        AdresseDTO adresse = new AdresseDTO();
        AdresseFacturationDTO adresseFacturation = usager.getAdresseFacturation();
        if(adresseFacturation != null) {
            adresse.setLigne1(adresseFacturation.getAdresse());
            adresse.setLigne2(adresseFacturation.getComplAdresse1() != null ? adresseFacturation.getComplAdresse1() : "");
            adresse.setLigne3(adresseFacturation.getComplAdresse2() != null ? adresseFacturation.getComplAdresse2() : "");
            adresse.setPays(adresseFacturation.getPaysCode());
            adresse.setCodePostal(adresseFacturation.getCodePostal());
            adresse.setVille(adresseFacturation.getVille());
        }
        result.setAdresse(adresse);

        // Raison sociale
        result.setRaisonSociale(usager.getRaisonSociale());
        result.setSaveRaisonSociale(true);
        result.setProfilType(usager.getType().getValue());
        return result;
    }

    @Override
    public boolean createMoyenPaiement(String ids, GichuniUsagerDTO usager, String orderId, String raisonSociale, String langue) {
        logStartMethod(LOGGER);
        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        String replace = ids.replace("[", "").replace("]", "");
        List<Integer> demandeIds = Stream.of(replace.split(",")).map(String::trim).map(Integer::parseInt).toList();
        Map<Integer, DemandeBO> demandes = new HashMap<>();
        Map<Integer, BigDecimal> totauxDemandes = new HashMap<>();
        Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes = new HashMap<>();
        moyenPaiement.setPkMoyensPaiements(orderId);
        // Si les demandes fournies sont déjà encaissées on retourne false pour lever un 409 au front
        if(sontDejaEncaissees(demandeIds)) {
            return false;
        }
        // Je crée la commande que j'associerai à mon moyen de paiement
        BigDecimal totalCommande = calculTotalCommande(demandeIds, usager.getId(), demandes, totauxDemandes,
                articlesDemandes);
        CommandeBO commande = createCommande(totalCommande, moyenPaiement, demandeIds, demandes, totauxDemandes,
                articlesDemandes);
        createInfoFacturation(usager, commande, raisonSociale, langue);
        LocalDateTime now = LocalDateTime.now();
        moyenPaiement.setCommande(commande);
        moyenPaiement.setDateCreation(now);
        moyenPaiement.setDateDerniereModification(now);
        moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutEnum.EN_ATTENTE_DE_VALIDATION);
        moyenPaiement.setPaymentSupplier(PSPEnum.LYRA);
        moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
        return true;
    }

    private boolean sontDejaEncaissees(List<Integer> demandeIds) {
        List<DemandeDTO> demandes = new ArrayList<>();
        for (Integer pkDemande : demandeIds) {
            DemandeDTO demande = demandesService.getDemande(pkDemande);
            if (null != demande && demarchesDataProvider.statutsDejaEncaisses()
                    .contains(demande.getDernierStatut().getName())) {
                demandes.add(demande);
            }
        }
        return demandeIds.size() == demandes.size();
    }

    @Override
    public void updateMoyenPaiement(MoyenPaiementInputDTO moyenPaiementInputDTO, String usagerToken) {
        // On retrouve le moyen de paiement associé à l'orderId
        MoyenPaiementBO moyenPaiementBo = moyenPaiementRepository.getReferenceById(
                moyenPaiementInputDTO.getReference());
        moyenPaiementBo.setPaymentMethodName(moyenPaiementInputDTO.getCardName());
        if (moyenPaiementInputDTO.isNew()) {
            // On est dans le cas ou l'usager à coché la case "Sauvegarde du moyen de
            // paiement pour plus tard"
            moyenPaiementBo.setPaymentMethodRecord(MoyenPaiementStatutEnum.ENREGISTRE_A_LA_CREATION.name());
        }
        // On est dans le cas ou l'usager a sélectionné un moyen de paiement existant sinon c'est le callback SPG qui nous fournira l'info
        if (moyenPaiementInputDTO.getPaymentMethodToken() != null && !moyenPaiementInputDTO.getPaymentMethodToken()
                .isEmpty()) {
            MwpaymtApiClient mwpaymtApiClient = new MwpaymtApiClient(gouvPropertiesResolver.getMwpaymtUrl(),
                    keycloakTokenService.exchangeUserToken(usagerToken));
            InfoCancelInputDTO input = new InfoCancelInputDTO();
            input.setPaymentMethodToken(moyenPaiementInputDTO.getPaymentMethodToken());
            mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO info = mwpaymtApiClient.getInfo(
                    input);
            moyenPaiementBo.setPaymentMethodToken(moyenPaiementInputDTO.getPaymentMethodToken());
            moyenPaiementBo.setPaymentMethodType("CARD");
            moyenPaiementBo.setEffectiveBrand(moyenPaiementInputDTO.getType());
            moyenPaiementBo.setPaymentMethodAccount(info.getPan());
            moyenPaiementBo.setExpiryDate(PaiementUtils.calculateExpiration(Integer.valueOf(info.getExpiryMonth()),
                    Integer.valueOf(info.getExpiryYear())));
            // Changer la demande de status
            debitEtMajStatut(moyenPaiementBo);
        }
        moyenPaiementBo.setDateDerniereModification(LocalDateTime.now());
        moyenPaiementRepository.save(moyenPaiementBo);
    }

    @Override
    public mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO getMoyenPaiement(InfoCancelInputDTO input, String usagerToken) {
        MwpaymtApiClient mwpaymtApiClient = new MwpaymtApiClient(gouvPropertiesResolver.getMwpaymtUrl(),
                keycloakTokenService.exchangeUserToken(usagerToken));
        return mwpaymtApiClient.getInfo(input);
    }

    @Override
    public List<PaymentMethodReferenceDTO> getReferences(String usagerToken) {
        return gichuniApiClient.getReferences(keycloakTokenService.exchangeUserToken(usagerToken));
    }

    @Async
    @Override
    public void updatePaiementStatusAsync(MwpaymtGenericCallbackDTO callbackDTO) {
        try {
            PropertiesDTO property = propertiesService.getProperty(SLEEP_TIME_ECRITURE_DONNEES_MONETIQUES);
            if(property != null && property.getValue() != null) {
                LOGGER.info("Attente {} millisec lors du callback", Integer.valueOf(property.getValue()));
                Thread.sleep(Integer.valueOf(property.getValue()));
            } else {
                LOGGER.info("Attente 2 sec lors du callback");
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        updatePaiementStatus(callbackDTO);
    }

    @Override
    public void updatePaiementStatus(MwpaymtGenericCallbackDTO callbackDTO) {
        LOGGER.info("Mise à jour du status de paiement suite à un callback reçu de MWPAYMT");

        // On retrouve le moyen de paiement associé à l'orderId
        MoyenPaiementBO moyenPaiementBo = moyenPaiementRepository.findById(callbackDTO.getOrderId()).orElseThrow(
                () -> new DemarchesServiceException("Aucun orderId " + callbackDTO.getOrderId() + " n'a été trouvé.",
                        HttpStatus.NOT_FOUND));

        PaymentMethodInformationDTO paymentMethodInformation = callbackDTO.getPaymentMethodInformation();
        moyenPaiementBo.setPaymentMethodType(paymentMethodInformation.getPaymentMethodType());
        moyenPaiementBo.setPaymentMethodToken(paymentMethodInformation.getPaymentMethodToken());
        moyenPaiementBo.setMoyenPaiementStatut(
                MoyenPaiementStatutEnum.fromLibelle(paymentMethodInformation.getPaymentMethodStatus().name()));
        moyenPaiementBo.setDateDerniereModification(LocalDateTime.now());
        moyenPaiementBo.setEffectiveBrand(paymentMethodInformation.getEffectiveBrand());
        moyenPaiementBo.setPaymentMethodAccount(paymentMethodInformation.getPan());
        moyenPaiementBo.setExpiryDate(PaiementUtils.calculateExpiration(paymentMethodInformation.getExpiryMonth(),
                paymentMethodInformation.getExpiryYear()));
        if (moyenPaiementBo.getMoyenPaiementStatut().equals(MoyenPaiementStatutEnum.VALIDE)) {
            debitEtMajStatut(moyenPaiementBo);
            if (moyenPaiementBo.getPaymentMethodRecord() != null && moyenPaiementBo.getPaymentMethodRecord()
                    .equals(MoyenPaiementStatutEnum.ENREGISTRE_A_LA_CREATION.name())) {
                LOGGER.info("Sauvegarde du moyen de paiement dans mon guichet suite à un callback reçu de MWPAYMT");
                gichuniApiClient.saveReference(paymentMethodInformation.getPaymentMethodType(),
                        paymentMethodInformation.getPaymentMethodToken(), moyenPaiementBo.getPaymentSupplier().name(),
                        gouvPropertiesResolver.getDemarcheId(), moyenPaiementBo.getPaymentMethodName(),
                        callbackDTO.getSub());
            }
        }
        moyenPaiementRepository.save(moyenPaiementBo);
    }

    private void debitEtMajStatut(MoyenPaiementBO moyenPaiementBo) {
        List<DemandeBO> demandes = commandesDemandesService.getDemandesFromCommande(moyenPaiementBo.getCommande().getPkCommandes());
        List<DemandeBO> demandesAFaireAvancer = demandes.stream().filter(d -> !d.getDernierStatut().getName().equals(demarchesDataProvider.statutPaiementARegulariser()))
                .toList();
        if (!demandesAFaireAvancer.isEmpty()) {
            demandesStatutsService.updateMultipleStatuts(demandesAFaireAvancer, EN_COURS_PAIEMENT_STATUT_KEY);
        }
        updateDemandes(demandes);
    }

    @Override
    public RegisterOutputDTO postInfoPaiement(RegisterInputDTO input, String usagerToken) {
        logStartMethod(LOGGER);
        MwpaymtApiClient mwpaymtApiClient = new MwpaymtApiClient(gouvPropertiesResolver.getMwpaymtUrl(),
                keycloakTokenService.exchangeUserToken(usagerToken));
        return mwpaymtApiClient.getToken(input);
    }

    @Override
    public void majTarif(Integer tarif) {
        PropertiesDTO property = propertiesService.getProperty(TARIF_CR_DEMAT_KEY);
        if (property != null) {
            propertiesService.updatePropertyValue(property.getPkProperties(), String.valueOf(tarif));
        }
    }

    @Override
    public void majStatutCaisse(String authorization) {
        LOGGER.info("Ouverture de la caisse, début du rattrapage pour d'éventuels paiements en cours");
        logStartMethod(LOGGER);
        List<CommandeOperationBO> latestCommandeOperationForStatus = commandeOperationRepository.findLatestCommandesOperationsForStatus(OperationStatutEnum.EN_ATTENTE);
        for (CommandeOperationBO commandeOperation : latestCommandeOperationForStatus) {
            DemandeDTO demande = demandesService.getDemande(commandeOperation.getDemande().getPkDemandes());
            String identifiant = demande.getIdentifiant();
            DebitDTO debit = debit(identifiant, null, authorization);
            try {
                if (debit.getStatut().equals(StatutDebitEnum.PAID)) {
                    // envoi mail agent
                    envoiMailAgent(demande, true);
                    MultipartFile recuPaiement = paiementsDataProvider.regularisationPaiement(debit, identifiant);
                    sauvegardeRecuPaiement(recuPaiement, identifiant);
                }
            } catch (Exception e) {
                envoiMailIncident(e, identifiant);
            }
        }
        logEndMethod(LOGGER);
    }

    void envoiMailIncident(Exception e, String identifiant) {
        LOGGER.error("Erreur lors du retour débit coté RESID");
        Set<String> mailingLists = mailService.getMailingLists(
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RESID.name());
        Map<String, Object> model = new HashMap<>();
        String strException = ExceptionUtils.getStackTrace(e);
        strException = strException.replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;");
        if (strException.length() > 3000) {
            strException = strException.substring(0, 3000) + "...<br/>";
        }
        model.put("exception", strException);
        mailService.sendMailSupport(MAIL_RATTRAPAGE_DEBIT_ECHEC_CODE + OBJET,
                MAIL_RATTRAPAGE_DEBIT_ECHEC_CODE + CORPS, mailingLists, null, identifiant, 0, model, null);
    }

    @Override
    public DebitDTO debit(String idTs, String orderIdResid, String keycloakToken) {
        logStartMethod(LOGGER);
        // En fonction de l'idTs retrouver toutes les informations (moyen paiement, facturation)
        DemandeBO demandeBo = demandesRepository.findByIdentifiant(idTs);
        Integer pkDemandes = demandeBo.getPkDemandes();
        Optional<CommandeDemandeBO> latestCommandeForDemande = commandeDemandeRepository.findLatestCommandeForDemande(
                pkDemandes);
        CommandeDemandeBO commandeDemande = latestCommandeForDemande.orElseThrow(
                () -> new EntityNotFoundException("Aucune commande trouvée pour la demande " + pkDemandes));
        MoyenPaiementBO moyenPaiement = moyenPaiementRepository.findByDemande_PkDemandesAndLastCreationDate(pkDemandes);
        InformationFacturationBO infoFacturation = infoFacturationRepository.findByCommande_PkCommandes(
                moyenPaiement.getCommande().getPkCommandes());
        DebitInputDTO debitInputDTO = mwpaymtTransformer.infoDebitToMwpaymtDebitDTO(idTs, orderIdResid, moyenPaiement,
                infoFacturation, commandeDemande.getMontant());
        MwpaymtApiClient mwpaymtApiClient = new MwpaymtApiClient(gouvPropertiesResolver.getMwpaymtUrl(), keycloakToken);
        DemandesUsagersBO usager = demandeBo.getUsager();
        Integer usagerId = usager.getId();
        GouvBPMUser user = new GouvBPMUser();
        user.setId(usagerId.toString());
        DebitOutputDTO debit;
        try {
            if (paiementsDataProvider.isCaisseOuverte()) {
                LOGGER.info("La caisse est ouverte : tentative de débit sur le middleware de paiement");
                debit = mwpaymtApiClient.debit(debitInputDTO);
            } else {
                LOGGER.info("La caisse est fermée, pas de débit tenté pour la demande {}", idTs);
                debit = createDebitPending();
            }
        } catch (Exception ex) {
            LOGGER.info("Error lors de la demande de débit tenté pour la demande {}: {}", idTs, ex.getMessage());
            debit = createDebitEnEchec();
            CommandeOperationBO operation = getCommandeOperationBO(debit, commandeDemande);
            commandeOperationRepository.save(operation);
            majHistoriqueDebit(pkDemandes, demandeBo, debit.getTransactionAction().getActionDebit(),
                    moyenPaiement, commandeDemande);
            envoiMailAgent(demandesService.getDemande(pkDemandes), false);
            postKafkaMessage(usagerId, moyenPaiement, debit, operation, demandeBo);
            return mwpaymtTransformer.debitOutputDTOToDebitDTO(debit);
        }
        CommandeOperationBO operation = getCommandeOperationBO(debit, commandeDemande);
        commandeOperationRepository.save(operation);
        majHistoriqueDebit(pkDemandes, demandeBo, debit.getTransactionAction().getActionDebit(),
                moyenPaiement, commandeDemande);
        postKafkaMessage(usagerId, moyenPaiement, debit, operation, demandeBo);
        logEndMethod(LOGGER);
        return mwpaymtTransformer.debitOutputDTOToDebitDTO(debit);
    }

    private void postKafkaMessage(Integer usagerId, MoyenPaiementBO moyenPaiement, DebitOutputDTO debit,
            CommandeOperationBO operation, DemandeBO demandeBo) {
        DemandeDTO demande = demandesTransformer.bo2Dto(demandeBo);
        AffichagePaiementMessage apm = new AffichagePaiementMessage(gouvPropertiesResolver.getDemarcheId(),
                demarchesDataProvider.getProcedureCode(), usagerId.toString(), PaymentTypeEnum.DEMANDE,
                moyenPaiement.getPaymentMethodToken(), debit.getTransactionAction().getDateDebit() != null
                ? debit.getTransactionAction().getDateDebit()
                : LocalDateTime.now(), operation.getMontant(),
                debit.getTransactionAction().getActionDebit().name(), demarchesDataProvider.getObjetPaiement(demande),
                demandeBo.getIdentifiant(),
                demande.getDateCreation().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                moyenPaiement.getExpiryDate(), moyenPaiement.getPaymentMethodAccount(),
                moyenPaiement.getEffectiveBrand(),
                gouvPropertiesResolver.getFrontUrl() + "/demande_view.html?id=" + demandeBo.getPkDemandes());
        guKafkaPaiementProducer.sendAffichagePaiementMessage(apm);
    }

    void envoiMailAgent(DemandeDTO demande, boolean debitEnSucces) {
        LOGGER.info("==== xaf-back-paiement ENVOI EMAIL AGENT ...");

        EmailInfoDTO emailInfo = getEmailInfoDTO(debitEnSucces);
        Map<String, Object> model = afMailTemplateModelProvider.getModel(emailInfo.getSubjectTemplateCode(), emailInfo.getBodyTemplateCode(), demande,
                null, null, null);
        try {
            mailService.sendMail(emailInfo, model, MailAudienceEnum.AGENT);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private EmailInfoDTO getEmailInfoDTO(boolean debitEnSucces) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        if (debitEnSucces) {
            emailInfo.setBodyTemplateCode(MAIL_NOTIFICATION_DEMANDE_PAYEE_AGENT_CODE + CORPS);
            emailInfo.setSubjectTemplateCode(MAIL_NOTIFICATION_DEMANDE_PAYEE_AGENT_CODE + OBJET);
        } else {
            emailInfo.setBodyTemplateCode(MAIL_DEBIT_ECHEC_AGENT_CODE + CORPS);
            emailInfo.setSubjectTemplateCode(MAIL_DEBIT_ECHEC_AGENT_CODE + OBJET);
        }
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addTo(afBackUtils.getDemarcheInfos().getEmailService(), afBackUtils.getDemarcheInfos().getNom());
        emailInfo.setLangue("fr");
        return emailInfo;
    }

    private void updateCommande(CommandeBO commande, double montantCapture) {
        double montantRestant = commande.getMontantInitial() - commande.getMontantDejaCapture() - montantCapture;
        LOGGER.info("Montant restant : {}", montantRestant);
        commande.setMontantRestant(montantRestant);
        commande.setMontantDejaCapture(commande.getMontantDejaCapture() + montantCapture);
        LOGGER.info("Sauvegarde de la commande en base : {}", commande);
        commandeRepository.save(commande);
    }

    private void majHistoriqueDebit(Integer pkDemandes, DemandeBO demandeBo, ActionDebitEnum actionDebit,
            MoyenPaiementBO moyenPaiement, CommandeDemandeBO commandeDemande) {
        LOGGER.info("Mise à jour de l'historique de la demande {}", pkDemandes);
        String state = "";
        String action = "";
        if(actionDebit.equals(ActionDebitEnum.SUCCESS)) {
            LOGGER.info("Le paiement est en succès, mise à jour de la commande {} en DB", moyenPaiement.getCommande().getPkCommandes());
            updateCommande(moyenPaiement.getCommande(), commandeDemande.getMontant());
            state = "DEBIT_REALISE";
            action = "Débit réalisé avec succès";
            paiementHistoriqueService.ajouterHistoriqueDebitOK(demandesTransformer.bo2Dto(demandeBo));
            demandesHistoriqueService.actionSysteme(pkDemandes, state, action);
        } else if (actionDebit.equals(ActionDebitEnum.FAILURE)) {
            state = "DEBIT_ECHEC";
            action = "Débit en échec. Demande de paiement envoyée";
            paiementHistoriqueService.ajouterHistoriqueDebitEchec(demandesTransformer.bo2Dto(demandeBo));
            demandesHistoriqueService.actionSysteme(pkDemandes, state, action);
        }
    }

    private static CommandeOperationBO getCommandeOperationBO(DebitOutputDTO debit,
            CommandeDemandeBO commandeDemande) {
        CommandeOperationBO operation = new CommandeOperationBO();
        LocalDateTime now = LocalDateTime.now();
        operation.setCommande(commandeDemande.getCommande());
        operation.setDemande(commandeDemande.getDemande());
        operation.setDateCreation(debit.getTransactionAction().getDateCreationDebit());
        operation.setDateRealisation(debit.getTransactionAction().getDateDebit());
        operation.setOperationType(OperationTypeEnum.DEBIT);
        switch (debit.getTransactionAction().getActionDebit()) {
            case SUCCESS:
                operation.setOperationStatut(OperationStatutEnum.SUCCES);
                break;
            case PENDING:
                operation.setOperationStatut(OperationStatutEnum.EN_ATTENTE);
                operation.setDateCreation(PaiementUtils.toUtc(now));
                operation.setDateRealisation(PaiementUtils.toUtc(now));
                break;
            case FAILURE:
                operation.setOperationStatut(OperationStatutEnum.ECHEC);
                operation.setDateCreation(PaiementUtils.toUtc(now));
                operation.setDateRealisation(PaiementUtils.toUtc(now));
                break;
        }
        operation.setMontant(commandeDemande.getMontant());
        return operation;
    }

    private static DebitOutputDTO createDebitEnEchec() {
        DebitOutputDTO result = new DebitOutputDTO();
        TransactionActionDTO action = new TransactionActionDTO();
        action.setActionDebit(ActionDebitEnum.FAILURE);
        action.setDateDebit(null);
        result.setTransactionAction(action);
        return result;
    }

    private static DebitOutputDTO createDebitPending() {
        DebitOutputDTO result = new DebitOutputDTO();
        TransactionActionDTO action = new TransactionActionDTO();
        action.setActionDebit(ActionDebitEnum.PENDING);
        action.setDateDebit(LocalDateTime.now());
        result.setTransactionAction(action);
        return result;
    }

    @Async
    public void updateDemandes(List<DemandeBO> demandes) {
        for (DemandeBO demande : demandes) {
            try {
                DemandesUsagersBO usager = demande.getUsager();
                Integer pkDemande = demande.getPkDemandes();
                Integer usagerId = usager.getId();

                GouvBPMUser user = new GouvBPMUser();
                user.setId(usagerId.toString());

                LOGGER.info("Ajout de l'historique de paiement...");
                PaiementHistoriqueDTO historique = new PaiementHistoriqueDTO();
                historique.setFkDemandes(demande.getPkDemandes());
                if (usager != null) {
                    historique.setContenu("Usager " + usager.getPrenom() + " " + usager.getNom() + " : Enregistre sa carte bancaire");
                }
                historique.setStatut(PaiementStatutEnum.CARTE_VALIDE);
                historique.setDate(LocalDateTime.now());
                historique.setUsagerId(usagerId);
                paiementHistoriqueService.ajouterHistorique(historique);

                if (demande.getDernierStatut().getName().contains(demarchesDataProvider.statutPaiementARegulariser())) {
                    String identifiant = demande.getIdentifiant();
                    DebitDTO debit = debit(identifiant, "00000", keycloakTokenService.getAccessToken());
                    if (!debit.getStatut().equals(StatutDebitEnum.UNPAID)) {
                        demandesStatutsService.updateStatut(demande,
                                demarchesDataProvider.statutPaiementARegulariserEnCours(), null, null, null, null,
                                null);
                    }
                    try {
                        if (debit.getStatut().equals(StatutDebitEnum.PAID)) {
                            MultipartFile recuPaiement = paiementsDataProvider.regularisationPaiement(debit,
                                    identifiant);
                            sauvegardeRecuPaiement(recuPaiement, identifiant);
                            envoiMailAgent(demandesTransformer.bo2Dto(demande), true);
                        }

                    } catch (Exception e) {
                        envoiMailIncident(e, identifiant);
                    }
                } else {
                    LOGGER.info("Progression dans le BPM pour la demande {}...", pkDemande);
                    Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(pkDemande);
                    variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), usagerId.toString());
                    variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), null);
                    gouvBPM.setProcessBusinessVariables(pkDemande, variables);

                    GouvBPMTask task = gouvBPM.getActiveTasksForDemande(pkDemande).getFirst();
                    gouvBPM.claimTask(task, user);
                    gouvBPM.completeTask(task, pkDemande);
                }
            } catch (Exception ex) {
                LOGGER.error("Erreur dans updateDemandes pour une demande : {}", ex.getMessage(), ex);
            }
        }
    }


    void sauvegardeRecuPaiement(MultipartFile recuPaiement, String identifiant) {
        if (recuPaiement != null) {
            LOGGER.info("Sauvegarde du reçu de paiement pour la demande {}", identifiant);
            factureService.saveRecuPaiement(identifiant, recuPaiement);
        } else {
            LOGGER.info("Le reçu de paiement n'a pas pu être recupéré pour la demande {}", identifiant);
        }
    }

    private void createInfoFacturation(GichuniUsagerDTO usager, CommandeBO commande, String raisonSociale, String langue) {
        // Stockage de l'info de facturation en base de donnée
        InfoFacturationResponseDTO result = getInfoFacturation(usager);
        result.setLangue(langue);
        if(null != raisonSociale) {
            result.setRaisonSociale(raisonSociale);
        }
        infoFacturationRepository
                .save(InfoFacturationTransformer.infoFacturationResponseDTOToInfoFacturationBO(result, commande));
    }

    private BigDecimal calculTotalCommande(List<Integer> demandeIds, Integer usagerId, Map<Integer, DemandeBO> demandes,
            Map<Integer, BigDecimal> totauxDemandes, Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes) {
        BigDecimal totalCommande = BigDecimal.ZERO;
        for (Integer pkDemande : demandeIds) {
            DemandeBO demandeBO = demandesRepository.findByPkDemandesAndUsagerId(pkDemande, usagerId);
            if (demandeBO == null) {
                throw new DemarchesServiceException(
                        "La demande " + pkDemande + " est introuvable pour l'usager id " + usagerId,
                        HttpStatus.NOT_FOUND);
            }
            demandes.put(pkDemande, demandeBO);
            var articlesDemande = montantService.getArticles(demandesTransformer.bo2Dto(demandeBO, new String[] {}));
            BigDecimal montantdemande = BigDecimal.ZERO;
            for (CommandeDemandeArticleBO article : articlesDemande) {
                BigDecimal montantArticle = BigDecimal.valueOf(article.getMontant());
                montantdemande = montantdemande.add(montantArticle);
            }
            articlesDemandes.put(pkDemande, articlesDemande);
            totauxDemandes.put(pkDemande, montantdemande);
            totalCommande = totalCommande.add(montantdemande);
        }

        return totalCommande;
    }

    private CommandeBO createCommande(BigDecimal totalCommande, MoyenPaiementBO moyenPaiement, List<Integer> demandeIds,
            Map<Integer, DemandeBO> demandes, Map<Integer, BigDecimal> totauxDemandes,
            Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes) {
        CommandeBO commande = new CommandeBO();
        commande.setDateCreation(LocalDateTime.now());
        commande.setMontantInitial(totalCommande.doubleValue());
        commande.setMontantRestant(totalCommande.doubleValue());
        commande.setMontantDejaCapture(0);
        commande.setMoyenPaiement(moyenPaiement);
        commandeRepository.save(commande);
        LOGGER.info("Created [ commande {}] ", commande);
        commande.setCommandesDemandes(
                createCommandesDemandes(commande, demandeIds, demandes, totauxDemandes, articlesDemandes));

        return commande;
    }

    /**
     * Méthode utilisée lorsque RESID rattrape leurs débits pour envoyer le mail de notifications aux agents que la demande est payée
     * Ils nous appellent pour stocker le reçu de paiement, on considère donc que le débit est OK, on envoie le mail aux agents
    **/
    @Override
    public void envoiMailAgent(String idTs) {
        DemandeDTO demande = demandesService.getDemande(String.valueOf(idTs));
        if (null != demande) {
            envoiMailAgent(demande, true);
        }
    }

    private List<CommandeDemandeBO> createCommandesDemandes(CommandeBO commande, List<Integer> demandeIds,
            Map<Integer, DemandeBO> demandes, Map<Integer, BigDecimal> totauxDemandes,
            Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes) {
        List<CommandeDemandeBO> commandesDemandes = new ArrayList<>();
        for (Integer pkDemande : demandeIds) {
            CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
            commandeDemande.setCommande(commande);
            commandeDemande.setDemande(demandes.get(pkDemande));
            commandeDemande.setMontant(totauxDemandes.get(pkDemande).doubleValue());
            commandeDemande.setCommandesDemandesArticles(new ArrayList<>());
            commandeDemande = commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);

            var articles = new ArrayList<CommandeDemandeArticleBO>();
            for (CommandeDemandeArticleBO articleBO : articlesDemandes.get(pkDemande)) {
                articleBO.setCommandeDemande(commandeDemande);
                articleBO = commandeDemandeArticleRepository.save(articleBO);
                LOGGER.info("Created [ commandeDemandeArticle {}] ", articleBO);
                articles.add(articleBO);
            }

            commandeDemande.setCommandesDemandesArticles(articles);
            commandeDemandeRepository.save(commandeDemande);
            commandesDemandes.add(commandeDemande);
            LOGGER.info("Updated [ commandeDemande {}] ", commande);
        }
        commandeRepository.save(commande);
        LOGGER.info("Updated [ commande {}] ", commande);
        return commandesDemandes;
    }

    public boolean isDebitDeclenche(Integer pkDemande) {
        List<CommandeOperationBO> allByFkDemandes = commandeOperationRepository.findAllByFkDemandes(pkDemande);
        return !allByFkDemandes.isEmpty();
    }

}
