package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.TemplateDTO;

import java.util.List;

/**
 * Service permettant la manipulation des templates.
 *
 * @author qdeme
 */
public interface TemplatesService {

    /**
     * Permet de récupérer le template correspondant à un DemarcheID et un TemplateID
     *
     * @return Le template demandé
     */
    TemplateDTO getTemplate(String demarcheId, Integer templateId);

    /**
     * Permet de récupérer le template correspondant à un DemarcheID, un code template, et une langue
     */
    TemplateDTO getTemplateByDemarcheIdAndCodeAndLangue(String demarcheId, String code, String langue);

    /**
     * Permet de récupérer les templates correspondant à un DemarcheID
     *
     * @return Les templates demandés
     */
    List<TemplateDTO> getTemplates(String demarcheId);

    /**
     * Permet de récupérer les templates filtrés par langue correspondant à un DemarcheID
     *
     * @return Les templates demandés
     */
    List<TemplateDTO> getTemplates(String demarcheId, String langue);

    /**
     * Permet de sauvegarder ou mettre à jour un template en base
     *
     * @return Le template sauvegardé ou mis à jour
     */
    TemplateDTO saveOrUpdateTemplate(String demarcheId, TemplateDTO template);

    /**
     * Permet de supprimer un template à partir du DemarcheID et du TemplateID
     */
    void deleteTemplate(String demarcheId, Integer templateId);

    /**
     * Permet de sauvegarder en base un template
     *
     * @return Le template sauvegardé
     */
    TemplateDTO saveTemplate(String demarcheId, TemplateDTO template);

    /**
     * Permet de modifier un template à partir du DemarcheID et du TemplateID
     *
     * @return Le template modifié
     */
    TemplateDTO updateTemplate(String demarcheId, TemplateDTO template);
}
