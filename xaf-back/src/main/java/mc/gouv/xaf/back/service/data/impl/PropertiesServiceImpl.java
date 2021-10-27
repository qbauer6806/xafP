package mc.gouv.xaf.back.service.data.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import mc.gouv.xaf.shared.dto.PropertiesListEntityDTO;
import mc.gouv.xaf.shared.dto.PropertiesTypeEnum;

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
        List<PropertiesDTO> propertiesByTypeList = getPropertiesByTypeList(types);
        //#28502 - [DEV] Gestion de l'édition des "properties" format JSON
        // Je tri la value de toutes les propriétées prefixées par LISTE_
        for (PropertiesDTO propertiesDTO : propertiesByTypeList) {
			if (propertiesDTO.getKey().startsWith("LISTE_")) {
				sortValueOfGivenProperty(propertiesDTO);
			}
		}
		return propertiesByTypeList;
    }

    private void sortValueOfGivenProperty(PropertiesDTO propertiesDTO) {
    	List<PropertiesListEntityDTO> jsonObjectsToDisplay = new ArrayList<PropertiesListEntityDTO>();
    	// Récupération du json représentant le fichier
    	ObjectMapper mapper = new ObjectMapper();
    	if (!StringUtils.isEmpty(propertiesDTO.getValue())) {
			try {
				jsonObjectsToDisplay = Arrays.asList(mapper.readValue(propertiesDTO.getValue(), PropertiesListEntityDTO[].class));
				Collections.sort(jsonObjectsToDisplay, new Comparator<PropertiesListEntityDTO>() {
		    		  @Override
		    		  public int compare(PropertiesListEntityDTO p1, PropertiesListEntityDTO p2) {
		    			  // On veut laisser le libelle Autre en 1ere position dans la liste
		    			  if (p1.getLabel().equals("AUTRE") || p2.getLabel().equals("AUTRE")) {
		    				  // je retourne 1 si AUTRE commme ça il reste au début de la liste
		    				  return 1;
		    			  } 
		    		    return p1.getLabel().toUpperCase().compareTo(p2.getLabel().toUpperCase());
		    		  }
		    	});
				ByteArrayOutputStream out = new ByteArrayOutputStream();
		    	mapper.writeValue(out, jsonObjectsToDisplay);
				final byte[] valueToAdd = out.toByteArray();
				propertiesDTO.setValue(new String(valueToAdd));
			} catch (Exception e) {
				e.printStackTrace();
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
     * Récupérer une Property par sa clé
     *
     * @param demarcheId l'id de la démarche
     * @param key la clé de la propriété à récupérer
     * @return le PropertiesDTO correspondant
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

}
