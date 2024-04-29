package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MailTemplateModelProviderTestlmpl implements MailTemplateModelProvider {

    @Override
    public Map.Entry<String, String> getMailTemplateCodesForAction(String action, Integer pkDemande) {
        return null;
    }

    @Override
    public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande, Map<String, Object> bpmVariables, String codeMotif, String commentaire) {
        return null;
    }

    @Override
    public Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire, Map<String, Object> bpmVariables) {
        return null;
    }

    @Override
    public Map<String, Object> getGenericModelDemande(DemandeDTO demande) {
        return null;
    }

    @Override
    public Map<String, Object> getGenericModel() {
        return null;
    }
}
