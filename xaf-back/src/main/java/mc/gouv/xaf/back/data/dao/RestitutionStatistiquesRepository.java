package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.RestitutionStatistiquesBO;

public interface RestitutionStatistiquesRepository extends CrudRepository<RestitutionStatistiquesBO, Integer> {

    List<RestitutionStatistiquesBO> findByUsagerId(Integer usagerId);

    List<RestitutionStatistiquesBO> findByHttpCode(Integer httpCode);

}
