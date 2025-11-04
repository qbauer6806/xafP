package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;
import static mc.gouv.xaf.back.service.utils.AfBackUtils.DTF_AAAA_MM_JJ;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
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
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeOperationTransformer;
import mc.gouv.xaf.back.paiement.dto.BillingDTO;
import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import mc.gouv.xaf.back.paiement.service.itg.MoneticoPaiementService;
import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MoneticoPaiementServiceImpl implements MoneticoPaiementService {

    public static final String UPDATE_PAIEMENT_DATA_THREAD = "THREAD_UPDATE_PAIEMENT_DATA_REF_";
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneticoPaiementServiceImpl.class);
    private static final String CODE_RETOUR_OK = "0";
    private static final String CODE_RETOUR_KO = "1";
    private static final int TAILLE_MAX_NOMS = 45;
    private static final int TAILLE_MAX_OBJETS = 50;
    private static final int TAILLE_MAX_CODE_POSTAL = 10;

    private static final String EN_COURS_PAIEMENT_STATUT_KEY = "EN_COURS_PAIEMENT";

    private final CommandeRepository commandeRepository;

    private final MoyenPaiementRepository moyenPaiementRepository;

    private final CommandeOperationRepository commandeOperationRepository;

    private final CommandeDemandeRepository commandeDemandeRepository;

    private final CommandesDemandesService commandesDemandesService;

    private final CommandeDemandeArticleRepository commandeDemandeArticleRepository;

    private final DemandesRepository demandesRepository;

    private final PaiementHistoriqueRepository paiementHistoriqueRepository;

    private final PaiementSecurityService paiementSecurityService;

    private final ReferenceFactoryService referenceFactoryService;

    private final UsagersCache usagersCache;

    private final PaiementPropertiesResolver paiementPropertiesResolver;

    private final MontantService montantService;

    private final GouvBPM gouvBPM;

    private final DemandesDataService demandesDataService;

    private final DemandesStatutsService demandesStatutsService;

    private final DemandesTransformer demandesTransformer;

    @Override
    public PaiementDTO create(String demandesId, String langue, Integer usagerId, boolean iframe) {
        logStartMethod(LOGGER);
        String safeDemandeId = AfBackUtils.logSafe(demandesId);
        String safeLangue = AfBackUtils.logSafe(langue);
        LOGGER.info("Parameters [ demandesId {}, langue {}, usagerId {} ] ", safeDemandeId, safeLangue, usagerId);

        String codeSociete = iframe
                ? paiementPropertiesResolver.getXafMoneticoCodeSiteIframe()
                : paiementPropertiesResolver.getCodeSiteStandard();
        List<Integer> demandesIdList = Stream.of(demandesId.split(",")).map(String::trim).map(Integer::parseInt)
                .toList();
        StringJoiner listeIdentifiantsDemandes = new StringJoiner(",");
        BigDecimal totalCommande = BigDecimal.ZERO;
        Map<Integer, BigDecimal> totauxDemandes = new HashMap<>();
        Map<Integer, DemandeBO> demandes = new HashMap<>();
        Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes = new HashMap<>();

        for (Integer demandeId : demandesIdList) {
            DemandeBO demandeBO = demandesRepository.findByFkAccess_UsagerIdAndPkDemandesAndFkAccess_ActiveTrue(
                    usagerId, demandeId);
            if (demandeBO == null) {
                throw new DemarchesServiceException(
                        "La demande " + demandeId + " est introuvable pour l'usager id " + usagerId,
                        HttpStatus.NOT_FOUND);
            }
            demandes.put(demandeId, demandeBO);
            listeIdentifiantsDemandes.add(demandeBO.getIdentifiant());

            // TODO Changer le moyen de récupérer le statut d'un paiement
            DemandeDataDTO data = demandesDataService.getDemandeData(demandeId,
                    PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());
            if (data != null && StringUtils.equals(data.getValue(), PaiementStatutEnum.EMPREINTE_VALIDE.name())) {
                throw new DemarchesServiceException(
                        "La demande " + demandeId + " a déjà une empreinte bancaire valide.", HttpStatus.CONFLICT);
            }

            var articlesDemande = montantService.getArticles(demandesTransformer.bo2Dto(demandeBO, new String[] {}));
            BigDecimal montantdemande = BigDecimal.ZERO;
            for (CommandeDemandeArticleBO article : articlesDemande) {
                BigDecimal montantArticle = BigDecimal.valueOf(article.getMontant());
                montantdemande = montantdemande.add(montantArticle);
            }
            articlesDemandes.put(demandeId, articlesDemande);
            totauxDemandes.put(demandeId, montantdemande);
            totalCommande = totalCommande.add(montantdemande);
        }

        LocalDateTime now = LocalDateTime.now();

        CommandeBO commande = new CommandeBO();
        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        moyenPaiement.setPkMoyensPaiements(referenceFactoryService.createSimpleReference12Digits());
        commande.setDateCreation(now);
        commande.setMontantInitial(totalCommande.doubleValue());
        commande.setMontantRestant(totalCommande.doubleValue());
        commande.setMontantDejaCapture(0);
        commande.setMoyenPaiement(moyenPaiement);
        commandeRepository.save(commande);
        LOGGER.info("Created [ commande {}] ", commande);

        commande.setCommandesDemandes(new ArrayList<>());
        for (Integer demandeId : demandesIdList) {
            CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
            commandeDemande.setCommande(commande);
            commandeDemande.setDemande(demandes.get(demandeId));
            commandeDemande.setMontant(totauxDemandes.get(demandeId).doubleValue());
            commandeDemande.setCommandesDemandesArticles(new ArrayList<>());
            commandeDemande = commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);

            var articles = new ArrayList<CommandeDemandeArticleBO>();
            for (CommandeDemandeArticleBO articleBO : articlesDemandes.get(demandeId)) {
                articleBO.setCommandeDemande(commandeDemande);
                articleBO = commandeDemandeArticleRepository.save(articleBO);
                LOGGER.info("Created [ commandeDemandeArticle {}] ", articleBO);
                articles.add(articleBO);
            }

            commandeDemande.setCommandesDemandesArticles(articles);
            commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Updated [ commandeDemande {}] ", commande);
            commande.getCommandesDemandes().add(commandeDemande);
        }

        commandeRepository.save(commande);
        LOGGER.info("Updated [ commande {}] ", commande);

        moyenPaiement.setCommande(commande);
        // TODO moyenPaiement.setDateLimite(now.plusDays(paiementPropertiesResolver.getValiditeMaxMoyenPaiement()));
        moyenPaiement.setDateDerniereModification(now);
        moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutEnum.EN_ATTENTE_DE_VALIDATION);
        // TODO moyenPaiement.setCodeSociete(codeSociete);
        // TODO moyenPaiement.setLangue(langue);

        PaiementDTO paiementDTO = new PaiementDTO(langue);
        paiementDTO.setDate(paiementSecurityService.dateFormat(new Date()));
        GichuniUsagerDTO usager = usagersCache.get(usagerId);

        ContexteCommandeDTO contexteCommandeDTO = new ContexteCommandeDTO();
        contexteCommandeDTO.setBilling(createBillingDTO(usager));

        paiementDTO.setContexte_commande(paiementSecurityService.contexteCommandeDTOtoBase64(contexteCommandeDTO));
        String date = paiementSecurityService.dateFormat(new Date());
        paiementDTO.setDate(date);
        paiementDTO.setThreeDSecureChallenge(paiementPropertiesResolver.getXafMonetico3dsv2Scenario());
        paiementDTO.setMontant(totalCommande + paiementPropertiesResolver.getCurrency());
        paiementDTO.setReference(moyenPaiement.getPkMoyensPaiements());
        paiementDTO.setMail(usager.getEmail());
        if (iframe) {
            paiementDTO.setMode_affichage("iframe");
        }
        paiementDTO.setSociete(codeSociete);
        paiementDTO.setTPE(paiementPropertiesResolver.getTpe());
        paiementDTO.setTexteLibre(paiementPropertiesResolver.getXafMoneticoTexteAller() + date + " - demandes ["
                + listeIdentifiantsDemandes + "]");
        paiementDTO.setUrlRetourErr(paiementPropertiesResolver.getEchecUrl());
        paiementDTO.setUrlRetourOk(paiementPropertiesResolver.getSuccesUrl());
        paiementDTO.setVersion(paiementPropertiesResolver.getVersionAller());
        paiementDTO.setLibelleMonetique(paiementPropertiesResolver.getXafMoneticoLibelleSociete());
        paiementDTO.setLibelleMonetiqueLocalite(paiementPropertiesResolver.getXafMoneticoLibelleLieu());

        // Création d'une clé MAC
        String mac = paiementSecurityService.getHmacStringInterfaceAller(paiementDTO);
        paiementDTO.setMAC(mac);
        // TODO moyenPaiement.setMac(mac);
        moyenPaiement = moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
        return paiementDTO;
    }

    private String couperSiTropGrand(String str, int max) {
        if (str.length() > max) {
            str = str.substring(0, max);
        }
        return str;
    }

    private BillingDTO createBillingDTO(GichuniUsagerDTO usager) {
        BillingDTO billingDTO = new BillingDTO();
        String prenom =
                usager.getPrenom() == null ? paiementPropertiesResolver.getPrenomParDefaut() : usager.getPrenom();
        billingDTO.setFirstName(couperSiTropGrand(prenom, TAILLE_MAX_NOMS));
        String nom = usager.getNom() == null ? paiementPropertiesResolver.getNomParDefaut() : usager.getNom();
        billingDTO.setLastName(couperSiTropGrand(nom, TAILLE_MAX_NOMS));
        String adresse1 =
                usager.getAdresse1() == null ? paiementPropertiesResolver.getAdresseParDefaut() : usager.getAdresse1();
        billingDTO.setAddressLine1(couperSiTropGrand(adresse1, TAILLE_MAX_OBJETS));
        String adresse2 = usager.getAdresse2();
        if (StringUtils.isNotEmpty(adresse2)) {
            billingDTO.setAddressLine2(couperSiTropGrand(adresse2, TAILLE_MAX_OBJETS));
        }
        String adresse3 = usager.getComplementAdresse();
        if (StringUtils.isNotEmpty(adresse3)) {
            billingDTO.setAddressLine3(couperSiTropGrand(adresse3, TAILLE_MAX_OBJETS));
        }
        String ville = usager.getVille() == null ? paiementPropertiesResolver.getVilleParDefaut() : usager.getVille();
        billingDTO.setCity(couperSiTropGrand(ville, TAILLE_MAX_OBJETS));
        billingDTO.setPostalCode(usager.getCodePostal() == null
                ? paiementPropertiesResolver.getCodePostalParDefaut()
                : couperSiTropGrand(usager.getCodePostal(), TAILLE_MAX_CODE_POSTAL));
        billingDTO.setCountry(usager.getPaysCode() == null
                ? paiementPropertiesResolver.getCodePaysParDefaut()
                : usager.getPaysCode());
        return billingDTO;
    }

    @Override
    public String updateStatus(MoneticoResponseDTO moneticoResponseDTO) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moneticoResponseDTO {}] ", moneticoResponseDTO);

        LOGGER.info("Vérification de la clé HMAC...");
        String mac = paiementSecurityService.getHmacStringInterfaceRetour(moneticoResponseDTO);
        if (!StringUtils.equals(moneticoResponseDTO.getMac(), mac)) {
            return CODE_RETOUR_KO;
        }

        String reference = moneticoResponseDTO.getReference();
        String safeReference = AfBackUtils.logSafe(reference);
        LOGGER.info("Récupération en BDD des informations de paiement avec la référence {}", safeReference);
        MoyenPaiementBO moyenPaiementBO = moyenPaiementRepository.findById(reference).orElseThrow(
                () -> new DemarchesServiceException(
                        "Aucun paiement portant la référence " + reference + " n'a été trouvé.", HttpStatus.NOT_FOUND));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMyy");
        YearMonth yeaMonthValidite = YearMonth.parse(moneticoResponseDTO.getVld(), formatter);
        LocalDateTime dateValidite = LocalDateTime.of(yeaMonthValidite.getYear(), yeaMonthValidite.getMonth(),
                yeaMonthValidite.getMonth().length(yeaMonthValidite.isLeapYear()), 0, 0);
        /* TODO if (dateValidite.isBefore(moyenPaiementBO.getDateLimite())) {
            LOGGER.info("Changement date limite moyen paiement [ dateValidite {}] ", dateValidite);
            moyenPaiementBO.setDateLimite(dateValidite);
        } else {
            dateValidite = moyenPaiementBO.getDateLimite();
        }*/

        if (moneticoResponseDTO.isCoderetourValid()) {
            moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.VALIDE);
            List<DemandeBO> demandes = commandesDemandesService.getDemandesFromCommande(
                    moyenPaiementBO.getCommande().getPkCommandes());
            demandesStatutsService.updateMultipleStatuts(demandes, EN_COURS_PAIEMENT_STATUT_KEY);
            updateDemandeData(demandes, dateValidite, moneticoResponseDTO);
        } else {
            moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.INVALIDE);
        }
        /* TODO
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
        */

        moyenPaiementRepository.save(moyenPaiementBO);
        LOGGER.info("Created [ moyenPaiementBO {}] ", moyenPaiementBO);
        return CODE_RETOUR_OK;
    }

    @Override
    public List<CommandeOperationDTO> getAllOperations() {
        List<CommandeOperationBO> commandeOperationBos = commandeOperationRepository.findAll();
        return CommandeOperationTransformer.bos2DtosMonetico(commandeOperationBos);
    }

    // TODO sauvegarder le statut du paiement de manière plus correct que dans les demandes data
    @Async
    void updateDemandeData(List<DemandeBO> demandes, LocalDateTime dateValidite,
            MoneticoResponseDTO moneticoResponseDTO) {
        Thread t = new Thread(() -> {
            Timestamp date = Timestamp.valueOf(LocalDateTime.now());
            for (DemandeBO demande : demandes) {
                Integer usagerId =demande.getUsager().getId();
                Integer pkDemande = demande.getPkDemandes();
                GouvBPMUser user = new GouvBPMUser();
                user.setId(usagerId.toString());

                LOGGER.info("========== Mise à jour des données de la demande {}...", pkDemande);
                Map<String, String> datas = new HashMap<>();
                datas.put(PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name(), LocalDateTime.now().format(DTF_AAAA_MM_JJ));
                datas.put(PaiementDemandeDataKeysEnum.DATE_EXPIRATION_EMPREINTE.name(),
                        dateValidite.format(DTF_AAAA_MM_JJ));
                datas.put(PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(),
                        PaiementStatutEnum.EMPREINTE_VALIDE.name());
                datas.put(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT.name(), moneticoResponseDTO.getModepaiement());
                datas.put(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name(),
                        moneticoResponseDTO.getReference());
                demandesDataService.saveOrUpdateDemandeDatas(pkDemande, datas);

                LOGGER.info("Ajout de l'historique de paiement...");
                PaiementHistoriqueBO historique = new PaiementHistoriqueBO();
                historique.setFkDemandes(demande);
                DemandesUsagersBO usager = demande.getUsager();
                if (usager != null) {
                    historique.setContenu("Usager " + usager.getPrenom() + " " + usager.getNom()
                            + " : Effectue une empreinte bancaire");
                }
                historique.setStatut(PaiementStatutEnum.EMPREINTE_VALIDE.name());
                historique.setDate(date);
                historique.setUsagerId(usagerId);
                paiementHistoriqueRepository.save(historique);

                LOGGER.info("Progression dans le BPM...");
                Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(pkDemande);
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(),
                        usagerId.toString());
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), null);
                gouvBPM.setProcessBusinessVariables(pkDemande, variables);

                GouvBPMTask task = gouvBPM.getActiveTasksForDemande(pkDemande).getFirst();
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
