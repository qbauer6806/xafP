package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationTypeBO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

@Component
public class CaptureServiceImpl implements CaptureService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureServiceImpl.class);

    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private PaiementApiClient paiementApiClient;
    @Autowired
    private FactureApiClient factureApiClient;
    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;
    @Autowired
    private ReferenceFactoryService referenceFactoryService;

    @Autowired
    private MontantService montantService;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PaiementsDataProvider paiementsDataProvider;
    
    @Override
    public OperationBO capture(MoyenPaiementBO moyenPaiementBO, DemandeDTO demandeDTO) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ moyenPaiementBO {}] ", moyenPaiementBO);
        OperationBO operation = new OperationBO();

        DemandeDataDTO data = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeDTO.getPkDemandes(), PaiementDemandeDataKeysEnum.NUMERO_PERMIS.name());
        String numeroPermis = data.getValue();
        LOGGER.info("Permis n° : {}", numeroPermis);

        HashMap<String, Double> objetMontants = montantService.getPaiements(demandeDTO);
        double prix = montantService.getMontant(objetMontants);
        operation.setMontant(prix);


        if (paiementApiClient.capture(moyenPaiementBO, operation, demandeDTO)) {
            moyenPaiementBO.setMontantCapture(moyenPaiementBO.getMontantCapture() + prix);
            moyenPaiementBO.setMontantRestant(moyenPaiementBO.getMontantRestant() - prix);

            operation.setPkOperation(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
            LocalDateTime now = LocalDateTime.now();
            operation.setDateCreation(now);
            operation.setDateDerniereModification(now);
            operation.setDateDerniereModification(now);

            moyenPaiementRepository.save(moyenPaiementBO);

            operation.setOperationType(OperationTypeBO.DEBIT);

            Optional<String> optionalNumFacture = factureApiClient.createFacture(numeroPermis, " ", operation.getMontant(), operation.getPkOperation(), paiementsDataProvider.getInfosFacturation(demandeDTO), objetMontants, demandeDTO, operation);
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
