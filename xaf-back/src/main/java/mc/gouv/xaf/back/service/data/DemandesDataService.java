package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

import java.util.List;
import java.util.Map;

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
    DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key);

    /**
     * Permet de récupérer la donnée d'une demande correspondant à un DemandeID et une clé, en vérifiant l'existance de la demande via le paramètre checkActive
     *
     * @return La donnée de demande demandée
     */
    DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key, boolean checkActive);

    /**
     * Permet de récupérer les données de demande correspondant à un DemandeID
     *
     * @return Les données de demande demandées
     */
    List<DemandeDataDTO> getDemandeDatas(String demarcheId, Integer demandeId);

    /**
     * Permet de récupérer les data des demandes associées au couple key / value
     *
     * @return Les données de demande associées
     */
    List<DemandeDataDTO> getDemandeDatasByKeyAndValue(String key, String value);

    /**
     * Permet de récupérer les data associées au couple key / value parmis la liste donnée
     */
    List<DemandeDataDTO> getDemandeDatasByKeyAndValueAndfkDemandes(String key, String value,
                                                                   List<DemandeBO> demandes);

    /**
     * Permet de sauvegarder ou mettre à jour une donnée de demande en base
     *
     * @return La donnée de demande sauvegardée ou mise à jour
     */
    DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value);

    /**
     * Permet de sauvegarder ou mettre à jour une donnée de demande en base
     *
     * @return La donnée de demande sauvegardée ou mise à jour
     */
    DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value, boolean checkActive);

    /**
     * Permet de mettre à jour en base une donnée de demande
     *
     * @return la donnée mise à jour
     */
    DemandeDataDTO updateDemandeData(DemandeDataDTO dataDTO);

    /**
     * Permet de supprimer une donnée de demande à partir du DemandeID et de sa clé
     */
    void deleteDemandeData(String demarcheId, Integer demandeId, String key);

    /**
     * Méthode permettant de sauvgarder plusieurs données de la demande en base
     *
     * @param demarcheId Identifiant de la démarche
     * @param demandeId  Identifiant de la demande
     * @param datas      Données à mettre à jour
     */
    void saveOrUpdateDemandeDatas(String demarcheId, Integer demandeId, Map<String, String> datas);

}
