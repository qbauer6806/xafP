package mc.gouv.xaf.back.service.data.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesDataRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.transformer.DemandesDataTransformer;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

/**
 * Service permettant la manipulation des données d'une demande.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesDataServiceImpl implements DemandesDataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemandesDataServiceImpl.class);

	@Autowired
	private DemandesDataRepository demandesDataRepository;

	@Autowired
	private DemandesService demandesService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key) {
		// Jette une exception si la demande n'existe pas
		demandesService.getCheckDemarcheDemandeDTO(demarcheId, demandeId, true);
		return getDemandeDataNoCheck(demarcheId, demandeId, key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDataDTO getDemandeDataNoCheck(String demarcheId, Integer demandeId, String key) {
		DemandesDataBO demandesDataBo = getDemandeDataBO(demandeId, key);
		LOGGER.info("Transformation bo -> dto ...");
		return DemandesDataTransformer.bo2Dto(demandesDataBo);
	}

	private DemandesDataBO getDemandeDataBO(Integer demandeId, String key) {
		LOGGER.info("Récupération en base de la donnée de demande...");
		return demandesDataRepository.findByFkDemandesPkDemandesAndKey(demandeId, key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDataDTO> getDemandeDatas(String demarcheId, Integer demandeId) {

		// Jette une exception si la demande n'existe pas
		demandesService.getCheckDemarcheDemandeDTO(demarcheId, demandeId, true);

		LOGGER.info("Récupération en base de la donnée de demande...");

		List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByFkDemandesPkDemandes(demandeId);

		LOGGER.info("Transformation bo -> dto ...");

		return DemandesDataTransformer.bo2Dto(demandesDatasBo);
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDataDTO> getDemandeDatasByKeyAndValue(String key, String value) {
        LOGGER.info("Récupération en base des IDs des demandes...");
        List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByKeyAndValue(key, value);
        LOGGER.info("Transformation bo -> dto ...");
        return DemandesDataTransformer.bo2Dto(demandesDatasBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDataDTO> getDemandeDatasByKeyAndValueAndfkDemandes(String key, String value,
            List<DemandeBO> demandeIds) {
        LOGGER.info("Récupération en base des IDs des demandes...");
        List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByKeyAndValueAndFkDemandesIn(key, value, demandeIds);
        LOGGER.info("Transformation bo -> dto ...");
        return DemandesDataTransformer.bo2Dto(demandesDatasBo);
    }

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value) {
		return saveOrUpdateDemandeData(demarcheId, demandeId, key, value,true);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws Exception
	 */
	@Override
	public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value, boolean checkActive) {
		// Jette une exception si la demande n'existe pas
		DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, checkActive);
		return saveOrUpdateDemandeDatas(demandeBo, key, value);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	// Cette méthode est créée pour ne pas bombarder elasticsearch si on met à jours
	// plusieurs clès valeurs avec la méthode saveOrUpdateDemandeData
	@Override
	public void saveOrUpdateDemandeDatas(String demarcheId, Integer demandeId, Map<String, String> datas) {

		// Jette une exception si la demande n'existe pas
		DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, true);

		if (datas != null) {
			for (Map.Entry<String, String> entry : datas.entrySet()) {
				saveOrUpdateDemandeDatas(demandeBo, entry.getKey(), entry.getValue());
			}
		}

	}

	private DemandeDataDTO saveOrUpdateDemandeDatas(DemandeBO demandeBo, String key, String value) {
		// Est-ce que cette donnée de demande existe déjà ?
		DemandesDataBO demandesDataBo = getDemandeDataBO(demandeBo.getPkDemandes(), key);

		if (demandesDataBo == null) {
			// Création
			LOGGER.info("Création de la donnée de demande...");
			demandesDataBo = new DemandesDataBO();
			demandesDataBo.setFkDemandes(demandeBo);
			demandesDataBo.setKey(key);
			demandesDataBo.setValue(value);
			if (demandeBo.getData() == null) {
				demandeBo.setData(new HashSet<>());
			}

			demandeBo.getData().add(demandesDataBo);
			demandesDataBo = demandesDataRepository.save(demandesDataBo);
		} else {
			// Mise à jour
			LOGGER.info("Mise à jour de la donnée de demande...");

			if (demandeBo.getData() != null) {
				for (DemandesDataBO data : demandeBo.getData()) {
					if (data.getKey().equals(key)) {
						data.setValue(value);
					}
				}
			}
			demandesDataBo.setValue(value);
			demandesDataBo = demandesDataRepository.save(demandesDataBo);
		}

		LOGGER.info("Transformation bo -> dto ...");

		return DemandesDataTransformer.bo2Dto(demandesDataBo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public DemandeDataDTO updateDemandeData(DemandeDataDTO dataDTO) {
	    DemandesDataBO dataBO = DemandesDataTransformer.dto2Bo(dataDTO);
	    DemandeBO demande = new DemandeBO();
	    demande.setPkDemandes(dataDTO.getDemandeId());
	    dataBO.setFkDemandes(demande);
	    dataBO = demandesDataRepository.save(dataBO);
	    return DemandesDataTransformer.bo2Dto(dataBO);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public void deleteDemandeData(String demarcheId, Integer demandeId, String key) {

		// Jette une exception si la demande n'existe pas
		demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, true);

		DemandesDataBO demandesDataBo = getDemandeDataBO(demandeId, key);

		LOGGER.info("Suppression de la donnée de demande...");
		if (demandesDataBo != null) {
			demandesDataBo.getFkDemandes().getData().remove(demandesDataBo);
			demandesDataRepository.delete(demandesDataBo);
		}

	}

}
