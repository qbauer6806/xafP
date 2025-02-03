package mc.gouv.xaf.back.service.templates;

import mc.gouv.xaf.shared.formbean.SmsTemplateFormBean;

public interface GestionSmsTemplateService {

    SmsTemplateFormBean retrieveTemplateForm(SmsTemplateFormBean formBean);

    void saveTemplateForm(SmsTemplateFormBean formBean);

}
