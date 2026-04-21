package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesDataRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.projection.DemandeDataExportProjection;
import mc.gouv.xaf.back.data.transformer.DemandesDataTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des données d'une demande.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesDataServiceImpl implements DemandesDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesDataServiceImpl.class);

    private final DemandesDataRepository demandesDataRepository;
    private final TransactionErrorsHandler transactionErrorsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandesHelperService demandesHelperService;

    @Override
    public DemandeDataDTO getDemandeData(Integer demandeId, String key) {
        return getDemandeData(demandeId, key, true);
    }

    @Override
    public DemandeDataDTO getDemandeData(Integer demandeId, String key, boolean checkActive) {

        // Jette une exception si la demande n'existe pas
        if (checkActive) {
            demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);
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
    public List<DemandeDataDTO> getDemandeDatas(Integer demandeId) {

        // Jette une exception si la demande n'existe pas
        demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);

        LOGGER.info("Récupération en base de la donnée de demande...");

        List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByFkDemandesPkDemandes(demandeId);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

        return DemandesDataTransformer.bo2Dto(demandesDatasBo);
    }

    @Override
    public DemandeDataDTO[] getDemandeDatasProjection(Integer demandeId) {
        List<DemandeDataExportProjection> demandesDatasBo = demandesDataRepository.findByFkDemandes_PkDemandes(
                demandeId);
        return DemandesDataTransformer.exportProjections2Dto(demandesDatasBo);
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
        List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByKeyAndValueAndFkDemandesIn(key, value,
                demandeIds);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesDataTransformer.bo2Dto(demandesDatasBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDataDTO> getDemandeDatasByFkDemandesPkDemandesAndKeyStartsWith(Integer fkDemandes,
            String keyPrefix) {
        LOGGER.info("Récupération en base des demandes data pour FK demande {} et Key prefix {}...", fkDemandes,
                keyPrefix);
        List<DemandesDataBO> demandesDatasBo = demandesDataRepository.findByFkDemandesPkDemandesAndKeyStartsWith(
                fkDemandes, keyPrefix);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesDataTransformer.bo2Dto(demandesDatasBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDataDTO saveOrUpdateDemandeData(Integer demandeId, String key, String value) {
        return saveOrUpdateDemandeData(demandeId, key, value, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDataDTO saveOrUpdateDemandeData(Integer demandeId, String key, String value, boolean checkActive) {
        // Jette une exception si la demande n'existe pas
        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, checkActive);
        return saveOrUpdateDemandeDatas(demandeBo, key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveOrUpdateDemandeDatas(Integer demandeId, Map<String, String> datas) {

        // Jette une exception si la demande n'existe pas
        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);

        if (datas != null) {
            for (Map.Entry<String, String> entry : datas.entrySet()) {
                saveOrUpdateDemandeDatas(demandeBo, entry.getKey(), entry.getValue());
            }
        }
    }

    private DemandeDataDTO saveOrUpdateDemandeDatas(DemandeBO demandeBo, String key, String value) {
        try {
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
        } catch (Exception e) {
            LOGGER.error("Erreur lors de saveOrUpdateDemandeDatas");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesDataServiceImpl - méthode saveOrUpdateDemandeDatas()", demandeBo.getPkDemandes(), e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    public DemandeDataDTO updateDemandeData(DemandeDataDTO dataDTO) {
        try {
            DemandesDataBO dataBO = DemandesDataTransformer.dto2Bo(dataDTO);
            DemandeBO demande = new DemandeBO();
            demande.setPkDemandes(dataDTO.getDemandeId());
            dataBO.setFkDemandes(demande);
            dataBO = demandesDataRepository.save(dataBO);
            return DemandesDataTransformer.bo2Dto(dataBO);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de updateDemandeData");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesDataServiceImpl - méthode updateDemandeData()", dataDTO.getDemandeId(), e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDemandeData(Integer demandeId, String key) {
        try {
            // Jette une exception si la demande n'existe pas
            demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);

            DemandesDataBO demandesDataBo = getDemandeDataBO(demandeId, key);

            LOGGER.info("Suppression de la donnée de demande...");
            if (demandesDataBo != null) {
                demandesDataBo.getFkDemandes().getData().remove(demandesDataBo);
                demandesDataRepository.delete(demandesDataBo);
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de deleteDemandeData");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesDataServiceImpl - méthode deleteDemandeData()", demandeId, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public void clonerDemandeData(DemandeBO demandeBo, DemandeBO newDemandeBo) {
        if (demandeBo.getData() != null) {
            LOGGER.info("Dupliquer des données de la demande");
            List<DemandeDataDTO> datasDto = DemandesDataTransformer.bo2Dto(new ArrayList<>(demandeBo.getData()));
            List<DemandesDataBO> datasBo = DemandesDataTransformer.dto2Bo(datasDto);
            for (DemandesDataBO dataBo : datasBo) {
                dataBo.setPkDemandesData(null);
                dataBo.setFkDemandes(newDemandeBo);
                demandesDataRepository.save(dataBo);
            }
            newDemandeBo.setData(new HashSet<>(datasBo));
        }
    }

}
