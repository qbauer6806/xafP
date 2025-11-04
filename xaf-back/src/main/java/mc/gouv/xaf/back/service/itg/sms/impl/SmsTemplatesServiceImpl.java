package mc.gouv.xaf.back.service.itg.sms.impl;

import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.SmsTemplatesRepository;
import mc.gouv.xaf.back.data.entity.SmsTemplateBO;
import mc.gouv.xaf.back.data.transformer.SmsTemplatesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.itg.sms.SmsTemplatesService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des templates de SMS
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SmsTemplatesServiceImpl implements SmsTemplatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsTemplatesServiceImpl.class);

    private final SmsTemplatesRepository smsTemplatesRepository;

    private SmsTemplateBO getSmsTemplateBO(Integer templateId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE, templateId);
        SmsTemplateBO SmsTemplateBO = smsTemplatesRepository.findByPkSmsTemplates(templateId);
        if (SmsTemplateBO == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        return SmsTemplateBO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmsTemplateDTO getTemplate(Integer templateId) {
        SmsTemplateBO SmsTemplateBO = getSmsTemplateBO(templateId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return SmsTemplatesTransformer.bo2Dto(SmsTemplateBO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmsTemplateDTO getTemplateByCodeAndLangue(String code, String langue) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        SmsTemplateBO SmsTemplateBO = smsTemplatesRepository.findByCodeAndLangue(code, langue);
        if (SmsTemplateBO == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return SmsTemplatesTransformer.bo2Dto(SmsTemplateBO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SmsTemplateDTO> getTemplates() {
        LOGGER.info("Récupération en base des templates...");
        List<SmsTemplateBO> SmsTemplateBOs = smsTemplatesRepository.findAll();
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return SmsTemplatesTransformer.bo2Dto(SmsTemplateBOs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SmsTemplateDTO> getTemplates(String langue) {
        LOGGER.info("Récupération en base des templates...");
        List<SmsTemplateBO> SmsTemplateBOs = smsTemplatesRepository.findByLangue(langue);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return SmsTemplatesTransformer.bo2Dto(SmsTemplateBOs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmsTemplateDTO saveOrUpdateTemplate(SmsTemplateDTO template) {
        if (template.getPkSmsTemplates() != null) {
            // PkSmsTemplates fourni, il faut donc mettre à jour un template
            return updateTemplate(template);
        } else {
            // Pas de PkSmsTemplates fourni, il faut donc créer un nouveau template
            return saveTemplate(template);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmsTemplateDTO saveTemplate(SmsTemplateDTO template) {
        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        SmsTemplateBO bo = SmsTemplatesTransformer.dto2Bo(template);

        // La date de création correspond à la date de dernière modification
        if (bo.getDateModif() == null) {
            bo.setDateModif(new Date());
        }
        bo = smsTemplatesRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return SmsTemplatesTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SmsTemplateDTO updateTemplate(SmsTemplateDTO template) {
        SmsTemplateBO SmsTemplateBO = getSmsTemplateBO(template.getPkSmsTemplates());

        LOGGER.info("Mise à jour du template...");
        SmsTemplateBO.setLangue(template.getLangue());
        SmsTemplateBO.setCode(template.getCode());
        SmsTemplateBO.setContenu(template.getContenu());
        SmsTemplateBO.setSender(template.getSender());

        // Modification implicite de la date
        if (template.getDateModif() == null) {
            SmsTemplateBO.setDateModif(new Date());
        } else {
            SmsTemplateBO.setDateModif(template.getDateModif());
        }

        SmsTemplateBO = smsTemplatesRepository.save(SmsTemplateBO);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

        SmsTemplateDTO ret = SmsTemplatesTransformer.bo2Dto(SmsTemplateBO);
        ret.setUpdated(true);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTemplate(Integer templateId) {
        SmsTemplateBO SmsTemplateBO = getSmsTemplateBO(templateId);
        LOGGER.info("Suppression du template...");
        smsTemplatesRepository.delete(SmsTemplateBO);
    }

}
