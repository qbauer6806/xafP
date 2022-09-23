package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeTransformer;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeOperationTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class CaptureServiceImpl implements CaptureService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureServiceImpl.class);

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;
    @Autowired
    private PaiementApiClient paiementApiClient;
    @Autowired
    private FactureApiClient factureApiClient;
    @Autowired
    private CommandeRepository commandeRepository;
    @Autowired
    private ReferenceFactoryService referenceFactoryService;
    @Autowired
    private MontantService montantService;
    @Autowired
    private DemandesDataService demandesDataService;
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    @Autowired
    private PaiementsDataProvider paiementsDataProvider;

    @Override
    public CommandeOperationDTO capture(CommandeDTO commandeDTO, DemandeDTO demandeDTO) throws Exception {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ commandeDTO {}] ", commandeDTO);
        CommandeOperationDTO operation = new CommandeOperationDTO();

        DemandeDataDTO data = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeDTO.getPkDemandes(), PaiementDemandeDataKeysEnum.NUMERO_PERMIS.name());
        String numeroPermis = data.getValue();
        LOGGER.info("Permis n° : {}", numeroPermis);

        HashMap<String, Double> objetMontants = montantService.getPaiements(demandeDTO);
        double prix = montantService.getMontant(objetMontants);
        operation.setMontant(prix);


        if (paiementApiClient.capture(commandeDTO, operation, demandeDTO)) {
            commandeDTO.setMontantDejaCapture(commandeDTO.getMontantDejaCapture() + prix);
            commandeDTO.setMontantRestant(commandeDTO.getMontantRestant() - prix);

            operation.setPkOperations(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
            LocalDateTime now = LocalDateTime.now();
            operation.setDateCreation(now);
            operation.setDateDerniereModification(now);
            operation.setDateDerniereModification(now);

            operation.setOperationType(OperationTypeEnum.DEBIT.name());

            Optional<String> optionalNumFacture = factureApiClient.createFacture(numeroPermis, " ", operation.getMontant(), operation.getPkOperations(), paiementsDataProvider.getInfosFacturation(demandeDTO), objetMontants, demandeDTO, operation);
            if (optionalNumFacture.isPresent()) {
                LOGGER.info("Created [ facture n°{}] ", optionalNumFacture.get());
                operation.setNumeroFacture(optionalNumFacture.get());
            }

            CommandeOperationBO commandeOperationBO = CommandeOperationTransformer.dto2Bo(operation);
            CommandeBO commandeBO = CommandeTransformer.dto2Bo(commandeDTO);
            if(commandeBO.getOperations() != null) {
                commandeBO.getOperations().add(commandeOperationBO);
            } else {
                List<CommandeOperationBO> commandeOperationBOList = new ArrayList<>();
                commandeOperationBOList.add(commandeOperationBO);
                commandeBO.setOperations(commandeOperationBOList);
            }
            commandeBO = commandeRepository.save(commandeBO);

            LOGGER.info("Created [ operation {}] ", operation);
            commandeOperationBO.setCommande(commandeBO);
            commandeOperationRepository.save(commandeOperationBO);
        }

        return operation;
    }
}
