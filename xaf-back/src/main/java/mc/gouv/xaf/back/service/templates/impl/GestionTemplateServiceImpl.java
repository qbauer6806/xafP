package mc.gouv.xaf.back.service.templates.impl;

import java.util.Date;
import jakarta.transaction.Transactional;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.templates.GestionTemplateService;
import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateCreateFormBean;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implémentation du service pour la gestion des templates
 *
 * @author mpavone
 */
@Component
public class GestionTemplateServiceImpl implements GestionTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionTemplateServiceImpl.class);

    private static final String OBJET = "_OBJET";
    private static final String CORPS = "_CORPS";
    private static final String LANG_FR = "fr";
    private static final String LANG_EN = "en";

    @Autowired
    private TemplatesService templatesService;

    @Autowired
    private TemplatesCache templatesCache;

    @Override
    public TemplateFormBean retrieveTemplateForm(TemplateFormBean formBean) {

        try {
            TemplateDTO templateDtoObjet = templatesService.getTemplateByCodeAndLangue(formBean.getCode() + OBJET,
                    formBean.getLangue());
            formBean.setObjet(templateDtoObjet.getContenu());
        } catch (Exception e) {
            LOGGER.error("Aucun objet trouvé pour le code {}", formBean.getCode());
        }

        try {
            TemplateDTO templateDtoCorps = templatesService.getTemplateByCodeAndLangue(formBean.getCode() + CORPS,
                    formBean.getLangue());
            formBean.setCorps(templateDtoCorps.getContenu());
        } catch (Exception e) {
            LOGGER.error("Aucun corps trouvé pour le code {}", formBean.getCode());
        }

        return formBean;
    }

    @Override
    public void saveTemplateForm(TemplateFormBean formBean) {
        try {
            TemplateDTO templateObjet = templatesService.getTemplateByCodeAndLangue(formBean.getCode() + OBJET,
                    formBean.getLangue());
            templateObjet.setContenu(formBean.getObjet());
            templateObjet.setDateModif(new Date());
            templatesService.saveOrUpdateTemplate(templateObjet);
        } catch (Exception e) {
            LOGGER.error("Aucun objet trouvé pour le code {}", formBean.getCode(), e);
        }

        try {
            TemplateDTO templateCorps = templatesService.getTemplateByCodeAndLangue(formBean.getCode() + CORPS,
                    formBean.getLangue());
            templateCorps.setContenu(formBean.getCorps());
            templateCorps.setDateModif(new Date());
            templatesService.saveOrUpdateTemplate(templateCorps);
        } catch (Exception e) {
            LOGGER.error("Aucun corps trouvé pour le code {}", formBean.getCode(), e);
        }

        // Refresh du cache après modification
        templatesCache.refresh();
    }

    @Transactional
    @Override
    public void saveTemplateForm(TemplateCreateFormBean formBean) {

        // Mail FR
        TemplateDTO templateObjet = new TemplateDTO();
        templateObjet.setCode(formBean.getCode() + OBJET);
        templateObjet.setContenu(formBean.getObjetFr());
        templateObjet.setLangue(LANG_FR);
        templateObjet.setDateModif(new Date());
        templatesService.saveOrUpdateTemplate(templateObjet);

        TemplateDTO templateCorps = new TemplateDTO();
        templateCorps.setCode(formBean.getCode() + CORPS);
        templateCorps.setContenu(formBean.getCorpsFr());
        templateCorps.setLangue(LANG_FR);
        templateCorps.setDateModif(new Date());
        templatesService.saveOrUpdateTemplate(templateCorps);

        // Mail EN
        if (StringUtils.isNotBlank(formBean.getObjetEn()) && StringUtils.isNotBlank(formBean.getCorpsEn())) {
            TemplateDTO templateObjetEn = new TemplateDTO();
            templateObjetEn.setCode(formBean.getCode() + OBJET);
            templateObjetEn.setContenu(formBean.getObjetEn());
            templateObjetEn.setLangue(LANG_EN);
            templateObjetEn.setDateModif(new Date());
            templatesService.saveOrUpdateTemplate(templateObjetEn);

            TemplateDTO templateCorpsEn = new TemplateDTO();
            templateCorpsEn.setCode(formBean.getCode() + CORPS);
            templateCorpsEn.setContenu(formBean.getCorpsEn());
            templateCorpsEn.setLangue(LANG_EN);
            templateCorpsEn.setDateModif(new Date());
            templatesService.saveOrUpdateTemplate(templateCorpsEn);
        }

        // Refresh du cache après modification
        templatesCache.refresh();
    }
}
