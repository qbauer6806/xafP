package mc.gouv.xaf.back.stc.service.impl;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.stc.client.cir.CirClient;
import mc.gouv.xaf.back.stc.client.monetico.MoneticoClient;
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
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.stc.dto.BillingDTO;
import mc.gouv.xaf.shared.stc.dto.ContexteCommandeDTO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

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
    private MoneticoClient moneticoClient;

    @Override
    public PaiementDTO create(Integer demandeId, String langue, Integer usagerId) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ demandeId {}, langue {}, usagerId {} ] ", demandeId, langue, usagerId);
        LocalDateTime now = LocalDateTime.now();
        PropertiesDTO montantProperty = propertiesService.getProperty("PERMC", "XAF_PAIEMENT_AMOUNT");
        double montant = Double.parseDouble(montantProperty.getValue());
        CommandeBO commande = new CommandeBO();
        commande.setDateCreation(now);
        commande.setMontant(montant);
        commande = commandeRepository.save(commande);
        LOGGER.info("Created [ commande {}] ", commande);

        CommandeDemandeBO commandeDemande = new CommandeDemandeBO();
        commandeDemande.setCommande(commande);
        commandeDemande.setDemande(demandesRepository.findById(demandeId).orElseThrow(RuntimeException::new));
        commandeDemande.setMontant(montant);

        commandeDemande = commandeDemandeRepository.save(commandeDemande);
        LOGGER.info("Created [ commandeDemande {}] ", commandeDemande);

        MoyenPaiementBO moyenPaiement = new MoyenPaiementBO();
        moyenPaiement.setCommande(commande);
        moyenPaiement.setReference(referenceFactoryService.createSimpleReference12Digits());
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
        paiementDTO.setMontant(montantProperty.getValue() + "0EUR");
        paiementDTO.setReference(moyenPaiement.getReference());
        paiementDTO.setMail("egermain.ext@gouv.mc");
        paiementDTO.setTexteLibre("TexteLibre");
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
    public MoyenPaiementBO getMoyenPaiement(Integer demandeId) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ demandeId {}] ", demandeId);
        CommandeDemandeBO commandeDemandeBO = commandeDemandeRepository.findByDemande_PkDemandes(demandeId).get(0);
        LOGGER.info("Find [ commandeDemandeBO {}] ", commandeDemandeBO);
        return moyenPaiementRepository.findByCommande_Id(commandeDemandeBO.getCommande().getId());

    }

    @Override
    public String capture(MoyenPaiementBO moyenPaiementBO) throws IOException {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementBO {}] ", moyenPaiementBO);
        String numAuthorisation = moneticoClient.capture(moyenPaiementBO);
        OperationBO operation = new OperationBO();
        operation.setMontant(moyenPaiementBO.getMontantRestant());
        operation.setReference(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
        operation.setOperationStatut(OperationStatutBO.ACCEPTEE);
        operation.setOperationType(OperationTypeBO.DEBIT);
        LocalDateTime now = LocalDateTime.now();
        operation.setDateCreation(now);
        operation.setDateDerniereModification(now);

        //todo parser numAuthorisation
        operation.setNumeroAuthorisation(0);
        operation = operationRepository.save(operation);
        LOGGER.info("Created [ operation {}] ", operation);
        CirClient.postPaiement(referenceFactoryService.createSimpleReferenceDigitsNumeric(6), null, operation.getMontant(), operation.getReference());

        return operation.getReference();
    }


}
