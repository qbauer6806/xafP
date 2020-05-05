package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;

import java.util.List;

/**
 * Service permettant la manipulation des périodes d'ouverture
 *
 * @author qdeme
 */
public interface PeriodesOuvertureService {

    /**
     * Permet de récupérer les motifs correspondant à un DemarcheID
     */
    List<PeriodeOuvertureDTO> getPeriodesOuverture(String demarcheId);

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     */
    PeriodeOuvertureDTO saveOrUpdatePeriodeOuverture(String demarcheId, PeriodeOuvertureDTO periodeOuverture);

    /**
     * Permet de supprimer une période d'ouverture à partir du DemarcheID et du PeriodeOuvertureID
     */
    void deletePeriodeOuverture(String demarcheId, Integer pkPeriodeOuverture);

    /**
     * Permet de supprimer toutes les périodes d'ouverture liées à une démarche
     * @param demarcheId l'id de la démarche
     */
    void deleteAllPeriodeOuverture(String demarcheId);

}
