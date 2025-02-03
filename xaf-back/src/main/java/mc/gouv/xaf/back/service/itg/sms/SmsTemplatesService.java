package mc.gouv.xaf.back.service.itg.sms;

import java.util.List;

import mc.gouv.xaf.shared.dto.SmsTemplateDTO;

/**
 * Service permettant la manipulation des templates de SMS
 *
 * @author qdeme
 */
public interface SmsTemplatesService {

    /**
     * Permet de récupérer le template correspondant à un TemplateID
     *
     * @return Le template demandé
     */
    SmsTemplateDTO getTemplate(Integer templateId);

    /**
     * Permet de récupérer le template correspondant à un code template, et une langue
     */
    SmsTemplateDTO getTemplateByCodeAndLangue(String code, String langue);

    /**
     * Permet de récupérer les templates correspondant
     *
     * @return Les templates demandés
     */
    List<SmsTemplateDTO> getTemplates();

    /**
     * Permet de récupérer les templates filtrés par langue correspondant
     *
     * @return Les templates demandés
     */
    List<SmsTemplateDTO> getTemplates(String langue);

    /**
     * Permet de sauvegarder ou mettre à jour un template en base
     *
     * @return Le template sauvegardé ou mis à jour
     */
    SmsTemplateDTO saveOrUpdateTemplate(SmsTemplateDTO template);

    /**
     * Permet de supprimer un template à partir du TemplateID
     */
    void deleteTemplate(Integer templateId);

    /**
     * Permet de sauvegarder en base un template
     *
     * @return Le template sauvegardé
     */
    SmsTemplateDTO saveTemplate(SmsTemplateDTO template);

    /**
     * Permet de modifier un template à partir du TemplateID
     *
     * @return Le template modifié
     */
    SmsTemplateDTO updateTemplate(SmsTemplateDTO template);
}
