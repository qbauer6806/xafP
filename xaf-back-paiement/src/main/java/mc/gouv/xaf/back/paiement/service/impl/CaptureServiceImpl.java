package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeOperationTransformer;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.CaptureService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private DemandesDataService demandesDataService;
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    @Autowired
    private PaiementsDataProvider paiementsDataProvider;
    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Override
    public CommandeOperationDTO capture(CommandeDTO commandeDTO, DemandeDTO demandeDTO) throws DemarchesServiceException {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ commandeDTO {}] ", commandeDTO);
        CommandeOperationDTO operation = new CommandeOperationDTO();

        DemandeDataDTO data = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeDTO.getPkDemandes(), PaiementDemandeDataKeysEnum.NUMERO_PERMIS.name());
        String numeroPermis = data.getValue();
        LOGGER.info("Permis n° : {}", numeroPermis);

        List<CommandeDemandeDTO> commandeDemandeDTOS = commandeDTO.getCommandesDemandes();
        CommandeDemandeDTO commandeDemandeDTO = null;
        for (CommandeDemandeDTO c : commandeDemandeDTOS) {
            if (c.getFkDemandes().equals(demandeDTO.getPkDemandes())) {
                commandeDemandeDTO = c;
                break;
            }
        }

        if (null == commandeDemandeDTO) {
            throw new DemarchesServiceException("Impossible de trouver la liaison entre la demande et la commande", HttpStatus.NOT_FOUND);
        }

        operation.setMontant(commandeDemandeDTO.getMontant());
        if (paiementApiClient.capture(commandeDTO, operation, demandeDTO)) {
            BigDecimal montantDejaCapture = BigDecimal.valueOf(commandeDTO.getMontantDejaCapture());
            montantDejaCapture = montantDejaCapture.add(BigDecimal.valueOf(commandeDemandeDTO.getMontant()));
            commandeDTO.setMontantDejaCapture(montantDejaCapture.doubleValue());

            BigDecimal montantRestant = BigDecimal.valueOf(commandeDTO.getMontantRestant());
            montantRestant = montantRestant.subtract(BigDecimal.valueOf(commandeDemandeDTO.getMontant()));
            commandeDTO.setMontantRestant(montantRestant.doubleValue());

            operation.setPkOperations(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
            LocalDateTime now = LocalDateTime.now();
            operation.setDateCreation(now);
            operation.setDateDerniereModification(now);
            operation.setOperationType(OperationTypeEnum.DEBIT.name());

            MoyenPaiementBO paiement = moyenPaiementRepository.findByCommande_PkCommandes(commandeDTO.getPkCommandes());
            Optional<String> optionalNumFacture = factureApiClient.createFacture(numeroPermis, "0", operation.getMontant(), paiement.getPkMoyensPaiements(), paiementsDataProvider.getInfosFacturation(demandeDTO), commandeDemandeDTO.getCommandeDemandeArticles(), demandeDTO, operation);
            if (optionalNumFacture.isPresent()) {
                LOGGER.info("Created [ facture n°{}] ", optionalNumFacture.get());
                operation.setNumeroFacture(optionalNumFacture.get());
            } else {
                operation.setNumeroFacture(FactureApiClient.INCIDENT);
            }

            CommandeOperationBO commandeOperationBO = CommandeOperationTransformer.dto2Bo(operation);
            CommandeBO commandeBO = CommandeTransformer.dto2Bo(commandeDTO);
            if (commandeBO.getOperations() != null) {
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
