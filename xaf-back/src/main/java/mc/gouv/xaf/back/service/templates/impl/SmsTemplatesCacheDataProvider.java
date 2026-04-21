package mc.gouv.xaf.back.service.templates.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.itg.sms.SmsTemplatesService;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;
import org.springframework.stereotype.Component;

/**
 * Data provider du cache de templates SMS
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class SmsTemplatesCacheDataProvider implements GouvCacheDataProvider<Integer, SmsTemplateDTO> {

    private final SmsTemplatesService smsTemplatesService;

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
