package mc.gouv.xaf.back.service.templates.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestionTemplateServiceImplTest {

    @InjectMocks
    private GestionTemplateServiceImpl gestionTemplateServiceImpl;

    @Mock
    private TemplatesService templatesService;

    private final String demarcheId = "TSNAME";
    private final String codeTemplateCorps = "CODE_TEMPLATE_CORPS";
    private final String codeTemplateObjet = "CODE_TEMPLATE_OBJET";
    private final String langue = "fr";

    private TemplateDTO templateCorps;
    private TemplateDTO templateObjet;

    @BeforeEach
    void setUp() {
        templateCorps = new TemplateDTO();
        templateCorps.setPkTemplates(1);
        templateCorps.setCode("CODE_TEMPLATE_CORPS");
        templateCorps.setLangue(langue);
        templateCorps.setDateModif(getDate("2021-12-09"));
        templateCorps.setContenu("ContenuCorps");

        templateObjet = new TemplateDTO();
        templateObjet.setPkTemplates(1);
        templateObjet.setCode("CODE_TEMPLATE_OBJET");
        templateObjet.setLangue(langue);
        templateObjet.setDateModif(getDate("2021-12-09"));
        templateObjet.setContenu("ContenuObjet");

        Mockito.when(templatesService.getTemplateByCodeAndLangue(codeTemplateCorps, langue)).thenReturn(templateCorps);
        Mockito.when(templatesService.getTemplateByCodeAndLangue(codeTemplateObjet, langue)).thenReturn(templateObjet);
    }

    @Test
    void retrieveTemplateForm() {
        TemplateFormBean formBean = new TemplateFormBean();
        formBean.setCode("CODE_TEMPLATE");
        formBean.setLangue("fr");
        gestionTemplateServiceImpl.retrieveTemplateForm(formBean);

        assertEquals("CODE_TEMPLATE", formBean.getCode());
        assertEquals("ContenuCorps", formBean.getCorps());
        assertEquals("ContenuObjet", formBean.getObjet());
        assertEquals("fr", formBean.getLangue());
    }

    @Test
    void saveTemplateForm() {
        TemplateFormBean formBean = new TemplateFormBean();
        formBean.setCode("CODE_TEMPLATE");
        formBean.setCorps("Corps Modifié");
        formBean.setObjet("Objet modifié");
        formBean.setLangue("fr");

        gestionTemplateServiceImpl.saveTemplateForm(formBean);

        verify(templatesService, times(1)).getTemplateByCodeAndLangue(codeTemplateCorps, langue);
        verify(templatesService, times(1)).getTemplateByCodeAndLangue(codeTemplateObjet, langue);

        templateCorps.setContenu(formBean.getCorps());
        templateObjet.setContenu(formBean.getObjet());
        verify(templatesService, times(1)).saveOrUpdateTemplate(templateCorps);
        verify(templatesService, times(1)).saveOrUpdateTemplate(templateObjet);
    }

    private Date getDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.of("Europe/Paris")).toInstant());
    }
}
