package mc.gouv.xaf.back.paiement.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.paiement.client.FactureClient;
import mc.gouv.xaf.back.paiement.client.PaiementClient;
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

    @Override
    public String capture(MoyenPaiementBO moyenPaiementBO, DemandeDTO demandeDTO) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementBO {}] ", moyenPaiementBO);
        OperationBO operation = new OperationBO();

        JsonNode contenuDemande = demandeDTO.getContenu();

        String numeroPermis = contenuDemande.get("titre").get("numeropermis").asText();
        LOGGER.info("Permis n° : {}", numeroPermis);

        HashMap<String, Double> objetMontants = montantService.getPaiements(demandeDTO);
        double prix = montantService.getMontant(objetMontants);
        operation.setMontant(prix);


        paiementClient.capture(moyenPaiementBO, operation);

        moyenPaiementBO.setMontantCapture(moyenPaiementBO.getMontantCapture() + prix);
        moyenPaiementBO.setMontantRestant(moyenPaiementBO.getMontantRestant() - prix);

        moyenPaiementRepository.save(moyenPaiementBO);

        operation.setPkOperation(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));

        operation.setOperationType(OperationTypeBO.DEBIT);
        LocalDateTime now = LocalDateTime.now();
        operation.setDateCreation(now);
        operation.setDateDerniereModification(now);
        operation.setDateDerniereModification(now);
        operation = operationRepository.save(operation);
        String numFacture = factureClient.createFacture(numeroPermis, " ", operation.getMontant(), operation.getPkOperation(), demandeDTO.getUsagerId(), objetMontants);
        LOGGER.info("Created [ facture n°{}] ", numFacture);
        operation.setNumeroFacture(numFacture);
        LOGGER.info("Created [ operation {}] ", operation);
        return numFacture;
    }
}
