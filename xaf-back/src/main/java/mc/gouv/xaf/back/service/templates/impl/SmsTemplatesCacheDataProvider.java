package mc.gouv.xaf.back.service.templates.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.sms.SmsTemplatesService;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;

/**
 * Data provider du cache de templates SMS
 *
 * @author qdeme
 */
@Profile("gouv")
@Component
public class SmsTemplatesCacheDataProvider implements GouvCacheDataProvider<Integer, SmsTemplateDTO> {

    @Autowired
    private SmsTemplatesService smsTemplatesService;

    @Override
    public ConcurrentHashMap<Integer, SmsTemplateDTO> getAll() {
        ConcurrentHashMap<Integer, SmsTemplateDTO> ret = new ConcurrentHashMap<>();
        List<SmsTemplateDTO> templates = smsTemplatesService.getTemplates();
        for (SmsTemplateDTO template : templates) {
            ret.put(template.getPkSmsTemplates(), template);
        }
        return ret;
    }

    @Override
    public SmsTemplateDTO get(Integer key) {
        return smsTemplatesService.getTemplate(key);
    }

}
