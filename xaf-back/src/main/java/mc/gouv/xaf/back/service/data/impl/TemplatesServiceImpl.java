package mc.gouv.xaf.back.service.data.impl;

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
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.TemplatesService;
import mc.gouv.xaf.shared.dto.TemplateDTO;

/**
 * Service permettant la manipulation des templates.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class TemplatesServiceImpl implements TemplatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatesServiceImpl.class);
    
    @Autowired
    private TemplatesRepository templatesRepository;
    
    @Autowired
    private DemarchesService demarchesService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO getTemplate(String demarcheId, Integer templateId) {
        
        LOGGER.info("Récupération en base du template...");
        
        TemplateBO templateBo = templatesRepository.findByDemarcheIdAndPkTemplates(demarcheId, templateId);
        
        if (templateBo == null) {
            throw new DemarchesServiceException("Template introuvable",
                    HttpStatus.NOT_FOUND);
        }
        
        LOGGER.info("Transformation bo -> dto ...");
        
        return TemplatesTransformer.bo2Dto(templateBo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO getTemplateByDemarcheIdAndCodeAndLangue(String demarcheId, String code, String langue) {
        
        LOGGER.info("Récupération en base du template...");
        
        TemplateBO templateBo = templatesRepository.findByDemarcheIdAndCodeAndLangue(demarcheId, code, langue);
        
        if (templateBo == null) {
            throw new DemarchesServiceException("Template introuvable",
                    HttpStatus.NOT_FOUND);
        }
        
        LOGGER.info("Transformation bo -> dto ...");
        
        return TemplatesTransformer.bo2Dto(templateBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TemplateDTO> getTemplates(String demarcheId) {

        LOGGER.info("Récupération en base des templates...");
        
        List<TemplateBO> templateBos = templatesRepository.findByDemarcheId(demarcheId);
        
        LOGGER.info("Transformation bo -> dto ...");
        
        return TemplatesTransformer.bo2Dto(templateBos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO saveOrUpdateTemplate(String demarcheId, TemplateDTO template) {
        
        if (template.getPkTemplates() != null) {
            // PkTemplates fourni, il faut donc mettre à jour un template
            return updateTemplate(demarcheId, template);
        } else {
            // Pas de PkTemplates fourni, il faut donc créer un nouveau template
            return saveTemplate(demarcheId, template);
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO saveTemplate(String demarcheId, TemplateDTO template) {
        
        // Vérification préalable de l'existence de la démarche indiquée
        demarchesService.getCheckDemarche(demarcheId);

        LOGGER.info("Transformation dto -> bo");
        
        TemplateBO bo = TemplatesTransformer.dto2Bo(template);
        
        bo = templatesRepository.save(bo);
        
        LOGGER.info("Transformation bo -> dto ...");
        
        return TemplatesTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO updateTemplate(String demarcheId, TemplateDTO template) {
        
        LOGGER.info("Récupération en base du template...");
        
        TemplateBO templateBo = templatesRepository.findByDemarcheIdAndPkTemplates(demarcheId, template.getPkTemplates());
        
        if (templateBo == null) {
            throw new DemarchesServiceException("Template introuvable",
                    HttpStatus.NOT_FOUND);
        }
        
        LOGGER.info("Mise à jour du template...");
        
        templateBo.setLangue(template.getLangue());
        templateBo.setCode(template.getCode());
        templateBo.setContenu(template.getContenu());
        
        templateBo = templatesRepository.save(templateBo);
        
        LOGGER.info("Transformation bo -> dto ...");
        
        TemplateDTO ret = TemplatesTransformer.bo2Dto(templateBo);
        ret.setUpdated(true);
        
        return ret;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTemplate(String demarcheId, Integer templateId) {

        LOGGER.info("Récupération en base du template...");
        
        TemplateBO templateBo = templatesRepository.findByDemarcheIdAndPkTemplates(demarcheId, templateId);
        
        if (templateBo == null) {
            throw new DemarchesServiceException("Template introuvable",
                    HttpStatus.NOT_FOUND);
        }
        
        LOGGER.info("Suppression du template...");
        
        templatesRepository.delete(templateBo);
    }

}
