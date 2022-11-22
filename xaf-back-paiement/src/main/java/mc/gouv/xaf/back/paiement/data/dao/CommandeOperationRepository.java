package mc.gouv.xaf.back.paiement.data.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;

public interface CommandeOperationRepository extends JpaRepository<CommandeOperationBO, String> {

	@Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateDerniereModification between :startDate and :endDate")
	List<CommandeOperationBO> findAllCommandeOperationBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("statut") OperationStatutEnum statut);
	
	@Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateDerniereModification >= :startDate")
	List<CommandeOperationBO> findAllCommandeOperationFrom(@Param("startDate") LocalDateTime startDate, @Param("statut") OperationStatutEnum statut);
	
	@Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut and cob.dateDerniereModification <= :endDate")
	List<CommandeOperationBO> findAllCommandeOperationUntil(@Param("endDate") LocalDateTime endDate, @Param("statut") OperationStatutEnum statut);
	
	@Query("select cob from CommandeOperationBO cob where cob.operationStatut = :statut")
	List<CommandeOperationBO> findAllCommandeOperation(@Param("statut") OperationStatutEnum statut);

}
