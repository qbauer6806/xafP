package mc.gouv.xaf.back.service.data;

import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

/**
 * Service permettant la manipulation des données d'une demande.
 *
 * @author qdeme
 */
public interface DemandesDataService {

    /**
     * Permet de récupérer la donnée d'une demande correspondant à un DemandeID et une clé
     *
     * @return La donnée de demande demandée
     */
    DemandeDataDTO getDemandeData(Integer demandeId, String key);

    /**
     * Permet de récupérer la donnée d'une demande correspondant à un DemandeID et une clé, en vérifiant l'existence de
     * la demande via le paramètre checkActive
     *
     * @return La donnée de demande demandée
     */
    DemandeDataDTO getDemandeData(Integer demandeId, String key, boolean checkActive);

    /**
     * Permet de récupérer les données de demande correspondant à un DemandeID
     *
     * @return Les données de demande demandées
     */
    List<DemandeDataDTO> getDemandeDatas(Integer demandeId);

    DemandeDataDTO[] getDemandeDatasProjection(Integer demandeId);

    /**
     * Permet de récupérer les data des demandes associées au couple key / value
     *
     * @return Les données de demande associées
     */
    List<DemandeDataDTO> getDemandeDatasByKeyAndValue(String key, String value);

    /**
     * Permet de récupérer les data associées au couple key / value parmis la liste donnée
     */
    List<DemandeDataDTO> getDemandeDatasByKeyAndValueAndfkDemandes(String key, String value, List<DemandeBO> demandes);

    /**
     * Récupère une liste de DemandeDataDTO dont la clé étrangère de la demande correspond à la valeur fournie et dont
     * la clé commence par le préfixe spécifié.
     *
     * @param fkDemandes
     *         La clé étrangère de la demande permettant de filtrer les données.
     * @param key
     *         Le préfixe que les clés des données retournées doivent commencer.
     * @return Une liste de DemandeDataDTO correspondant aux critères.
     */
    List<DemandeDataDTO> getDemandeDatasByFkDemandesPkDemandesAndKeyStartsWith(Integer fkDemandes, String key);
    /**
     * Permet de sauvegarder ou mettre à jour une donnée de demande en base
     *
     * @return La donnée de demande sauvegardée ou mise à jour
     */
    DemandeDataDTO saveOrUpdateDemandeData(Integer demandeId, String key, String value);

    /**
     * Permet de sauvegarder ou mettre à jour une donnée de demande en base
     *
     * @return La donnée de demande sauvegardée ou mise à jour
     */
    DemandeDataDTO saveOrUpdateDemandeData(Integer demandeId, String key, String value, boolean checkActive);

    /**
     * Permet de mettre à jour en base une donnée de demande
     *
     * @return la donnée mise à jour
     */
    DemandeDataDTO updateDemandeData(DemandeDataDTO dataDTO);

    /**
     * Permet de supprimer une donnée de demande à partir du DemandeID et de sa clé
     */
    void deleteDemandeData(Integer demandeId, String key);

    /**
     * <p>Méthode permettant de sauvegarder plusieurs données de la demande en base.</p>
     *
     * @param demandeId
     *         Identifiant de la demande
     * @param datas
     *         Données à mettre à jour
     */
    void saveOrUpdateDemandeDatas(Integer demandeId, Map<String, String> datas);

    /**
     * Duplication des données d'une demande vers une nouvelle demande.
     *
     * @param demandeBo
     *         L'objet BO de la demande à cloner
     * @param newDemandeBo
     *         le nouvel objet BO
     */
    void clonerDemandeData(DemandeBO demandeBo, DemandeBO newDemandeBo);

}
