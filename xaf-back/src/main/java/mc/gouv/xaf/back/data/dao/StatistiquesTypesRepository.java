package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.StatistiquesTypesBO;

public interface StatistiquesTypesRepository extends CrudRepository<StatistiquesTypesBO, Integer> {
	List<StatistiquesTypesBO> findByIdentifiantDemande(String identifiantDemande);
}
