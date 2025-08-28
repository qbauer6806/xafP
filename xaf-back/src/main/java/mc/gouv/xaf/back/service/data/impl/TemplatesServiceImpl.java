package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.TemplatesRepository;
import mc.gouv.xaf.back.data.entity.TemplateBO;
import mc.gouv.xaf.back.data.transformer.TemplatesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.TemplateDTO;

/**
 * Service permettant la manipulation des templates.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class TemplatesServiceImpl implements TemplatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatesServiceImpl.class);

    @Autowired
    private TemplatesRepository templatesRepository;

    private TemplateBO getTemplateBO(Integer templateId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE, templateId);
        TemplateBO templateBo = templatesRepository.findByPkTemplates(templateId);
        if (templateBo == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        return templateBo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO getTemplate(Integer templateId) {
        TemplateBO templateBo = getTemplateBO(templateId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TemplatesTransformer.bo2Dto(templateBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO getTemplateByCodeAndLangue(String code, String langue) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        TemplateBO templateBo = templatesRepository.findByCodeAndLangue(code, langue);
        if (templateBo == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TemplatesTransformer.bo2Dto(templateBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TemplateDTO> getTemplates() {
        LOGGER.info("Récupération en base des templates...");
        List<TemplateBO> templateBos = templatesRepository.findAll();
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TemplatesTransformer.bo2Dto(templateBos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TemplateDTO> getTemplates(String langue) {
        LOGGER.info("Récupération en base des templates...");
        List<TemplateBO> templateBos = templatesRepository.findByLangue(langue);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TemplatesTransformer.bo2Dto(templateBos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO saveOrUpdateTemplate(TemplateDTO template) {
        if (template.getPkTemplates() != null) {
            // PkTemplates fourni, il faut donc mettre à jour un template
            return updateTemplate(template);
        } else {
            // Pas de PkTemplates fourni, il faut donc créer un nouveau template
            return saveTemplate(template);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO saveTemplate(TemplateDTO template) {
        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        TemplateBO bo = TemplatesTransformer.dto2Bo(template);

        // La date de création correspond à la date de dernière modification
        if (bo.getDateModif() == null) {
            bo.setDateModif(new Date());
        }
        bo = templatesRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TemplatesTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO updateTemplate(TemplateDTO template) {
        TemplateBO templateBo = getTemplateBO(template.getPkTemplates());

        LOGGER.info("Mise à jour du template...");
        templateBo.setLangue(template.getLangue());
        templateBo.setCode(template.getCode());
        templateBo.setContenu(template.getContenu());

        // Modification implicite de la date
        if (template.getDateModif() == null) {
            templateBo.setDateModif(new Date());
        } else {
            templateBo.setDateModif(template.getDateModif());
        }

        templateBo = templatesRepository.save(templateBo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

        TemplateDTO ret = TemplatesTransformer.bo2Dto(templateBo);
        ret.setUpdated(true);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTemplate(Integer templateId) {
        TemplateBO templateBo = getTemplateBO(templateId);
        LOGGER.info("Suppression du template...");
        templatesRepository.delete(templateBo);
    }

}
