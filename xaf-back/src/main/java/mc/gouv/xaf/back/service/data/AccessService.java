package mc.gouv.xaf.back.service.data;

import java.util.List;
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
     * Permet de récupérer un accès à partir de l'UsagerID
     * 
     * @return L'accès demandé
     */
    AccessDTO getAccessActive(Integer usagerId);

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
     * <p>Permet de désactiver un accès à partir de l'UsagerID</p>
     * <p>
     *     Pour des besoins d'archivage et d'accès des demandes associées,
     *     il a été décidé que les suppressions consistent en l'écriture d'un flag Active = false.
     * </p>
     */
    void deleteAccess(Integer usagerId);

    /**
     * Permet de sauvegarder ou de mettre à jour un accès en base
     * 
     * @return L'accès sauvegardé ou mis à jour
     */
    AccessDTO saveOrUpdateAccess(Integer usagerId, AccessDTO access);

    /**
     * Permet de récupérer la liste des usagersIds enregistrés dans la base, pour une démarche
     */
    List<Integer> getUsagersIds();

    /**
     * Similaire à getAccess() mais retourne un BO, pour être utilisé par d'autres classes de la couche service
     */
    AccessBO getAccessBOActive(Integer usagerId);

    /**
     * Similaire à getAccess() mais retourne un BO, pour être utilisé par d'autres classes de la couche service
     */
    AccessBO getAccessBO(Integer usagerId, boolean active);


}
