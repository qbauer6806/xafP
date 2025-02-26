package mc.gouv.xaf.back.paiement.mock;

import java.util.Map;
import java.util.Map.Entry;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

@Component
public class MailTemplateModelProviderTestlmpl implements MailTemplateModelProvider {

    @Override
    public void setModel(Map<String, Object> model, String bodyTemplateCode, Map<String, Object> bpmVariables, DemandeDTO demandeDTO) {

    }

    @Override
    public Entry<String, String> getMailTemplateCodesForAction(String action) {
        return null;
    }
}
