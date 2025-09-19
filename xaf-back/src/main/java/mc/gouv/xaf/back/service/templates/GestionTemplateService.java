package mc.gouv.xaf.back.service.templates;

import mc.gouv.xaf.shared.dto.ExportTemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateCreateFormBean;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import java.io.IOException;
import java.util.List;

public interface GestionTemplateService {

    TemplateFormBean retrieveTemplateForm(TemplateFormBean formBean);

    void saveTemplateForm(TemplateFormBean formBean);

    void saveTemplateForm(TemplateCreateFormBean formBean);

    String exportConfig() throws IOException;

    List<ExportTemplateDTO> importConfig(byte[] file) throws IOException;

    void deleteTemplate(String templateCode);

}
