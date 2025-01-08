package mc.gouv.xaf.back.service.templates.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.sms.SmsTemplatesService;
import mc.gouv.xaf.back.service.templates.GestionSmsTemplateService;
import mc.gouv.xaf.back.service.templates.SmsTemplatesCache;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;
import mc.gouv.xaf.shared.formbean.SmsTemplateFormBean;

/**
 * Implémentation du service pour la gestion des templates SMS
 *
 * @author qdeme
 */
@Component
public class GestionSmsTemplateServiceImpl implements GestionSmsTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionSmsTemplateServiceImpl.class);

    @Autowired
    private SmsTemplatesService smsTemplatesService;

    @Autowired
    private SmsTemplatesCache smsTemplatesCache;

    @Override
    public SmsTemplateFormBean retrieveTemplateForm(SmsTemplateFormBean formBean) {
        try {
        	SmsTemplateDTO templateDtoCorps = smsTemplatesService.getTemplateByCodeAndLangue(formBean.getCode(),
                    formBean.getLangue());
            formBean.setSender(templateDtoCorps.getSender());
            formBean.setCorps(templateDtoCorps.getContenu());
        } catch (Exception e) {
            LOGGER.error("Aucun corps trouvé pour le code {}", formBean.getCode());
        }

        return formBean;
    }

    @Override
    public void saveTemplateForm(SmsTemplateFormBean formBean) {

        try {
            SmsTemplateDTO templateCorps = smsTemplatesService.getTemplateByCodeAndLangue(formBean.getCode(),
                    formBean.getLangue());
            templateCorps.setContenu(formBean.getCorps());
            templateCorps.setSender(formBean.getSender());
            templateCorps.setDateModif(new Date());
            smsTemplatesService.saveOrUpdateTemplate(templateCorps);
        } catch (Exception e) {
            LOGGER.error("Aucun corps trouvé pour le code {}", formBean.getCode(), e);
        }

        // Refresh du cache après modification
        smsTemplatesCache.refresh();
    }
}
