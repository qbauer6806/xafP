package mc.gouv.xaf.back.stc.service.impl;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.stc.client.FactureClient;
import mc.gouv.xaf.back.stc.client.PaiementClient;
import mc.gouv.xaf.back.stc.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.stc.data.dao.CommandeRepository;
import mc.gouv.xaf.back.stc.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.stc.data.dao.OperationRepository;
import mc.gouv.xaf.back.stc.data.entity.CommandeBO;
import mc.gouv.xaf.back.stc.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementStatutBO;
import mc.gouv.xaf.back.stc.data.entity.OperationBO;
import mc.gouv.xaf.back.stc.data.entity.OperationStatutBO;
import mc.gouv.xaf.back.stc.data.entity.OperationTypeBO;
import mc.gouv.xaf.back.stc.service.MoneticoService;
import mc.gouv.xaf.back.stc.service.PaiementService;
import mc.gouv.xaf.back.stc.service.ReferenceFactoryService;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.stc.dto.BillingDTO;
import mc.gouv.xaf.shared.stc.dto.ContexteCommandeDTO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mc.gouv.xaf.back.stc.LoggerMethodeUtils.logStartMethod;

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
    private MoneticoService moneticoService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private ReferenceFactoryService referenceFactoryService;

    @Autowired
    private UsagersCache usagersCache;
    @Autowired
    private PaiementClient paiementClient;
    @Autowired
    private FactureClient factureClient;

    @Override
    public PaiementDTO create(String demandesId, String langue, Integer usagerId) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ demandesId {}, langue {}, usagerId {} ] ", demandesId, langue, usagerId);

        PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
        double prix = Double.parseDouble(montantProperty.getValue());
        List<Integer> demandesIdList = Stream.of(demandesId.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
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

        moyenPaiement = moyenPaiementRepository.save(moyenPaiement);
        LOGGER.info("Created [ moyenPaiement {}] ", moyenPaiement);
        PaiementDTO paiementDTO = new PaiementDTO(langue);
        paiementDTO.setDate(moneticoService.dateFormat(new Date()));

        ContexteCommandeDTO contexteCommandeDTO = new ContexteCommandeDTO();
        contexteCommandeDTO.setBilling(new BillingDTO());

        paiementDTO.setContexte_commande(moneticoService.contexteCommandeDTOtoBase64(contexteCommandeDTO));
        paiementDTO.setDate(moneticoService.dateFormat(new Date()));
        paiementDTO.setMontant(montant + "EUR");
        paiementDTO.setReference(moyenPaiement.getPkMoyenPaiement());

        GichuniUsagerDTO usager = usagersCache.get(usagerId);
        paiementDTO.setMail(usager.getEmail());
        paiementDTO.setMode_affichage("iframe");
        paiementDTO.setThreeDSecureChallenge("challenge_mandated");
        paiementDTO.setMAC(moneticoService.getHmacString(paiementDTO));

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
    public String capture(MoyenPaiementBO moyenPaiementBO, Integer demandeId) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementBO {}] ", moyenPaiementBO);
        PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
        double prix = Double.parseDouble(montantProperty.getValue());

        moyenPaiementBO.setMontantCapture(moyenPaiementBO.getMontantCapture() + prix);
        moyenPaiementBO.setMontantRestant(moyenPaiementBO.getMontantRestant() - prix);

        String numAuthorisation = paiementClient.capture(moyenPaiementBO, prix);

        moyenPaiementRepository.save(moyenPaiementBO);

        OperationBO operation = new OperationBO();
        operation.setMontant(moyenPaiementBO.getMontantRestant());
        operation.setPkOperation(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
        operation.setOperationStatut(OperationStatutBO.ACCEPTEE);
        operation.setOperationType(OperationTypeBO.DEBIT);
        LocalDateTime now = LocalDateTime.now();
        operation.setDateCreation(now);
        operation.setDateDerniereModification(now);
        operation.setNumeroAuthorisation(Integer.parseInt(numAuthorisation));
        operation = operationRepository.save(operation);
        LOGGER.info("Created [ operation {}] ", operation);
        factureClient.createFacture(referenceFactoryService.createSimpleReferenceDigitsNumeric(6), null, operation.getMontant(), operation.getPkOperation(), demandeId);

        return operation.getPkOperation();
    }


}
