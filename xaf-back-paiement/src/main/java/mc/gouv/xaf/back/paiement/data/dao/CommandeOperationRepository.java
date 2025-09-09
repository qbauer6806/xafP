package mc.gouv.xaf.back.paiement.data.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;

public interface CommandeOperationRepository extends JpaRepository<CommandeOperationBO, String> {

    // TODO @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateDerniereModification between :startDate and :endDate")
    @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateCreation between :startDate and :endDate")
    List<CommandeOperationBO> findAllCommandeOperationBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, @Param("statut") OperationStatutEnum statut);

    // TODO @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateDerniereModification >= :startDate")
    @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateCreation >= :startDate")
    List<CommandeOperationBO> findAllCommandeOperationFrom(@Param("startDate") LocalDateTime startDate,
            @Param("statut") OperationStatutEnum statut);

    // TODO @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateDerniereModification <= :endDate")
    @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateCreation <= :endDate")
    List<CommandeOperationBO> findAllCommandeOperationUntil(@Param("endDate") LocalDateTime endDate,
            @Param("statut") OperationStatutEnum statut);

    @Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut")
    List<CommandeOperationBO> findAllCommandeOperation(@Param("statut") OperationStatutEnum statut);

    @Query("select cob from CommandeOperationBO cob where cob.demande.pkDemandes = :fkDemandes")
    List<CommandeOperationBO> findAllByFkDemandes(@Param("fkDemandes") Integer fkDemandes);

    @Query("""
    SELECT co FROM CommandeOperationBO co
    JOIN co.commande c
    WHERE co.operationStatut = :status
      AND co.dateCreation = (
          SELECT MAX(co2.dateCreation)
          FROM CommandeOperationBO co2
          WHERE co2.commande = c
      )
""")
    List<CommandeOperationBO> findLatestCommandesOperationsForStatus(@Param("status") OperationStatutEnum status);

    void deleteByCommande_PkCommandesIn(Set<Integer> ids);
}
