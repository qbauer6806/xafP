package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.PropertiesRepository;
import mc.gouv.xaf.back.data.entity.PropertiesBO;
import mc.gouv.xaf.back.data.transformer.PropertiesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.templates.AfPropertiesTemplateProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.TemplateUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesListEntityDTO;
import mc.gouv.xaf.shared.enums.PropertiesTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des Properties d'une démarche
 *
 * @author mboutelier.ext
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PropertiesServiceImpl implements PropertiesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesServiceImpl.class);

    private static final PropertiesTypeEnum[] FRONT_PROPERTIES = { PropertiesTypeEnum.FRONT_AF,
            PropertiesTypeEnum.FRONT_GEST, PropertiesTypeEnum.BACKFRONT_AF, PropertiesTypeEnum.BACKFRONT_GEST };
    private static final PropertiesTypeEnum[] AF_PROPERTIES = { PropertiesTypeEnum.FRONT_AF,
            PropertiesTypeEnum.BACKFRONT_AF, PropertiesTypeEnum.BACK_AF };

    private static final String AUTRE = "AUTRE";

    private final PropertiesRepository propertiesRepository;

    private final TemplateUtils templateUtils;

    private final ObjectProvider<AfPropertiesTemplateProvider> afPropertiesTemplateProvider;
    /**
     * Récupère toute les Properties liées à une démarche
     *
     * @return une List de PropertiesDTO
     */
    @Override
    public List<PropertiesDTO> getProperties() {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        List<PropertiesBO> bos = propertiesRepository.findAll();
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PropertiesTransformer.bo2Dto(bos);
    }

    /**
     * Récupère les Properties d'une démarche liées à un certain type
     *
     * @param type
     *         Le type d'enum à filtrer
     * @return une List de Properties
     */
    @Override
    public List<PropertiesDTO> getPropertiesByType(PropertiesTypeEnum type) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE, type.name());
        List<PropertiesBO> bos = propertiesRepository.findByType(type.name());
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PropertiesTransformer.bo2Dto(bos);
    }

    /**
     * Récupère les Properties d'une démarche liées à une liste de types
     *
     * @param types
     *         La liste de types à filtrer
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
            LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
            List<PropertiesBO> bos = propertiesRepository.findByTypeIn(typeStr);
            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            result = PropertiesTransformer.bo2Dto(bos);
        }
        return result;
    }

    @Override
    public List<PropertiesDTO> getFrontProperties() {
        List<PropertiesTypeEnum> types = Arrays.asList(FRONT_PROPERTIES);
        List<PropertiesDTO> propertiesByTypeList = getPropertiesByTypeList(types);
        //#28502 - [DEV] Gestion de l'édition des "properties" format JSON
        // Je tri la value de toutes les propriétées prefixées par LISTE_
        for (PropertiesDTO propertiesDTO : propertiesByTypeList) {
            if (propertiesDTO.getKey().startsWith("LISTE_")) {
                sortValueOfGivenProperty(propertiesDTO);
            }
        }
        AfPropertiesTemplateProvider provider = afPropertiesTemplateProvider.getIfAvailable();
        if (provider == null) {
            return propertiesByTypeList;
        }

        for (PropertiesDTO dto : propertiesByTypeList) {

            String key = dto.getKey();
            String template = dto.getValue();

            if (key == null  || template == null) {
                continue;
            }

            Map<String, String> model = provider.getModel(key);
            if (model == null || model.isEmpty()) {
                continue;
            }
            try {
                dto.setValue(templateUtils.renderWithVelocity(template, model));
            } catch (Exception e) {
                LOGGER.warn("Erreur rendu template Velocity pour key={}", key, e);
            }
        }
        return propertiesByTypeList;
    }

    private void sortValueOfGivenProperty(PropertiesDTO propertiesDTO) {
        List<PropertiesListEntityDTO> jsonObjectsToDisplay;
        // Récupération du json représentant le fichier
        ObjectMapper mapper = new ObjectMapper();
        if (!StringUtils.isEmpty(propertiesDTO.getValue())) {
            try {
                jsonObjectsToDisplay = Arrays.asList(
                        mapper.readValue(propertiesDTO.getValue(), PropertiesListEntityDTO[].class));
                jsonObjectsToDisplay.sort((p1, p2) -> {
                    // Si p1 est "AUTRE", il doit être placé avant p2
                    if (p1.getLabel().equalsIgnoreCase(AUTRE) || p1.getId().equalsIgnoreCase(AUTRE)) {
                        return -1;
                    }
                    // Si p2 est "AUTRE", il doit être placé avant p1
                    if (p2.getLabel().equalsIgnoreCase(AUTRE) || p2.getId().equalsIgnoreCase(AUTRE)) {
                        return 1;
                    }
                    // Sinon, on compare les labels normalement (en majuscules pour être insensible à la casse)
                    return p1.getLabel().toUpperCase().compareTo(p2.getLabel().toUpperCase());
                });
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                mapper.writeValue(out, jsonObjectsToDisplay);
                propertiesDTO.setValue(out.toString());
            } catch (Exception e) {
                LOGGER.error("Erreur dans sortValueOfGivenProperty", e);
            }
        }
    }

    @Override
    public List<PropertiesDTO> getAdminsFonctionnelsProperties() {
        List<PropertiesTypeEnum> types = Arrays.asList(AF_PROPERTIES);
        return getPropertiesByTypeList(types);
    }

    /**
     * Ajoute ou mets à jour une Properties
     *
     * @param toSave
     *         La propriété à sauvegarder
     * @return la Properties sauvée
     */
    @Override
    public PropertiesDTO saveOrUpdateProperties(PropertiesDTO toSave) {
        PropertiesDTO saved;

        if (toSave.getPkProperties() == null) {
            LOGGER.info("Création d'une nouvelle propriété ...");
            LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
            PropertiesBO bo = PropertiesTransformer.dto2Bo(toSave);
            bo = propertiesRepository.save(bo);
            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            saved = PropertiesTransformer.bo2Dto(bo);
        } else {
            LOGGER.info("Mise à jour d'une propriété");
            Optional<PropertiesBO> propertiesBoOpt = propertiesRepository.findById(toSave.getPkProperties());
            if (propertiesBoOpt.isEmpty()) {
                throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
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
     * @param propertiesId
     *         L'id de la propriété à supprimer
     */
    @Override
    public void deleteProperties(Integer propertiesId) {
        Optional<PropertiesBO> propertiesBoOpt = propertiesRepository.findById(propertiesId);
        if (propertiesBoOpt.isEmpty()) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Suppression de la propriété ...");
        propertiesRepository.delete(propertiesBoOpt.get());
    }

    /**
     * Récupérer une Property par sa clé
     *
     * @param key
     *         la clé de la propriété à récupérer
     * @return le PropertiesDTO correspondant
     */
    @Override
    public PropertiesDTO getProperty(String key) {
        Optional<PropertiesBO> propertiesBoOptional = propertiesRepository.findByKey(key);
        if (propertiesBoOptional.isPresent()) {
            PropertiesBO propertiesBO = propertiesBoOptional.get();
            return PropertiesTransformer.bo2Dto(propertiesBO);
        }
        return null;
    }

    @Override
    public String getPropertyPourRecap(String key, JsonNode pathNode, boolean recap) {
        PropertiesDTO prop = getProperty(key);
        if (prop != null) {
            PropertiesListEntityDTO[] entreprises = AfBackUtils.parserPropertiesListJson(prop.getValue());
            if (null == entreprises || entreprises.length == 0) {
                LOGGER.warn("Impossible de transformer la valeur de la dem_property (key={}) en map", key);
                return "ERREUR";
            }
            Optional<PropertiesListEntityDTO> matchingObject = Arrays.stream(entreprises)
                    .filter(e -> e.getId().equals(pathNode.asText())).findFirst();
            String result = matchingObject.map(PropertiesListEntityDTO::getLabel).orElse(null);
            if (null != result) {
                // refs #33280 - [BO] Traitement de la demande - Erreur 500 suite à tentative de génération du récap pour une demande ayant ''&" dans le nom de l'entreprise partenaire
                return recap ? result.replace("&", "&#38;").replace("<", "&lt;").replace(">", "&gt;") : result;
            } else {
                return "";
            }
        } else {
            LOGGER.warn("Impossible de récupérer la dem_property requise par le fichier récap (key={})", key);
            return "ERREUR";
        }
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
        if (propertiesBoOpt.isEmpty()) {
            throw new DemarchesServiceException("La propriété spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }
        PropertiesBO bo = propertiesBoOpt.get();
        bo.setValue(value);
        bo = propertiesRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PropertiesTransformer.bo2Dto(bo);
    }

}
