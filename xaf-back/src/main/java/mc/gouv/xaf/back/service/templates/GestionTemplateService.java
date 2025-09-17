package mc.gouv.xaf.back.service.templates;

import mc.gouv.xaf.shared.formbean.TemplateCreateFormBean;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;

public interface GestionTemplateService {

    TemplateFormBean retrieveTemplateForm(TemplateFormBean formBean);

    void saveTemplateForm(TemplateFormBean formBean);

    void saveTemplateForm(TemplateCreateFormBean formBean);

}
