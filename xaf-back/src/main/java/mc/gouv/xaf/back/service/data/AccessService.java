package mc.gouv.xaf.back.service.data;

import java.util.List;
import java.util.Optional;

import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.shared.dto.AccessDTO;

/**
 * Service permettant la manipulation des accès.
 * 
 * @author qdeme
 *
 */
public interface AccessService {

    /**
     * Permet de récupérer un accès à partir du DemarcheID et de l'UsagerID
     * 
     * @param access
     * @return L'accès demandé
     */
    public AccessDTO getAccess(String demarcheId, Integer usagerId);

    /**
     * Permet de récupérer un accès à partir du pkAccess
     * 
     * @param access
     * @return L'accès demandé
     */
    public AccessDTO getAccess(Integer pkAccess);

    /**
     * Permet de savoir sur l'access est actif ou pas
     * 
     * @param pkAccess
     * @return
     */
    public Boolean isAccessActive(Integer pkAccess);

    /**
     * Permet de supprimer un accès à partir du DemarcheID et de l'UsagerID
     * 
     * @param access
     * @param usagerId
     */
    public void deleteAccess(String demarcheId, Integer usagerId);

    /**
     * Permet de sauvegarder ou de mettre à jour un accès en base
     * 
     * @param access
     * @return L'accès sauvegardé ou mis à jour
     */
    public AccessDTO saveOrUpdateAccess(String demarcheId, Integer usagerId, AccessDTO access);

    /**
     * Permet de récupérer la liste des usagersIds enregistrés dans la base, pour une démarche
     * 
     * @param demarcheId
     * @return
     */
    public List<Integer> getUsagersIds(String demarcheId);

    /**
     * Similaire à getAccess() mais retourne un BO, pour être utilisé par d'autres classes de la couche service
     * 
     * @param access
     * @return
     */
    public AccessBO getAccessBO(String demarcheId, Integer usagerId);

    /**
     * Similaire à getAccess() mais retourne un BO, pour être utilisé par d'autres classes de la couche service
     * 
     * @param pkAccess
     * @return
     */
    public Optional<AccessBO> getAccessBO(Integer pkAccess);

}
