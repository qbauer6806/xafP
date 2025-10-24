package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

@Component
public class MailTemplateModelProviderTestlmpl implements MailTemplateModelProvider {

    @Override
    public String getMailTemplateCodeForAction(String action, DemandeDTO demande) {
        return "";
    }
}
