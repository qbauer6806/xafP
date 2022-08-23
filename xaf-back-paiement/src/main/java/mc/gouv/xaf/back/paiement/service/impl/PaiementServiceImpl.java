package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.client.SecurityService;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.*;
import mc.gouv.xaf.back.paiement.dto.BillingDTO;
import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.stc.MoyenPaiementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
public class PaiementServiceImpl implements PaiementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementServiceImpl.class);

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PropertiesService propertiesService;

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


        // TODO TO REMOVE THIS
        // La langue est normalement récupérée du WYSI et permet d'initialiser l'iframe monetico en FR ou EN en fonction de la langue du formulaire FO.
        // Cependant, pour des tests utilisateurs, on fait pointer la callback retour monetico comme suit:
        // FR -> callback vers notre DEV
        // EN -> callback vers la REC
        // J'ai créé une propriété BO pour faire en sorte qu'en REC on puisse modifier le code langue sans que le formulaire FO soit en Anglais...
        // Oui, c'est nul. Mais voilà quoi. jpp

        PropertiesDTO codeLangue = propertiesService.getProperty("PERMC", "TEMP_CODE_LANGUE_MONETICO");

        // TODO END TO REMOVE THIS



        LOGGER.info("Parameters [ demandesId {}, langue {}, usagerId {} ] ", demandesId, langue, usagerId);
        String codeSociete = iframe ? paiementPropertiesResolver.getXafMoneticoCodeSiteIframe() : paiementPropertiesResolver.getCodeSiteStandard();
        PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
        double prix = Double.parseDouble(montantProperty.getValue());
        List<Integer> demandesIdList = Stream.of(demandesId.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
        double montant = 0;
        for (Integer demandeId : demandesIdList) {
            double montantdemande = montantService.getMontant(demandeId);
            montant += montantdemande;
        }

        LocalDateTime now = LocalDateTime.now();

        CommandeBO commande = new CommandeBO();
        commande.setDateCreation(now);
        commande.setMontant(montant);
        commande = commandeRepository.save(commande);
        LOGGER.info("Created [ commande {}] ", commande);

        StringJoiner listePkDemandes = new StringJoiner(",");

        for (Integer demandeId : demandesIdList) {
            CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
            commandeDemande.setCommande(commande);
            DemandeBO demandeBO = demandesRepository.findById(demandeId).orElseThrow(RuntimeException::new);
            listePkDemandes.add(demandeBO.getIdentifiant());
            commandeDemande.setDemande(demandeBO);
            commandeDemande.setMontant(prix);
            commandeDemande = commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);
        }


        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        moyenPaiement.setCommande(commande);
        moyenPaiement.setPkMoyenPaiement(referenceFactoryService.createSimpleReference12Digits());
        moyenPaiement.setDateLimite(now.plusDays(paiementPropertiesResolver.getValiditeMaxMoyenPaiement()));
        moyenPaiement.setDateDerniereModification(now);
        moyenPaiement.setMontantInitial(montant);
        moyenPaiement.setMontantRestant(montant);
        moyenPaiement.setMontantCapture(0);
        moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.EN_ATTENTE_DE_VALIDATION);
        moyenPaiement.setCodeSociete(codeSociete);

        moyenPaiement = moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
        PaiementDTO paiementDTO = new PaiementDTO(codeLangue.getValue());
        paiementDTO.setDate(securityService.dateFormat(new Date()));
        GichuniUsagerDTO usager = usagersCache.get(usagerId);

        BillingDTO billingDTO = new BillingDTO();
        billingDTO.setFirstName(usager.getPrenom());
        billingDTO.setLastName(usager.getNom());
        billingDTO.setAddressLine1(usager.getAdresse1() == null ? paiementPropertiesResolver.getAdresseParDefaut() : usager.getAdresse1());
        billingDTO.setCity(usager.getVille() == null ? paiementPropertiesResolver.getVilleParDefaut() : usager.getVille());
        billingDTO.setPostalCode(usager.getCodePostal() == null ? paiementPropertiesResolver.getCodePostalParDefaut() : usager.getCodePostal());
        billingDTO.setCountry(usager.getPaysCode() == null ? paiementPropertiesResolver.getCodePaysParDefaut() : usager.getPaysCode());

        ContexteCommandeDTO contexteCommandeDTO = new ContexteCommandeDTO();
        contexteCommandeDTO.setBilling(billingDTO);

        paiementDTO.setContexte_commande(securityService.contexteCommandeDTOtoBase64(contexteCommandeDTO));
        String date = securityService.dateFormat(new Date());
        paiementDTO.setDate(date);
        paiementDTO.setThreeDSecureChallenge(paiementPropertiesResolver.getXafMonetico3dsv2Scenario());
        paiementDTO.setMontant(montant + paiementPropertiesResolver.getCurrency());
        paiementDTO.setReference(moyenPaiement.getPkMoyenPaiement());


        paiementDTO.setMail(usager.getEmail());
        if (iframe) {
            paiementDTO.setMode_affichage("iframe");
        }
        paiementDTO.setSociete(codeSociete);
        paiementDTO.setTPE(paiementPropertiesResolver.getTpe());

        paiementDTO.setTexteLibre(paiementPropertiesResolver.getXafMoneticoTexteAller() + " - " + date+" - demandes ["+listePkDemandes+"]");

        paiementDTO.setUrlRetourErr(paiementPropertiesResolver.getEchecUrl());
        paiementDTO.setUrlRetourOk(paiementPropertiesResolver.getSuccesUrl());
        paiementDTO.setVersion(paiementPropertiesResolver.getVersionAller());
        paiementDTO.setMAC(securityService.getHmacString(paiementDTO));

        LOGGER.info("Return [ paiementDTO {}] ", paiementDTO);
        return paiementDTO;
    }

    @Override
    public void updateStatus(MoyenPaiementDTO moyenPaiementDTO) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementDTO {}] ", moyenPaiementDTO);
        MoyenPaiementBO moyenPaiement = moyenPaiementRepository.findById(moyenPaiementDTO.getReference()).get();
        String status = moyenPaiementDTO.getCodeRetour();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMyy");
        YearMonth yeaMonthValidite = YearMonth.parse(moyenPaiementDTO.getVld(), formatter);
        LocalDateTime dateValidite = LocalDateTime.of(yeaMonthValidite.getYear(), yeaMonthValidite.getMonth(), yeaMonthValidite.getMonth().length(yeaMonthValidite.isLeapYear()), 0, 0);
        if (dateValidite.isBefore(moyenPaiement.getDateLimite())) {
            LOGGER.info("Changement date limite moyen paiement [ dateValidite {}] ", dateValidite);
            moyenPaiement.setDateLimite(dateValidite);
        } else {
            dateValidite = moyenPaiement.getDateLimite();
        }

        if (status.equals("payetest") || status.equals("paiement")) {
            moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.VALIDE);
            List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByCommande_PkCommande(moyenPaiement.getCommande().getPkCommande());
            updateDemandeData(commandeDemandeBOList, dateValidite, moyenPaiementDTO);
        } else {
            moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.INVALIDE);
        }

        moyenPaiement.setAuthentification(moyenPaiementDTO.getAuthentification());
        moyenPaiement.setModepaiement(moyenPaiementDTO.getModepaiement());
        moyenPaiement.setOriginetr(moyenPaiementDTO.getOriginetr());
        moyenPaiement.setIpclient(moyenPaiementDTO.getIpclient());
        moyenPaiement.setHpancb(moyenPaiementDTO.getHpancb());
        moyenPaiement.setBincb(moyenPaiementDTO.getBincb());
        moyenPaiement.setOriginecb(moyenPaiementDTO.getOriginecb());
        moyenPaiement.setCbmasquee(moyenPaiementDTO.getCbmasquee());
        moyenPaiement.setEcard(moyenPaiementDTO.getEcard());
        moyenPaiement.setTypecompte(moyenPaiementDTO.getTypecompte());
        moyenPaiement.setUsage(moyenPaiementDTO.getUsage());
        moyenPaiement.setNumauto(moyenPaiementDTO.getNumauto());
        moyenPaiement.setBrand(moyenPaiementDTO.getBrand());
        moyenPaiement.setVld(moyenPaiementDTO.getVld());
        moyenPaiement.setCvx(moyenPaiementDTO.getCvx());

        moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
    }

    @Override
    public Optional<MoyenPaiementBO> getMoyenPaiement(Integer demandeId) {
        logStartMethod(LOGGER);
        DemandeDataDTO data = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeId, PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name());
        return moyenPaiementRepository.findById(data.getValue());
    }

    @Async
    void updateDemandeData(List<CommandeDemandeBO> commandeDemandeBOList, LocalDateTime dateValidite, MoyenPaiementDTO moyenPaiement) {
        new Thread(() -> {
            Timestamp date = Timestamp.valueOf(LocalDateTime.now());
            for (CommandeDemandeBO commandeDemandeBO : commandeDemandeBOList) {
                DemandeBO demandeBO = commandeDemandeBO.getDemande();
                Integer pkDemande = demandeBO.getPkDemandes();
                Integer usagerId = demandeBO.getFkAccess().getUsagerId();
                GouvBPMUser user = new GouvBPMUser();
                user.setId(usagerId.toString());

                LOGGER.info("Mise à jour des données de la demande...");
                String demarcheId = gouvPropertiesResolver.getDemarcheId();
                Map<String, String> datas = new HashMap<>();
                datas.put(PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name(), LocalDateTime.now().format(DTF_AAAA_MM_JJ));
                datas.put(PaiementDemandeDataKeysEnum.DATE_EXPIRATION_EMPREINTE.name(), dateValidite.format(DTF_AAAA_MM_JJ));
                datas.put(PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.EMPREINTE_VALIDE.name());
                datas.put(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT.name(), moyenPaiement.getModepaiement());
                datas.put(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name(), moyenPaiement.getReference());
                demandesDataService.saveOrUpdateDemandeDatas(demarcheId, pkDemande, datas);

                LOGGER.info("Ajout de l'historique de paiement...");
                PaiementHistoriqueBO historique = new PaiementHistoriqueBO();
                historique.setFkDemande(demandeBO);
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
                    throw new RuntimeException(e1);
                }
            }
        }).start();
    }
}
