package mc.gouv.xaf.back.service.data;

import java.util.List;
import mc.gouv.xaf.shared.dto.TemplateDTO;

/**
 * Service permettant la manipulation des templates.
 *
 * @author qdeme
 */
public interface TemplatesService {

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
     * Permet de supprimer un template à partir du TemplateCode
     */
    void deleteTemplateByCode(String templateCode, String langue);

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
