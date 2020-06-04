package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.Static;
import mc.gouv.xaf.back.data.dao.PropertiesRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
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

    private static final PropertiesTypeEnum[] FRONT_PROPERTIES = {PropertiesTypeEnum.FRONT_AF,
            PropertiesTypeEnum.FRONT_GEST, PropertiesTypeEnum.BACKFRONT_AF, PropertiesTypeEnum.BACKFRONT_GEST};
    private static final PropertiesTypeEnum[] AF_PROPERTIES = {PropertiesTypeEnum.FRONT_AF,
            PropertiesTypeEnum.BACKFRONT_AF, PropertiesTypeEnum.BACK_AF};

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
     * Récupère les Properties d'une démarche liées à une liste de types
     *
     * @param types La liste de types à filtrer
     * @return une List de Properties
     */
    @Override
    public List<PropertiesDTO> getPropertiesByTypeList(List<PropertiesTypeEnum> types) {
        List<PropertiesDTO> result = new ArrayList<>();
        if (types.isEmpty()) {
            LOGGER.info("La liste des types est vide !");
        } else {
            List<String> typeStr = new ArrayList<>(types.size());
            types.forEach(type -> typeStr.add(type.name()));
            LOGGER.info("Récupération en base des propriétés ...");
            String demarcheId = gouvPropertiesResolver.getDemarcheId();
            List<PropertiesBO> bos = propertiesRepository.findAllInListOfTypes(demarcheId, typeStr);
            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            result = PropertiesTransformer.bo2Dto(bos);
        }
        return result;
    }

    @Override
    public List<PropertiesDTO> getFrontProperties() {
        List<PropertiesTypeEnum> types = Arrays.asList(FRONT_PROPERTIES);
        List<PropertiesDTO> propertiesDTOS = getPropertiesByTypeList(types);
        // On ajoute la propriété ici, car elle n'est pas disponible dans la PropertiesServlet
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.piwik.external.piwikUrl", Static.getValue("mc.gouv.piwik.external.piwikUrl")));
        return propertiesDTOS;
    }

    @Override
    public List<PropertiesDTO> getAdminsFonctionnelsProperties() {
        List<PropertiesTypeEnum> types = Arrays.asList(AF_PROPERTIES);
        return getPropertiesByTypeList(types);
    }

    /**
     * Ajoute ou mets à jour la valeur d'une Properties
     *
     * @return le dto de la propriété sauvée
     */
    @Override
    public PropertiesDTO updatePropertyValue(Integer pkProperties, String value) {
        LOGGER.info("Vérification de l'existance de la propriété {} ...", pkProperties);
        Optional<PropertiesBO> propertiesBoOpt = propertiesRepository.findById(pkProperties);
        if (!propertiesBoOpt.isPresent()) {
            throw new DemarchesServiceException("La propriété spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }
        PropertiesBO bo = propertiesBoOpt.get();
        bo.setValue(value);
        bo = propertiesRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PropertiesTransformer.bo2Dto(bo);
    }

    /**
     * Récupérer une Property par sa clé
     * @param demarcheId
     * @param key
     * @return
     */
	@Override
	public PropertiesDTO getProperty(String demarcheId, String key) {
        Optional<PropertiesBO> propertiesBoOptional = propertiesRepository.findByDemarchePkDemarchesAndKey(demarcheId, key);
        if (propertiesBoOptional.isPresent()) {
            PropertiesBO propertiesBO = propertiesBoOptional.get();
            return PropertiesTransformer.bo2Dto(propertiesBO);
        }
        return null;
	}
}
