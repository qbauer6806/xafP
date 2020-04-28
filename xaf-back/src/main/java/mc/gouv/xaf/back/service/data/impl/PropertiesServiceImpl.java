package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemarchesRepository;
import mc.gouv.xaf.back.data.dao.PropertiesRepository;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.back.data.entity.PropertiesBO;
import mc.gouv.xaf.back.data.transformer.PropertiesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service permettant la manipulation des Properties d'une démarche
 *
 * @author mboutelier.ext
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class PropertiesServiceImpl implements PropertiesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesServiceImpl.class);

    @Autowired
    private DemarchesRepository demarchesRepository;

    @Autowired
    private PropertiesRepository propertiesRepository;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * Récupère toute les Properties liées à une démarche
     *
     * @return une List de PropertiesDTO
     */
    @Override
    public List<PropertiesDTO> getProperties() {
        LOGGER.info("Récupération en base des propriétés ...");
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        List<PropertiesBO> bos = propertiesRepository.findByDemarchePkDemarches(demarcheId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PropertiesTransformer.bo2Dto(bos);
    }

    /**
     * Récupère les Properties d'une démarche liées à un certain type
     *
     * @param type Le type d'enum à filtrer
     * @return une List de Properties
     */
    @Override
    public List<PropertiesDTO> getPropertiesByType(PropertiesTypeEnum type) {
        LOGGER.info("Récupération en base des propriétés ...");
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        List<PropertiesBO> bos = propertiesRepository.findByDemarchePkDemarchesAndType(demarcheId, type.name());
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PropertiesTransformer.bo2Dto(bos);
    }

    /**
     * Ajoute ou mets à jour une Properties
     *
     * @param toSave La propriété à sauvegarder
     * @return la Properties sauvée
     */
    @Override
    public PropertiesDTO saveOrUpdateProperties(PropertiesDTO toSave) {
        PropertiesDTO saved;
        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        // Vérification préalable de l'existence de la démarche
        Optional<DemarchesBO> demarcheBo = demarchesRepository.findById(demarcheId);
        if (!demarcheBo.isPresent()) {
            throw new DemarchesServiceException("La démarche spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }

        if (toSave.getPkProperties() == null) {
            LOGGER.info("Création d'une nouvelle propriété ...");
            LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
            PropertiesBO bo = PropertiesTransformer.dto2Bo(toSave);
            bo.setDemarche(demarcheBo.get());
            bo = propertiesRepository.save(bo);
            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            saved = PropertiesTransformer.bo2Dto(bo);
        } else {
            LOGGER.info("Mise à jour d'une propriété");
            Optional<PropertiesBO> propertiesBoOpt = propertiesRepository.findById(toSave.getPkProperties());
            if (!propertiesBoOpt.isPresent()) {
                throw new DemarchesServiceException("La propriété spécifiée est introuvable", HttpStatus.NOT_FOUND);
            }
            PropertiesBO bo = propertiesBoOpt.get();
            bo.setValue(toSave.getValue());
            bo.setKey(toSave.getKey());
            bo.setType(toSave.getType().name());
            bo = propertiesRepository.save(bo);
            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            saved = PropertiesTransformer.bo2Dto(bo);
        }
        return saved;
    }

    /**
     * Supprime une Properties
     *
     * @param propertiesId L'id de la propriété à supprimer
     */
    @Override
    public void deleteProperties(Integer propertiesId) {
        Optional<PropertiesBO> propertiesBoOpt = propertiesRepository.findById(propertiesId);
        if (!propertiesBoOpt.isPresent()) {
            throw new DemarchesServiceException("La propriété spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Suppression de la propriété ...");
        propertiesRepository.delete(propertiesBoOpt.get());
    }

    /**
     * Vérification de la clé de la propriété pour garantir l'unicité
     *
     * @param toCheck  La propriété à vérifier
     * @param isCreate Flag indiquant si l'action est une création
     * @return un boolean contenant le résultat
     */
    public boolean checkProperty(PropertiesDTO toCheck, boolean isCreate) {
        boolean result = true;
        String key = toCheck.getKey();
        LOGGER.info("Vérification de l'unicité de la clé {} ...", key);

        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        Optional<PropertiesBO> propertiesBoOptional = propertiesRepository.findByDemarchePkDemarchesAndKey(demarcheId, key);
        if (propertiesBoOptional.isPresent()) {
            PropertiesBO propertiesBO = propertiesBoOptional.get();
            result = !isCreate && toCheck.getPkProperties().equals(propertiesBO.getPkProperties());
        }

        String message = result ? "La propriété n'est pas présente" : "La propriété est présente";
        LOGGER.info(message);

        return result;
    }
}
