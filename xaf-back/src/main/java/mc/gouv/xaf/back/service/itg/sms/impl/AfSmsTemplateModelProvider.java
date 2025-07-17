package mc.gouv.xaf.back.service.itg.sms.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * @author qdeme
 */
@Component
public class AfSmsTemplateModelProvider extends AfTemplateModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfSmsTemplateModelProvider.class);

    @Autowired(required = false)
    private SmsTemplateModelProvider smsTemplateModelProvider;

    public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
            Map<String, Object> bpmVariables, String codeMotif, String commentaire) {
        LOGGER.info("Construction du modèle pour le template (demandeId= {} ...", demande.getPkDemandes());

        Map<String, Object> model = getGenericModelDemandeMailSms(demande, codeMotif, commentaire, bpmVariables);
        smsTemplateModelProvider.setModel(model, bodyTemplateCode, bpmVariables);

        return model;
    }

    public Entry<String, String> getMailTemplateCodesForAction(String action) {
        return smsTemplateModelProvider.getSmsTemplateCodesForAction(action);
    }

}
