package mc.gouv.xaf.back.service.data.impl;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.shared.SharedMessages;
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
    private DemandesStatutsRepository demandesStatutsRepository;

	@Autowired
	private DemandesService demandesService;

    @Autowired
    private EntityManager em;


	@Override
	public DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key) {
		return getDemandeData(demarcheId, demandeId, key, true);
	}

	@Override
	public DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key, boolean checkActive) {

		// Jette une exception si la demande n'existe pas
		if(checkActive) {
			demandesService.getCheckDemarcheDemandeDTO(demarcheId, demandeId, true);
		}

		DemandesDataBO demandesDataBo = getDemandeDataBO(demandeId, key);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
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

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

		return DemandesDataTransformer.bo2Dto(demandesDatasBo);
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDataDTO> getDemandeDatasByKeyAndValue(String key, String value) {
        LOGGER.info("Récupération en base des IDs des demandes...");
        List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByKeyAndValue(key, value);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
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
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesDataTransformer.bo2Dto(demandesDatasBo);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value) {
		return saveOrUpdateDemandeData(demarcheId, demandeId, key, value,true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value, boolean checkActive) {
		// Jette une exception si la demande n'existe pas
		DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, checkActive);
		return saveOrUpdateDemandeDatas(demandeBo, key, value);
	}

	/**
	 * {@inheritDoc}
	 */
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

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

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

	public void clonerDemandeData(DemandeBO demandeBo, DemandeBO newDemandeBo) {
		if (demandeBo.getData() != null) {
			LOGGER.info("Dupliquer des données de la demande");
			List<DemandeDataDTO> datasDto = DemandesDataTransformer
					.bo2Dto(new ArrayList<>(demandeBo.getData()));
			List<DemandesDataBO> datasBo = DemandesDataTransformer.dto2Bo(datasDto);
			for (DemandesDataBO dataBo : datasBo) {
				dataBo.setPkDemandesData(null);
				dataBo.setFkDemandes(newDemandeBo);
				demandesDataRepository.save(dataBo);
			}
			newDemandeBo.setData(new HashSet<>(datasBo));
		}
	}

    @Override
    public int updateStatuts() {
        LOGGER.info("Début de la méthode DemandesDataServiceImpl.updateStatuts");
        AtomicInteger d = new AtomicInteger();
        try (Stream<DemandesDataBO> demandesFiles = demandesDataRepository.streamAll()) {
            demandesFiles.peek(em::detach)
                    .forEach(data -> {
                        if(data.getKey().equals("IS_EN_ATTENTE_VALIDATION") && data.getValue().equals("1")) {
                            DemandesStatutsBO statut = data.getFkDemandes().getDernierStatut();
                            statut.setName("validationHierarchiqueTask");
                            demandesStatutsRepository.save(statut);
                            LOGGER.info("{} data lus", d.incrementAndGet());
                        }
                    });
        }
        LOGGER.info("Fin de la méthode DemandesDataServiceImpl.updateStatuts");
        return d.get();
    }
}
