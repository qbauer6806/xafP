package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeOperationTransformer;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.service.itg.PaiementApiClient;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.commons.collections4.CollectionUtils;
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
    private PaiementsDataProvider paiementsDataProvider;

    @Override
    public CommandeOperationDTO capture(CommandeDTO commandeDTO, DemandeDTO demandeDTO)
            throws DemarchesServiceException {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ commandeDTO {}] ", commandeDTO);
        List<CommandeDemandeDTO> commandeDemandeDTOS = commandeDTO.getCommandesDemandes();
        if (CollectionUtils.isEmpty(commandeDemandeDTOS)) {
            throw new DemarchesServiceException("Aucune liaison commande - demande trouvée", HttpStatus.NOT_FOUND);
        }
        CommandeDemandeDTO commandeDemandeDTO = commandeDemandeDTOS.stream()
                .filter(comm -> comm.getFkDemandes().equals(demandeDTO.getPkDemandes())).findFirst().orElseThrow(
                        () -> new DemarchesServiceException(
                                "Impossible de trouver la liaison entre la demande et la commande",
                                HttpStatus.NOT_FOUND));

        CommandeOperationDTO operation = new CommandeOperationDTO();
        // Si la démarche gère des tâches, il se peut que la demande soit partiellement validée, on doit calculer le montant à capturer
        // Sinon la méthode par défaut retourne le montant de la commande.
        operation.setMontant(paiementsDataProvider.getMontantCapture(demandeDTO, commandeDemandeDTO));
        boolean resultatCapture = paiementApiClient.capture(commandeDTO, operation, demandeDTO);

        operation.setPkOperations(referenceFactoryService.createSimpleReferenceDigitsNumeric(7));
        LocalDateTime now = LocalDateTime.now();
        operation.setDateCreation(now);
        // TODO operation.setDateDerniereModification(now);
        operation.setOperationType(OperationTypeEnum.DEBIT.name());

        if (resultatCapture) {
            BigDecimal montantDejaCapture = BigDecimal.valueOf(commandeDTO.getMontantDejaCapture());
            montantDejaCapture = montantDejaCapture.add(BigDecimal.valueOf(operation.getMontant()));
            commandeDTO.setMontantDejaCapture(montantDejaCapture.doubleValue());

            BigDecimal montantRestant = BigDecimal.valueOf(commandeDTO.getMontantRestant());
            montantRestant = montantRestant.subtract(BigDecimal.valueOf(operation.getMontant()));
            commandeDTO.setMontantRestant(montantRestant.doubleValue());

            List<CirRequestDTO> lignes = paiementsDataProvider.getLignesFacture(demandeDTO, operation, commandeDTO);
            Optional<String> optionalNumFacture = factureApiClient.createFacture(lignes, demandeDTO);
            if (optionalNumFacture.isPresent()) {
                LOGGER.info("Created [ facture n°{}] ", optionalNumFacture.get());
                //TODO operation.setNumeroFacture(optionalNumFacture.get());
            } else {
                //TODO operation.setNumeroFacture(FactureApiClient.INCIDENT);
            }
        }

        // Enregistrement de l'opéation même en cas d'échec ou d'incident
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

        return operation;
    }
}
