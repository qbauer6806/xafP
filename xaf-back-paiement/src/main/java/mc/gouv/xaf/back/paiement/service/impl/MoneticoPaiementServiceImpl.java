package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.*;
import mc.gouv.xaf.back.paiement.data.entity.*;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeOperationTransformer;
import mc.gouv.xaf.back.paiement.dto.*;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.paiement.service.itg.MoneticoPaiementService;
import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;
import static mc.gouv.xaf.back.service.utils.AfBackUtils.DTF_AAAA_MM_JJ;

@Service
public class MoneticoPaiementServiceImpl implements MoneticoPaiementService {
    public static final String UPDATE_PAIEMENT_DATA_THREAD = "THREAD_UPDATE_PAIEMENT_DATA_REF_";
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneticoPaiementServiceImpl.class);
    private static final String CODE_RETOUR_OK = "0";
    private static final String CODE_RETOUR_KO = "1";

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private PaiementSecurityService paiementSecurityService;

    @Autowired
    private ReferenceFactoryService referenceFactoryService;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private PaiementPropertiesResolver paiementPropertiesResolver;

    @Autowired
    private MontantService montantService;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public PaiementDTO create(String demandesId, String langue, Integer usagerId, boolean iframe) {
        logStartMethod(LOGGER);
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        LOGGER.info("Parameters [ demandesId {}, langue {}, usagerId {} ] ", demandesId, langue, usagerId);
        String codeSociete = iframe ? paiementPropertiesResolver.getXafMoneticoCodeSiteIframe() : paiementPropertiesResolver.getCodeSiteStandard();
        List<Integer> demandesIdList = Stream.of(demandesId.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
        HashMap<Integer, BigDecimal> montants = new HashMap<>();
        HashMap<Integer, DemandeBO> demandes = new HashMap<>();
        StringJoiner listePkDemandes = new StringJoiner(",");
        BigDecimal montant = BigDecimal.ZERO;
        for (Integer demandeId : demandesIdList) {
            Optional<DemandeBO> demandeBOOptional = demandesRepository.findById(demandeId);
            if (!demandeBOOptional.isPresent()) {
                throw new DemarchesServiceException("La demande " + demandeId + " est introuvable.", HttpStatus.NOT_FOUND);
            }
            DemandeBO demandeBO = demandeBOOptional.get();
            demandes.put(demandeId, demandeBO);
            listePkDemandes.add(demandeBO.getIdentifiant());

            // TODO Changer le moyen de récupérer le statut d'un paiement
            DemandeDataDTO data = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());
            if (data != null && StringUtils.equals(data.getValue(), PaiementStatutEnum.EMPREINTE_VALIDE.name())) {
                throw new DemarchesServiceException("La demande " + demandeId + " a déjà une empreinte bancaire valide.", HttpStatus.CONFLICT);
            }

            BigDecimal montantdemande = montantService.getMontant(DemandesTransformer.bo2Dto(demandeBO, new String[]{}));
            montants.put(demandeId, montantdemande);
            montant = montant.add(montantdemande);
        }

        LocalDateTime now = LocalDateTime.now();

        CommandeBO commande = new CommandeBO();
        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        moyenPaiement.setPkMoyensPaiements(referenceFactoryService.createSimpleReference12Digits());
        commande.setDateCreation(now);
        commande.setMontantInitial(montant.doubleValue());
        commande.setMontantRestant(montant.doubleValue());
        commande.setMontantDejaCapture(0);
        commande.setMoyenPaiement(moyenPaiement);
        commandeRepository.save(commande);
        LOGGER.info("Created [ commande {}] ", commande);

        for (Integer demandeId : demandesIdList) {
            CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
            commandeDemande.setCommande(commande);
            commandeDemande.setDemande(demandes.get(demandeId));
            commandeDemande.setMontant(montants.get(demandeId).doubleValue());
            commandeDemande = commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);

            if (commande.getCommandesDemandes() != null) {
                commande.getCommandesDemandes().add(commandeDemande);
            } else {
                List<CommandeDemandeBO> commandeDemandeBOList = new ArrayList<>();
                commandeDemandeBOList.add(commandeDemande);
                commande.setCommandesDemandes(commandeDemandeBOList);
            }
        }

        commandeRepository.save(commande);
        LOGGER.info("Updated [ commande {}] ", commande);

        moyenPaiement.setCommande(commande);
        moyenPaiement.setDateLimite(now.plusDays(paiementPropertiesResolver.getValiditeMaxMoyenPaiement()));
        moyenPaiement.setDateDerniereModification(now);
        moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutEnum.EN_ATTENTE_DE_VALIDATION);
        moyenPaiement.setCodeSociete(codeSociete);
        moyenPaiement.setLangue(langue);

        PaiementDTO paiementDTO = new PaiementDTO(langue);
        paiementDTO.setDate(paiementSecurityService.dateFormat(new Date()));
        GichuniUsagerDTO usager = usagersCache.get(usagerId);

        BillingDTO billingDTO = new BillingDTO();
        billingDTO.setFirstName(usager.getPrenom() == null ? paiementPropertiesResolver.getPrenomParDefaut() : usager.getPrenom());
        billingDTO.setLastName(usager.getNom() == null ? paiementPropertiesResolver.getNomParDefaut() : usager.getNom());
        billingDTO.setAddressLine1(usager.getAdresse1() == null ? paiementPropertiesResolver.getAdresseParDefaut() : usager.getAdresse1());
        billingDTO.setCity(usager.getVille() == null ? paiementPropertiesResolver.getVilleParDefaut() : usager.getVille());
        billingDTO.setPostalCode(usager.getCodePostal() == null ? paiementPropertiesResolver.getCodePostalParDefaut() : usager.getCodePostal());
        billingDTO.setCountry(usager.getPaysCode() == null ? paiementPropertiesResolver.getCodePaysParDefaut() : usager.getPaysCode());

        ContexteCommandeDTO contexteCommandeDTO = new ContexteCommandeDTO();
        contexteCommandeDTO.setBilling(billingDTO);

        paiementDTO.setContexte_commande(paiementSecurityService.contexteCommandeDTOtoBase64(contexteCommandeDTO));
        String date = paiementSecurityService.dateFormat(new Date());
        paiementDTO.setDate(date);
        paiementDTO.setThreeDSecureChallenge(paiementPropertiesResolver.getXafMonetico3dsv2Scenario());
        paiementDTO.setMontant(montant + paiementPropertiesResolver.getCurrency());
        paiementDTO.setReference(moyenPaiement.getPkMoyensPaiements());
        paiementDTO.setMail(usager.getEmail());
        if (iframe) {
            paiementDTO.setMode_affichage("iframe");
        }
        paiementDTO.setSociete(codeSociete);
        paiementDTO.setTPE(paiementPropertiesResolver.getTpe());
        paiementDTO.setTexteLibre(paiementPropertiesResolver.getXafMoneticoTexteAller() + date + " - demandes [" + listePkDemandes + "]");
        paiementDTO.setUrlRetourErr(paiementPropertiesResolver.getEchecUrl());
        paiementDTO.setUrlRetourOk(paiementPropertiesResolver.getSuccesUrl());
        paiementDTO.setVersion(paiementPropertiesResolver.getVersionAller());

        // Création d'une clé MAC
        String mac = paiementSecurityService.getHmacStringInterfaceAller(paiementDTO);
        paiementDTO.setMAC(mac);
        moyenPaiement.setMac(mac);
        moyenPaiement = moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);

        LOGGER.info("Return [ paiementDTO {}] ", paiementDTO);
        return paiementDTO;
    }

    @Override
    public String updateStatus(MoneticoResponseDTO moneticoResponseDTO) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moneticoResponseDTO {}] ", moneticoResponseDTO);

        LOGGER.info("Vérification de la clé HMAC...");
        String mac = paiementSecurityService.getHmacStringInterfaceRetour(moneticoResponseDTO);
        if (!StringUtils.equals(moneticoResponseDTO.getMac(), mac)) {
            return CODE_RETOUR_KO + '\n' + mac;
        }

        String reference = moneticoResponseDTO.getReference();
        LOGGER.info("Récupération en BDD des informations de paiement avec la référence {}", reference);
        Optional<MoyenPaiementBO> moyenPaiementBOOptional = moyenPaiementRepository.findById(reference);
        if (!moyenPaiementBOOptional.isPresent()) {
            throw new DemarchesServiceException("Aucun paiement portant la référence " + reference + " n'a été trouvé.", HttpStatus.NOT_FOUND);
        }
        MoyenPaiementBO moyenPaiementBO = moyenPaiementBOOptional.get();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMyy");
        YearMonth yeaMonthValidite = YearMonth.parse(moneticoResponseDTO.getVld(), formatter);
        LocalDateTime dateValidite = LocalDateTime.of(yeaMonthValidite.getYear(), yeaMonthValidite.getMonth(), yeaMonthValidite.getMonth().length(yeaMonthValidite.isLeapYear()), 0, 0);
        if (dateValidite.isBefore(moyenPaiementBO.getDateLimite())) {
            LOGGER.info("Changement date limite moyen paiement [ dateValidite {}] ", dateValidite);
            moyenPaiementBO.setDateLimite(dateValidite);
        } else {
            dateValidite = moyenPaiementBO.getDateLimite();
        }

        String status = moneticoResponseDTO.getCodeRetour();
        if (status.equals("payetest") || status.equals("paiement")) {
            moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.VALIDE);
            List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByCommande_PkCommandes(moyenPaiementBO.getCommande().getPkCommandes());
            updateDemandeData(commandeDemandeBOList, dateValidite, moneticoResponseDTO);
        } else {
            moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.INVALIDE);
        }

        moyenPaiementBO.setAuthentification(moneticoResponseDTO.getAuthentification());
        moyenPaiementBO.setModepaiement(moneticoResponseDTO.getModepaiement());
        moyenPaiementBO.setOriginetr(moneticoResponseDTO.getOriginetr());
        moyenPaiementBO.setIpclient(moneticoResponseDTO.getIpclient());
        moyenPaiementBO.setHpancb(moneticoResponseDTO.getHpancb());
        moyenPaiementBO.setBincb(moneticoResponseDTO.getBincb());
        moyenPaiementBO.setOriginecb(moneticoResponseDTO.getOriginecb());
        moyenPaiementBO.setCbmasquee(moneticoResponseDTO.getCbmasquee());
        moyenPaiementBO.setEcard(moneticoResponseDTO.getEcard());
        moyenPaiementBO.setTypecompte(moneticoResponseDTO.getTypecompte());
        moyenPaiementBO.setUsage(moneticoResponseDTO.getUsage());
        moyenPaiementBO.setNumauto(moneticoResponseDTO.getNumauto());
        moyenPaiementBO.setBrand(moneticoResponseDTO.getBrand());
        moyenPaiementBO.setVld(moneticoResponseDTO.getVld());
        moyenPaiementBO.setCvx(moneticoResponseDTO.getCvx());

        moyenPaiementRepository.save(moyenPaiementBO);
        LOGGER.info("Created [ moyenPaiementBO {}] ", moyenPaiementBO);
        return CODE_RETOUR_OK;
    }

    @Override
    public List<CommandeOperationDTO> getAllOperations() {
        List<CommandeOperationBO> commandeOperationBos = commandeOperationRepository.findAll();
        return CommandeOperationTransformer.bos2Dtos(commandeOperationBos);
    }

    // TODO sauvegarder le statut du paiement de manière plus correct que dans les demandes data
    @Async
    void updateDemandeData(List<CommandeDemandeBO> commandeDemandeBOList, LocalDateTime dateValidite, MoneticoResponseDTO moneticoResponseDTO) {
        Thread t = new Thread(() -> {
            Timestamp date = Timestamp.valueOf(LocalDateTime.now());
            for (CommandeDemandeBO commandeDemandeBO : commandeDemandeBOList) {
                DemandeBO demandeBO = commandeDemandeBO.getDemande();

                Integer pkDemande = demandeBO.getPkDemandes();
                Integer usagerId = demandeBO.getFkAccess().getUsagerId();
                GouvBPMUser user = new GouvBPMUser();
                user.setId(usagerId.toString());

                LOGGER.info("========== Mise à jour des données de la demande {}...", pkDemande);
                String demarcheId = gouvPropertiesResolver.getDemarcheId();
                Map<String, String> datas = new HashMap<>();
                datas.put(PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name(), LocalDateTime.now().format(DTF_AAAA_MM_JJ));
                datas.put(PaiementDemandeDataKeysEnum.DATE_EXPIRATION_EMPREINTE.name(), dateValidite.format(DTF_AAAA_MM_JJ));
                datas.put(PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.EMPREINTE_VALIDE.name());
                datas.put(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT.name(), moneticoResponseDTO.getModepaiement());
                datas.put(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name(), moneticoResponseDTO.getReference());
                demandesDataService.saveOrUpdateDemandeDatas(demarcheId, pkDemande, datas);

                LOGGER.info("Ajout de l'historique de paiement...");
                PaiementHistoriqueBO historique = new PaiementHistoriqueBO();
                historique.setFkDemandes(demandeBO);
                historique.setContenu("Usager " + demandeBO.getUsagerPrenom() + " " + demandeBO.getUsagerNom() + " : Effectue une empreinte bancaire");
                historique.setStatut(PaiementStatutEnum.EMPREINTE_VALIDE.name());
                historique.setDate(date);
                historique.setUsagerId(demandeBO.getFkAccess().getUsagerId());
                paiementHistoriqueRepository.save(historique);

                LOGGER.info("Progression dans le BPM...");
                Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(pkDemande);
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), usagerId.toString());
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), null);
                gouvBPM.setProcessBusinessVariables(pkDemande, variables);

                GouvBPMTask task = gouvBPM.getActiveTasksForDemande(pkDemande).get(0);
                try {
                    gouvBPM.claimTask(task, user);
                    gouvBPM.completeTask(task, pkDemande);
                } catch (Exception e1) {
                    LOGGER.error("Erreur lors du claim et de la complétion de la tache du paiement");
                    throw new DemarchesServiceException(e1.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        });
        t.setName(UPDATE_PAIEMENT_DATA_THREAD + moneticoResponseDTO.getReference());
        t.start();
    }
}
