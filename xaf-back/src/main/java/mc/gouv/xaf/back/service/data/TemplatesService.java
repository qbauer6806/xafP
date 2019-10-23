package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.back.shared.dto.TemplateDTO;

/**
 * Service permettant la manipulation des templates.
 * 
 * @author qdeme
 *
 */
public interface TemplatesService {
    
    /**
     * Permet de récupérer le template correspondant à un DemarcheID et un TemplateID
     * @param template
     * @return Le template demandé
     */
    public TemplateDTO getTemplate(String demarcheId, Integer templateId);
    
    /**
     * Permet de récupérer le template correspondant à un DemarcheID, un code template, et une langue
     * @param template
     * @return
     */
    public TemplateDTO getTemplateByDemarcheIdAndCodeAndLangue(String demarcheId, String code, String langue);
    
    /**
     * Permet de récupérer les templates correspondant à un DemarcheID
     * @param template
     * @return Les templates demandés
     */
    public List<TemplateDTO> getTemplates(String demarcheId);

    /**
     * Permet de sauvegarder ou mettre à jour un template en base
     * @param template
     * @return Le template sauvegardé ou mis à jour
     */
    public TemplateDTO saveOrUpdateTemplate(String demarcheId, TemplateDTO template);
    
    /**
     * Permet de supprimer un template à partir du DemarcheID et du TemplateID
     * @param template
     */
    public void deleteTemplate(String demarcheId, Integer templateId);
    
    /**
     * Permet de sauvegarder en base un template
     * @param template
     * @return Le template sauvegardé
     */
    public TemplateDTO saveTemplate(String demarcheId, TemplateDTO template);
    
    /**
     * Permet de modifier un template à partir du DemarcheID et du TemplateID
     * @param template
     * @return Le template modifié
     */
    public TemplateDTO updateTemplate(String demarcheId, TemplateDTO template);
}
