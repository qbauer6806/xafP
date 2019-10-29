package mc.gouv.xaf.back.service.data;

import java.util.List;
import java.util.Map;

import mc.gouv.xaf.shared.dto.DemandeDataDTO;

/**
 * Service permettant la manipulation des données d'une demande.
 * 
 * @author qdeme
 *
 */
public interface DemandesDataService {

    /**
     * Permet de récupérer la donnée d'une demande correspondant à un DemandeID et une clé
     * @param demandeData
     * @return La donnée de demande demandée
     */
    public DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key);

    /**
     * Permet de récupérer les données de demande correspondant à un DemandeID
     * @param demandeData
     * @return Les données de demande demandées
     */
    public List<DemandeDataDTO> getDemandeDatas(String demarcheId, Integer demandeId);

    /**
     * Permet de sauvegarder ou mettre à jour une donnée de demande en base
     * @param demandeData
     * @return La donnée de demande sauvegardée ou mise à jour
     * @throws Exception 
     */
    public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value)
            throws Exception;

    /**
     * Permet de supprimer une donnée de demande à partir du DemandeID et de sa clé
     * @param demandeData
     * @throws Exception 
     */
    public void deleteDemandeData(String demarcheId, Integer demandeId, String key) throws Exception;

    /**
     * Méthode permettant de sauvgarder plusieurs données de la demande en base
     * 
     * @param demarcheId Identifiant de la démarche
     * @param demandeId Identifiant de la demande
     * @param datas Données à mettre à jour
     * 
     * @throws Exception Problème de maj
     */
    void saveOrUpdateDemandeDatas(String demarcheId, Integer demandeId, Map<String, String> datas) throws Exception;

}
