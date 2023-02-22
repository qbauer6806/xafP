package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.TemplateBO;

/**
 * 
 * @author qdeme
 *
 */
public interface TemplatesRepository extends CrudRepository<TemplateBO, Integer> {

    List<TemplateBO> findByDemarcheId(String demarcheId);

    List<TemplateBO> findByDemarcheIdAndLangue(String demarcheId, String langue);

    TemplateBO findByDemarcheIdAndPkTemplates(String demarcheId, Integer pkTemplates);
    
    TemplateBO findByDemarcheIdAndCodeAndLangue(String demarcheId, String code, String langue);
    
}
