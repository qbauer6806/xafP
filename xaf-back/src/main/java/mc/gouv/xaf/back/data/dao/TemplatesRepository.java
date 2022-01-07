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

    public List<TemplateBO> findByDemarcheId(String demarcheId);

    public List<TemplateBO> findByDemarcheIdAndLangue(String demarcheId, String langue);

    public TemplateBO findByDemarcheIdAndPkTemplates(String demarcheId, Integer pkTemplates);
    
    public TemplateBO findByDemarcheIdAndCodeAndLangue(String demarcheId, String code, String langue);
    
}
