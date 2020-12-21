package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.DemandeBO;

import java.util.List;

public interface DemandesStatistiquesInternesRepository extends CrudRepository<DemandeBO, Integer> {

    Long countByFkAccessDemarcheIdAndCanalAndDernierStatutLibelle(String demarcheId, String canal, String status);

    Long countByPkDemandesInAndCanal(List<Integer> ids, String canal);

}
