package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesTypeEnum;

import java.util.List;

/**
 * Service permettant la manipulation des Properties d'une démarche
 *
 * @author mboutelier.ext
 */
public interface PropertiesService {

    /**
     * Récupère toute les Properties liées à une démarche
     *
     * @return une List de PropertiesDTO
     */
    List<PropertiesDTO> getProperties();

    /**
     * Récupère les Properties d'une démarche liées à un certain type
     *
     * @param type Le type d'enum à filtrer
     * @return une List de Properties
     */
    List<PropertiesDTO> getPropertiesByType(PropertiesTypeEnum type);

    /**
     * Récupère les Properties d'une démarche liées à une liste de types
     *
     * @param types La liste de types à filtrer
     * @return une List de Properties
     */
    List<PropertiesDTO> getPropertiesByTypeList(List<PropertiesTypeEnum> types);

    /**
     * Récupère la liste des propriétés FRONT
     */
    List<PropertiesDTO> getFrontProperties();

    /**
     * Récupère la liste des propriétés Admins Fonctionnels
     */
    List<PropertiesDTO> getAdminsFonctionnelsProperties();
    
    /**
     * Ajoute ou mets à jour une Properties
     *
     * @param toSave La propriété à sauvegarder
     * @return la Properties sauvée
     */
    PropertiesDTO saveOrUpdateProperties(PropertiesDTO toSave);

    /**
     * Supprime une Properties
     *
     * @param propertiesId L'id de la propriété à supprimer
     */
    void deleteProperties(Integer propertiesId);
    
    /**
     * Récupérer une Property par sa clé
     * @param demarcheId
     * @param key
     * @return
     */
    PropertiesDTO getProperty(String demarcheId, String key);

	/**
	 * Ajoute ou mets à jour la valeur d'une Properties
	 *
	 * @return le dto de la propriété sauvée
	 */
    PropertiesDTO updatePropertyValue(Integer pkProperties, String value);

}
