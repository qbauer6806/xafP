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
     * Vérification de la clé de la propriété pour garantir l'unicité
     *
     * @param toCheck  La propriété à vérifier
     * @param isCreate Flag indiquant si l'action est une création
     * @return un boolean contenant le résultat
     */
    boolean checkProperty(PropertiesDTO toCheck, boolean isCreate);
    
    /**
     * Récupérer une Property par sa clé
     * @param demarcheId
     * @param key
     * @return
     */
    PropertiesDTO getProperty(String demarcheId, String key);
}
