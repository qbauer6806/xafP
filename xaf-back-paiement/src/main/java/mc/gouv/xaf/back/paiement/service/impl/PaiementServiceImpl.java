package mc.gouv.xaf.back.paiement.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeArticleRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.InformationFacturationRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.transformer.InfoFacturationTransformer;
import mc.gouv.xaf.back.paiement.data.transformer.MoyenPaiementTransformer;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.paiement.service.TableauPaiementService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.AdresseDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.VousDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mc.gouv.xaf.shared.paiement.enums.PSPEnum;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import org.springframework.transaction.annotation.Transactional;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
@Transactional(rollbackFor = Exception.class)
public class PaiementServiceImpl implements PaiementService {


    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementServiceImpl.class);

    @Autowired
    private TableauPaiementService tableauPaiementService;

    @Autowired
    private BrouillonsService brouillonsService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeArticleRepository commandeDemandeArticleRepository;

    @Autowired
    private InformationFacturationRepository infoFacturationRepository;

    @Autowired
    private MontantService montantService;

    @Autowired
    private DemandesTransformer demandesTransformer;

    @Override
    public List<TableauDTO> getTableauPaiement(String ids, String objectType, Integer usagerId) {
        List<TableauDTO> result = new ArrayList<>();
        List<String> idsList = Arrays.asList(ids.replace("[", "").replace("]", "").split(","));
        if (objectType.equals(RequestConstant.BROUILLONS_PATH)) {
            for (String currentId : idsList) {
                // On va chercher l'objet dans l'implémentation de TableauPaiementService propre à chaque TS
                BrouillonDTO brouillon = brouillonsService.getBrouillon(Integer.valueOf(currentId), usagerId);
                JsonNode contenu = brouillon.getContenu();
                TableauDTO itemTableauPaiement = tableauPaiementService.getItemTableauPaiement(contenu, brouillon.getPkBrouillons());
                if (null != itemTableauPaiement) {
                    result.add(itemTableauPaiement);
                }
            }
        } else if (objectType.equals(RequestConstant.DEMANDES_PATH)) {
            for (String currentId : idsList) {
                // On va chercher l'objet dans l'implémentation de TableauPaiementService propre à chaque TS
                DemandeDTO demande = demandesService.getDemande(Integer.valueOf(currentId), usagerId);
                JsonNode contenu = demande.getContenu();
                TableauDTO itemTableauPaiement = tableauPaiementService.getItemTableauPaiement(contenu, demande.getPkDemandes());
                if (null != itemTableauPaiement) {
                    result.add(itemTableauPaiement);
                }
            }
        }
        return result;
    }

    @Override
    public InfoFacturationResponseDTO getInfoFacturation(Integer usagerId) {
        GichuniUsagerDTO usager = usagersCache.get(usagerId);
        InfoFacturationResponseDTO result = new InfoFacturationResponseDTO();
        VousDTO vous = new VousDTO();
        // TODO determiné où on ira chercher ces infos par la suite
        // Identité
        vous.setNom(usager.getNom());
        vous.setPrenom(usager.getPrenom());
        vous.setTitre(usager.getTitre());
        result.setVous(vous);
        result.setEmail(usager.getEmail());

        // Adresse
        AdresseDTO adresse = new AdresseDTO();
        adresse.setLigne1(usager.getAdresse1());
        adresse.setLigne2(usager.getAdresse2() != null ? usager.getAdresse2() : "");
        adresse.setLigne3(usager.getComplementAdresse() != null ? usager.getComplementAdresse() : "");
        adresse.setPays(usager.getPaysCode());
        adresse.setCodePostal(usager.getCodePostal());
        adresse.setVille(usager.getVille());
        result.setAdresse(adresse);

        // Raison sociale
        result.setRaisonSociale(usager.getRaisonSociale());
        result.setSaveRaisonSociale(true);
        result.setProfilType(usager.getType().getValue());

        return result;
    }

    @Override
    public void createMoyenPaiement(String ids, Integer usagerId, String orderId) {
        logStartMethod(LOGGER);
        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        String replace = ids.replace("[", "").replace("]", "");
        List<String> demandeIds = new ArrayList<>(Arrays.asList(replace.split(",")));
        Map<Integer, DemandeBO> demandes = new HashMap<>();
        Map<Integer, BigDecimal> totauxDemandes = new HashMap<>();
        Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes = new HashMap<>();
        moyenPaiement.setPkMoyensPaiements(orderId);
        // Je crée la commande que j'associerai à mon moyen de paiement
        BigDecimal totalCommande = calculTotalCommande(demandeIds, usagerId, demandes, totauxDemandes,
                articlesDemandes);
        CommandeBO commande = createCommande(totalCommande, moyenPaiement, demandeIds, demandes, totauxDemandes,
                articlesDemandes);
        createInfoFacturation(usagerId, commande);
        LocalDateTime now = LocalDateTime.now();
        moyenPaiement.setCommande(commande);
        moyenPaiement.setDateCreation(now);
        moyenPaiement.setDateDerniereModification(now);
        moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutEnum.EN_ATTENTE_DE_VALIDATION);
        moyenPaiement.setPaymentSupplier(PSPEnum.LYRA);
        moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
    }

    private void createInfoFacturation(Integer usagerId, CommandeBO commande) {
        // Stockage de l'info de facturation en base de donnée
        InfoFacturationResponseDTO result = getInfoFacturation(usagerId);
        infoFacturationRepository
                .save(InfoFacturationTransformer.infoFacturationResponseDTOToInfoFacturationBO(result, commande));
    }

    private BigDecimal calculTotalCommande(List<String> demandeIds, Integer usagerId, Map<Integer, DemandeBO> demandes,
            Map<Integer, BigDecimal> totauxDemandes, Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes) {
        BigDecimal totalCommande = BigDecimal.ZERO;
        for (String demandeId : demandeIds) {
            Integer pkDemande = Integer.valueOf(demandeId);
            DemandeBO demandeBO = demandesRepository.findByPkDemandesAndUsagerId(pkDemande, usagerId);
            if (demandeBO == null) {
                throw new DemarchesServiceException(
                        "La demande " + demandeId + " est introuvable pour l'usager id " + usagerId,
                        HttpStatus.NOT_FOUND);
            }
            demandes.put(pkDemande, demandeBO);
            // TODO Changer le moyen de récupérer le statut d'un paiement
            // TODO à voir
            //            DemandeDataDTO data = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());
            //            if (data != null && StringUtils.equals(data.getValue(), PaiementStatutEnum.EMPREINTE_VALIDE.name())) {
            //                throw new DemarchesServiceException("La demande " + demandeId + " a déjà une empreinte bancaire valide.", HttpStatus.CONFLICT);
            //            }

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

    private CommandeBO createCommande(BigDecimal totalCommande, MoyenPaiementBO moyenPaiement, List<String> demandeIds,
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

    private List<CommandeDemandeBO> createCommandesDemandes(CommandeBO commande, List<String> demandeIds,
            Map<Integer, DemandeBO> demandes, Map<Integer, BigDecimal> totauxDemandes,
            Map<Integer, List<CommandeDemandeArticleBO>> articlesDemandes) {
        List<CommandeDemandeBO> commandesDemandes = new ArrayList<>();
        for (String demandeId : demandeIds) {
            CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
            commandeDemande.setCommande(commande);
            commandeDemande.setDemande(demandes.get(Integer.valueOf(demandeId)));
            commandeDemande.setMontant(totauxDemandes.get(Integer.valueOf(demandeId)).doubleValue());
            commandeDemande.setCommandesDemandesArticles(new ArrayList<>());
            commandeDemande = commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);

            var articles = new ArrayList<CommandeDemandeArticleBO>();
            for (CommandeDemandeArticleBO articleBO : articlesDemandes.get(Integer.valueOf(demandeId))) {
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
}
