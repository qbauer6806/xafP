package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.StatistiquesTypesRepository;
import mc.gouv.xaf.back.data.entity.StatistiquesTypesBO;
import mc.gouv.xaf.back.data.transformer.StatistiquesTypesTransformer;
import mc.gouv.xaf.back.service.data.StatistiquesTypesService;
import mc.gouv.xaf.shared.dto.StatistiquesTypesDTO;

/**
 * Service permettant la manipulation des statistiques types.
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class StatistiquesTypesServiceImpl implements StatistiquesTypesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatistiquesTypesServiceImpl.class);
	
	@Autowired 
	private StatistiquesTypesRepository statTypeRepository;

	@Override
	public void deleteStatistiquesTypes(String identifiantDemande) {
		LOGGER.info("Supression de types pour la demande {}", identifiantDemande);
		List<StatistiquesTypesBO> typesToDelete = statTypeRepository.findByIdentifiantDemande(identifiantDemande);
		if(null != typesToDelete && !typesToDelete.isEmpty()) {
			for (StatistiquesTypesBO statistiquesTypesBO : typesToDelete) {
				statTypeRepository.delete(statistiquesTypesBO);
			}
		}
	}

	@Override
	public StatistiquesTypesDTO saveStatistiquesTypes(StatistiquesTypesDTO statType) {
		LOGGER.info("Création d'un type pour la demande {}", statType.getIdentifiantDemande());
		StatistiquesTypesBO bo = StatistiquesTypesTransformer.dto2Bo(statType);
		bo = statTypeRepository.save(bo);
		return StatistiquesTypesTransformer.bo2Dto(bo);
	}

	@Override
	public List<StatistiquesTypesDTO> getStatistiquesTypes(String identifiantDemande) {
		List<StatistiquesTypesDTO> result = new ArrayList<StatistiquesTypesDTO>();
		LOGGER.info("Recupération des types de la demande {}", identifiantDemande);
		List<StatistiquesTypesBO> typesToReturn = statTypeRepository.findByIdentifiantDemande(identifiantDemande);
		for (StatistiquesTypesBO statistiquesTypesBO : typesToReturn) {
			result.add(StatistiquesTypesTransformer.bo2Dto(statistiquesTypesBO));
		}
		return result;
	}
}
