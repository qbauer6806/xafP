package mc.gouv.xaf.back.service.templates.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.TemplatesRepository;
import mc.gouv.xaf.back.data.entity.TemplateBO;
import mc.gouv.xaf.back.data.transformer.TemplatesTransformer;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.back.service.templates.GestionTemplateService;
import mc.gouv.xaf.shared.dto.ExportTemplateDTO;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.formbean.TemplateCreateFormBean;
import mc.gouv.xaf.shared.formbean.TemplateFormBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implémentation du service pour la gestion des templates
 *
 * @author mpavone
 */
@Component
@RequiredArgsConstructor
public class GestionTemplateServiceImpl implements GestionTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionTemplateServiceImpl.class);

    private static final String OBJET = "_OBJET";
    private static final String CORPS = "_CORPS";
    private static final String LANG_FR = "fr";
    private static final String LANG_EN = "en";

    private final TemplatesService templatesService;
    private final TemplatesRepository templatesRepository;

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
    }


    @Override
    public String exportConfig() throws IOException {

        LOGGER.info("Début de l'export de la configuration des templates");

        List<TemplateDTO> templatesList = TemplatesTransformer.bo2Dto(templatesRepository.findAll());

        // Convertir en fichier d'export
        List<ExportTemplateDTO> exportTemplateList = new ArrayList<>();
        for( TemplateDTO template : templatesList ) {
            ExportTemplateDTO exportTemplateDTO = new ExportTemplateDTO();
            exportTemplateDTO.setCode(template.getCode());
            exportTemplateDTO.setLangue(template.getLangue());
            exportTemplateDTO.setContenu(template.getContenu());
            exportTemplateDTO.setDateModif(template.getDateModif());
            exportTemplateList.add(exportTemplateDTO);
        }

        ObjectMapper mapper = new ObjectMapper();
        String exportedConfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportTemplateList);
        LOGGER.debug("Fin de l'export de la configuration des templates, fichier exporté {}", exportedConfig);
        return exportedConfig;
    }

    @Override
    public List<ExportTemplateDTO> importConfig(byte[] file) throws IOException {

        LOGGER.info("Début de l'import de la configuration");

        ObjectMapper mapper = new ObjectMapper();
        List<ExportTemplateDTO> config;
        try {
            config = mapper.readValue(file, new TypeReference<>() {});
        } catch (JsonParseException | JsonMappingException e) {
            throw new BadRequestException("Le fichier ne respecte pas la structure des fichiers à importer");
        }

        if (config != null) {
            templatesRepository.deleteAll();
            Iterable<TemplateBO> saved = templatesRepository.saveAll(TemplatesTransformer.exportDto2Bo(config));
            List<TemplateBO> configBo = StreamSupport.stream(saved.spliterator(), false)
                    .collect(Collectors.toList());

            LOGGER.info("Fin de l'import de la configuration");

            return TemplatesTransformer.bo2ExportDto(configBo);
        }

        LOGGER.info("La configuration n'a pas pu être importée");
        return null;
    }

    @Transactional
    @Override
    public void deleteTemplate(String templateCode, String langue) {
        templatesService.deleteTemplateByCode(templateCode + OBJET, langue);
        templatesService.deleteTemplateByCode(templateCode + CORPS, langue);
    }
}
