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
     * Permet de récupérer le template correspondant à un TemplateID
     *
     * @return Le template demandé
     */
    TemplateDTO getTemplate(Integer templateId);

    /**
     * Permet de récupérer le template correspondant à un code template, et une langue
     */
    TemplateDTO getTemplateByCodeAndLangue(String code, String langue);

    /**
     * Permet de récupérer les templates correspondant
     *
     * @return Les templates demandés
     */
    List<TemplateDTO> getTemplates();

    /**
     * Permet de récupérer les templates filtrés par langue correspondant
     *
     * @return Les templates demandés
     */
    List<TemplateDTO> getTemplates(String langue);

    /**
     * Permet de sauvegarder ou mettre à jour un template en base
     *
     * @return Le template sauvegardé ou mis à jour
     */
    TemplateDTO saveOrUpdateTemplate(TemplateDTO template);

    /**
     * Permet de supprimer un template à partir du TemplateID
     */
    void deleteTemplate(Integer templateId);

    /**
     * Permet de sauvegarder en base un template
     *
     * @return Le template sauvegardé
     */
    TemplateDTO saveTemplate(TemplateDTO template);

    /**
     * Permet de modifier un template à partir du TemplateID
     *
     * @return Le template modifié
     */
    TemplateDTO updateTemplate(TemplateDTO template);
}
