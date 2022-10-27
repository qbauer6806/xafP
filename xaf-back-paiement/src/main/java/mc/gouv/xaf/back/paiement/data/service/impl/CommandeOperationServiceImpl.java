package mc.gouv.xaf.back.paiement.data.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.service.CommandeOperationService;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeOperationTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;

@Component
public class CommandeOperationServiceImpl implements CommandeOperationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandeOperationServiceImpl.class);
	
	@Autowired
	private CommandeOperationRepository commandeOperationRepository;

	@Override
	public List<CommandeOperationDTO> getAllCommandeOperationsAccepteeFilteredByDate(Date startDate, Date endDate) {
		LOGGER.info("Récupération en base des commandes operations filtrées par date...");
		LocalDateTime startDateLdt = null;
		LocalDateTime endDateLdt = null;
		OperationStatutEnum statutAccepte = OperationStatutEnum.ACCEPTEE;
		// Conversion des dates en LocalDateTime pour respecter les données en base
		if (startDate != null) {
			startDateLdt = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
		}
		if (endDate != null) {
			endDateLdt = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
		}
		
		List<CommandeOperationBO> commandeOperations;
		if (startDate != null && endDate != null) {
			commandeOperations = commandeOperationRepository.findAllCommandeOperationBetween(startDateLdt, endDateLdt, statutAccepte);
		} else if (startDate != null) {
			commandeOperations = commandeOperationRepository.findAllCommandeOperationFrom(startDateLdt, statutAccepte);
		} else if (endDate != null) {
			commandeOperations = commandeOperationRepository.findAllCommandeOperationUntil(endDateLdt, statutAccepte);
		} else {
			commandeOperations = commandeOperationRepository.findAllCommandeOperation(statutAccepte);
		}

		LOGGER.info("Transformation bo -> dto ...");

		return CommandeOperationTransformer.bos2Dtos(commandeOperations);
	}

}
