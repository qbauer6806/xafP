package mc.gouv.xaf.back.service.templates.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GestionTemplateServiceImplTest {

    @InjectMocks
    private GestionTemplateServiceImpl gestionTemplateServiceImpl;

    @Mock
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Mock
    private TemplatesService templatesService;

    // Mock utilisé pour le saveTemplateForm
    @Mock
    private TemplatesCache templatesCache;

    private final String demarcheId = "TSNAME";
    private final String codeTemplateCorps = "CODE_TEMPLATE_CORPS";
    private final String codeTemplateObjet = "CODE_TEMPLATE_OBJET";
    private final String langue = "fr";

    private TemplateDTO templateCorps;
    private TemplateDTO templateObjet;

    @Before
    public void setUp() {
        templateCorps = new TemplateDTO();
        templateCorps.setPkTemplates(1);
        templateCorps.setCode("CODE_TEMPLATE_CORPS");
        templateCorps.setDemarcheId(demarcheId);
        templateCorps.setLangue(langue);
        templateCorps.setDateModif(getDate("2021-12-09"));
        templateCorps.setContenu("ContenuCorps");

        templateObjet = new TemplateDTO();
        templateObjet.setPkTemplates(1);
        templateObjet.setCode("CODE_TEMPLATE_OBJET");
        templateObjet.setDemarcheId(demarcheId);
        templateObjet.setLangue(langue);
        templateObjet.setDateModif(getDate("2021-12-09"));
        templateObjet.setContenu("ContenuObjet");

        Mockito.when(gouvPropertiesResolver.getDemarcheId()).thenReturn(demarcheId);
        Mockito.when(templatesService.getTemplateByDemarcheIdAndCodeAndLangue(demarcheId, codeTemplateCorps,langue)).thenReturn(templateCorps);
        Mockito.when(templatesService.getTemplateByDemarcheIdAndCodeAndLangue(demarcheId, codeTemplateObjet,langue)).thenReturn(templateObjet);
    }

    @Test
    public void retrieveTemplateForm() {
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
    public void saveTemplateForm() {
        TemplateFormBean formBean = new TemplateFormBean();
        formBean.setCode("CODE_TEMPLATE");
        formBean.setCorps("Corps Modifié");
        formBean.setObjet("Objet modifié");
        formBean.setLangue("fr");

        gestionTemplateServiceImpl.saveTemplateForm(formBean);

        verify(templatesService, times(1)).getTemplateByDemarcheIdAndCodeAndLangue(demarcheId, codeTemplateCorps, langue);
        verify(templatesService, times(1)).getTemplateByDemarcheIdAndCodeAndLangue(demarcheId, codeTemplateObjet, langue);

        templateCorps.setContenu(formBean.getCorps());
        templateObjet.setContenu(formBean.getObjet());
        verify(templatesService, times(1)).saveOrUpdateTemplate(demarcheId, templateCorps);
        verify(templatesService, times(1)).saveOrUpdateTemplate(demarcheId, templateObjet);
    }

    private Date getDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.of("Europe/Paris")).toInstant());
    }
}