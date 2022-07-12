package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.paiement.client.FactureClient;
import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.client.SecurityService;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementStatutBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationTypeBO;
import mc.gouv.xaf.back.paiement.service.DemandeStatutService;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.stc.config.MoneticoPaiementConfig;
import mc.gouv.xaf.shared.stc.dto.BillingDTO;
import mc.gouv.xaf.shared.stc.dto.ContexteCommandeDTO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
public class PaiementServiceImpl implements PaiementService {
    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);
    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private ReferenceFactoryService referenceFactoryService;

    @Autowired
    @Lazy
    private UsagersCache usagersCache;
    @Autowired
    private PaiementClient paiementClient;
    @Autowired
    private FactureClient factureClient;

    @Autowired
    private DemandeStatutService demandeStatutService;

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public PaiementDTO create(String demandesId, String langue, Integer usagerId) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ demandesId {}, langue {}, usagerId {} ] ", demandesId, langue, usagerId);

        PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
        double prix = Double.parseDouble(montantProperty.getValue());
        List<Integer> demandesIdList = Stream.of(demandesId.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
        double montant = prix * demandesIdList.size();


        LocalDateTime now = LocalDateTime.now();

        CommandeBO commande = new CommandeBO();
        commande.setDateCreation(now);
        commande.setMontant(montant);
        commande = commandeRepository.save(commande);
        LOGGER.info("Created [ commande {}] ", commande);

        for (Integer demandeId : demandesIdList) {
            CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
            commandeDemande.setCommande(commande);
            commandeDemande.setDemande(demandesRepository.findById(demandeId).orElseThrow(RuntimeException::new));
            commandeDemande.setMontant(prix);
            commandeDemande = commandeDemandeRepository.save(commandeDemande);
            LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);
        }


        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        moyenPaiement.setCommande(commande);
        moyenPaiement.setPkMoyenPaiement(referenceFactoryService.createSimpleReference12Digits());
        moyenPaiement.setDateLimite(now.plusDays(30));
        moyenPaiement.setDateDerniereModification(now);
        moyenPaiement.setMontantInitial(montant);
        moyenPaiement.setMontantRestant(montant);
        moyenPaiement.setMontantCapture(0);
        moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.EN_ATTENTE_DE_VALIDATION);

        moyenPaiement = moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
        PaiementDTO paiementDTO = new PaiementDTO(langue);
        paiementDTO.setDate(securityService.dateFormat(new Date()));
        GichuniUsagerDTO usager = usagersCache.get(usagerId);

        BillingDTO billingDTO = new BillingDTO();
        billingDTO.setAddressLine1(usager.getAdresse1());
        billingDTO.setCity(usager.getVille());
        billingDTO.setPostalCode(usager.getCodePostal());
        billingDTO.setCountry(usager.getPaysCode());

        ContexteCommandeDTO contexteCommandeDTO = new ContexteCommandeDTO();
        contexteCommandeDTO.setBilling(billingDTO);

        paiementDTO.setContexte_commande(securityService.contexteCommandeDTOtoBase64(contexteCommandeDTO));
        String date = securityService.dateFormat(new Date());
        paiementDTO.setDate(date);
        paiementDTO.setMontant(montant + "EUR");
        paiementDTO.setReference(moyenPaiement.getPkMoyenPaiement());


        paiementDTO.setMail(usager.getEmail());
        paiementDTO.setMode_affichage("iframe");
        paiementDTO.setTexteLibre(MoneticoPaiementConfig.MONETICOPAIEMENT_COMPANYCODE + date);
        paiementDTO.setMAC(securityService.getHmacString(paiementDTO));

        LOGGER.info("Return [ paiementDTO {}] ", paiementDTO);
        return paiementDTO;
    }

    @Override
    public void updateStatus(String reference, String status) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ reference {}, status {} ] ", reference, status);
        MoyenPaiementBO moyenPaiement = moyenPaiementRepository.findById(reference).get();
        if (status.equals("payetest") || status.equals("paiement")) {
            moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.VALIDE);
            List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByCommande_PkCommande(moyenPaiement.getCommande().getPkCommande());
            for (CommandeDemandeBO commandeDemandeBO : commandeDemandeBOList) {
                DemandeBO demandeBO = commandeDemandeBO.getDemande();
                DemandesStatutsBO dernierStatut = demandeBO.getDernierStatut();
                dernierStatut.setLibelle(demandeStatutService.getEnAttenteDeTraitement());
                demandesStatutsRepository.save(dernierStatut);
                try {
                    indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demandeBO.getPkDemandes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            moyenPaiement.setMoyenPaiementStatut(MoyenPaiementStatutBO.INVALIDE);
        }

        moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
    }

    @Override
    public Optional<MoyenPaiementBO> getMoyenPaiement(Integer demandeId) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ demandeId {}] ", demandeId);
        CommandeDemandeBO commandeDemandeBO = commandeDemandeRepository.findByDemande_PkDemandes(demandeId).get(0);
        LOGGER.info("Find [ commandeDemandeBO {}] ", commandeDemandeBO);
        List<MoyenPaiementBO> moyenPaiements = moyenPaiementRepository.findByCommande_PkCommande(commandeDemandeBO.getCommande().getPkCommande());
        return moyenPaiements.stream().sorted(Comparator.comparing(MoyenPaiementBO::getDateLimite, Comparator.nullsLast(Comparator.reverseOrder()))).findFirst();
    }

    @Override
    public String capture(MoyenPaiementBO moyenPaiementBO, Integer usagerId) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementBO {}] ", moyenPaiementBO);
        OperationBO operation = new OperationBO();
        PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
        double prix = Double.parseDouble(montantProperty.getValue());
        operation.setMontant(prix);
        moyenPaiementBO.setMontantCapture(moyenPaiementBO.getMontantCapture() + prix);

        paiementClient.capture(moyenPaiementBO, operation);

        moyenPaiementBO.setMontantRestant(moyenPaiementBO.getMontantRestant() - prix);
        moyenPaiementRepository.save(moyenPaiementBO);

        operation.setPkOperation(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));

        operation.setOperationType(OperationTypeBO.DEBIT);
        LocalDateTime now = LocalDateTime.now();
        operation.setDateCreation(now);
        operation.setDateDerniereModification(now);
        operation.setDateDerniereModification(now);
        operation = operationRepository.save(operation);
        String numFacture = factureClient.createFacture(referenceFactoryService.createSimpleReferenceDigitsNumeric(6), " ", operation.getMontant(), operation.getPkOperation(), usagerId);
        LOGGER.info("Created [ facture n°{}] ", numFacture);
        operation.setNumeroFacture(numFacture);
        LOGGER.info("Created [ operation {}] ", operation);
        return numFacture;
    }


}
