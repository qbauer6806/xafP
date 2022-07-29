package mc.gouv.xaf.back.paiement.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.paiement.client.FactureClient;
import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationTypeBO;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class CaptureServiceImpl implements CaptureService {
    private static Logger LOGGER = LoggerFactory.getLogger(CaptureServiceImpl.class);

    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private PaiementClient paiementClient;
    @Autowired
    private FactureClient factureClient;
    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;
    @Autowired
    private ReferenceFactoryService referenceFactoryService;

    @Autowired
    private MontantService montantService;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;


    @Override
    public OperationBO capture(MoyenPaiementBO moyenPaiementBO, DemandeDTO demandeDTO) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementBO {}] ", moyenPaiementBO);
        OperationBO operation = new OperationBO();

        JsonNode contenuDemande = demandeDTO.getContenu();


        //todo change permis
        String numeroPermis = contenuDemande.get("titre").get("numeropermis").asText();
        LOGGER.info("Permis n° : {}", numeroPermis);

        HashMap<String, Double> objetMontants = montantService.getPaiements(demandeDTO);
        double prix = montantService.getMontant(objetMontants);
        operation.setMontant(prix);


        if (paiementClient.capture(moyenPaiementBO, operation, demandeDTO)) {
            moyenPaiementBO.setMontantCapture(moyenPaiementBO.getMontantCapture() + prix);
            moyenPaiementBO.setMontantRestant(moyenPaiementBO.getMontantRestant() - prix);

            operation.setPkOperation(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
            LocalDateTime now = LocalDateTime.now();
            operation.setDateCreation(now);
            operation.setDateDerniereModification(now);
            operation.setDateDerniereModification(now);

            moyenPaiementRepository.save(moyenPaiementBO);

            operation.setOperationType(OperationTypeBO.DEBIT);

            Optional<String> optionalNumFacture = factureClient.createFacture(numeroPermis, " ", operation.getMontant(), operation.getPkOperation(), demandeDTO.getUsagerId(), objetMontants, demandeDTO, operation);
            if (optionalNumFacture.isPresent()) {
                LOGGER.info("Created [ facture n°{}] ", optionalNumFacture.get());
                operation.setNumeroFacture(optionalNumFacture.get());
            }

            LOGGER.info("Created [ operation {}] ", operation);
            operation = operationRepository.save(operation);
        } else {
            commandeDemandeRepository.deleteAll(commandeDemandeRepository.findByDemande_PkDemandes(demandeDTO.getPkDemandes()));
        }


        return operation;
    }
}
