package mc.gouv.xaf.back.service.data;

import java.util.List;
import java.util.Optional;

import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.shared.dto.AccessDTO;

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
     * @return L'accès demandé
     */
    AccessDTO getAccess(String demarcheId, Integer usagerId);

    /**
     * Permet de récupérer un accès à partir du pkAccess
     * 
     * @return L'accès demandé
     */
    AccessDTO getAccess(Integer pkAccess);

    /**
     * Permet de savoir sur l'access est actif ou pas
     */
    boolean isAccessActive(Integer pkAccess);

    /**
     * <p>Permet de désactiver un accès à partir du DemarcheID et de l'UsagerID</p>
     * <p>
     *     Pour des besoins d'archivage et d'accès des demandes associées,
     *     il a été décidé que les suppressions consistent en l'écriture d'un flag Active = false.
     * </p>
     */
    void deleteAccess(String demarcheId, Integer usagerId);

    /**
     * Permet de sauvegarder ou de mettre à jour un accès en base
     * 
     * @return L'accès sauvegardé ou mis à jour
     */
    AccessDTO saveOrUpdateAccess(String demarcheId, Integer usagerId, AccessDTO access);

    /**
     * Permet de récupérer la liste des usagersIds enregistrés dans la base, pour une démarche
     */
    List<Integer> getUsagersIds(String demarcheId);

    /**
     * Similaire à getAccess() mais retourne un BO, pour être utilisé par d'autres classes de la couche service
     */
    AccessBO getAccessBO(String demarcheId, Integer usagerId);

    /**
     * Similaire à getAccess() mais retourne un BO, pour être utilisé par d'autres classes de la couche service
     */
    Optional<AccessBO> getAccessBO(Integer pkAccess);

}
