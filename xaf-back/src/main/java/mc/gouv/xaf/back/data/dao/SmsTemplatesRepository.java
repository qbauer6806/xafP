package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.SmsTemplateBO;

/**
 * @author qdeme
 */
public interface SmsTemplatesRepository extends CrudRepository<SmsTemplateBO, Integer> {

    List<SmsTemplateBO> findAll();

    List<SmsTemplateBO> findByLangue(String langue);

    SmsTemplateBO findByPkSmsTemplates(Integer pkTemplates);

    SmsTemplateBO findByCodeAndLangue(String code, String langue);

}