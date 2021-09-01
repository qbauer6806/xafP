package mc.gouv.xaf.back.data.es.dao;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;

@Conditional(IndexationEnabledCondition.class)
public interface DemandesFilesEsRepository extends ElasticsearchRepository<DemandeFileEsDTO, String>{

}
