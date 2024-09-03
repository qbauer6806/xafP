package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service permettant la manipulation des périodes d'ouverture
 *
 * @author qdeme
 */
public interface PeriodesOuvertureService {

    /**
     * Permet de récupérer les périodes
     */
    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    /**
     * Permet de récupérer les périodes correspondant en mode paginé
     */
    Page<PeriodeOuvertureDTO> getPeriodesOuverturePageable(Pageable pageable);

    /**
     * Permet de récupérer la dernière période d'ouverture terminée
     */
    PeriodeOuvertureDTO getDernierePeriodeOuvertureTerminee();

    /**
     * Permet de récupérer toutes les périodes d'ouverture dans le futur
     */
    List<PeriodeOuvertureDTO> getPeriodesOuvertureFutures();

    /**
     * Permet de récupérer toutes les périodes d'ouverture en cours
     */
    List<PeriodeOuvertureDTO> getPeriodesOuvertureEnCours();

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     */
    PeriodeOuvertureDTO saveOrUpdatePeriodeOuverture(PeriodeOuvertureDTO periodeOuverture);

    /**
     * Permet de supprimer une période d'ouverture à partir du PeriodeOuvertureID
     */
    void deletePeriodeOuverture(Integer pkPeriodeOuverture);

    /**
     * Permet de supprimer toutes les périodes d'ouverture liées à une démarche
     */
    void deleteAllPeriodeOuverture();

}
