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

    List<TemplateBO> findAll();

    List<TemplateBO> findByLangue(String langue);

    TemplateBO findByPkTemplates(Integer pkTemplates);
    
    TemplateBO findByCodeAndLangue(String code, String langue);
    
}
