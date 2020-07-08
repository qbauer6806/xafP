package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;

/**
 * 
 * Service permettant la manipulation des périodes d'ouverture
 * 
 * @author qdeme
 *
 */
public interface PeriodesOuvertureService {
    
    /**
     * Permet de récupérer les motifs correspondant à un DemarcheID
     */
    public List<PeriodeOuvertureDTO> getPeriodesOuverture(String demarcheId);

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     */
    public PeriodeOuvertureDTO saveOrUpdatePeriodeOuverture(String demarcheId, PeriodeOuvertureDTO periodeOuverture);
    
    /**
     * Permet de supprimer une période d'ouverture à partir du DemarcheID et du PeriodeOuvertureID
     */
    public void deletePeriodeOuverture(String demarcheId, Integer pkPeriodeOuverture);
    
}
