package mc.gouv.af.back.data.es.dao;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.data.es.model.DemandeEsDTO;

@Conditional(IndexationEnabledCondition.class)
public interface DemandeEsRepository extends ElasticsearchRepository<DemandeEsDTO, String> {

    long deleteByIdentifiantIn(List<String> ids);

}
