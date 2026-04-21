package mc.gouv.xaf.back.service.itg.sms.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class AfSmsTemplateModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfSmsTemplateModelProvider.class);

    private final Optional<SmsTemplateModelProvider> smsTemplateModelProvider;
    private final AfTemplateModelProvider afTemplateModelProvider;

    public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
            Map<String, Object> bpmVariables, String codeMotif, String commentaire) {
        LOGGER.info("Construction du modèle pour le template (demandeId= {} ...", demande.getPkDemandes());

        Map<String, Object> model = afTemplateModelProvider.getGenericModelDemandeMailSms(demande, codeMotif,
                commentaire, bpmVariables);
        smsTemplateModelProvider.ifPresent(provider -> provider.setModel(model, bodyTemplateCode, bpmVariables));

        return model;
    }

    public Entry<String, String> getMailTemplateCodesForAction(String action) {
        return smsTemplateModelProvider.map(provider -> provider.getSmsTemplateCodesForAction(action)).orElse(null);
    }

}
