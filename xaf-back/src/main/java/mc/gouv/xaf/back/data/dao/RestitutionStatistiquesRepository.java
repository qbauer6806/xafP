package mc.gouv.xaf.back.data.dao;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.RestitutionStatistiquesBO;

public interface RestitutionStatistiquesRepository extends CrudRepository<RestitutionStatistiquesBO, Integer> {

    RestitutionStatistiquesBO findByUsagerId(Integer usagerId);

    RestitutionStatistiquesBO findByHttpCode(Integer httpCode);

}
