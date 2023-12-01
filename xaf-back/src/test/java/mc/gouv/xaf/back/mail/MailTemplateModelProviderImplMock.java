package mc.gouv.xaf.back.mail;

import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@ActiveProfiles("test")
@Component
public class MailTemplateModelProviderImplMock implements MailTemplateModelProvider {

    @Override
    public Entry<String, String> getMailTemplateCodesForAction(String action, Integer pkDemande) {

        String bodyTemplateCode = MailTemplateMock.accepteContentFRCode;
        String subjectTemplateCode = MailTemplateMock.acceptSubjectFRCode;

        return new SimpleEntry<>(bodyTemplateCode, subjectTemplateCode);

    }

    @Override
    public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
            Map<String, Object> bpmVariables, String codeMotif, String commentaire) {

        Map<String, Object> model = new HashMap<>();

        model.put("identifiant", MailTestMockObjects.IDENTIFIER);
        model.put("motif", MailTestMockObjects.MOTIF);
        model.put("commentaire", MailTestMockObjects.COMMENT);
        model.put("titre", MailTestMockObjects.TITLE);
        model.put("prenom", MailTestMockObjects.FIRST_NAME);
        model.put("nom", MailTestMockObjects.LAST_NAME);
        model.put("urlBack", MailTestMockObjects.BACK_URL);
        model.put("urlFront", MailTestMockObjects.FRONT_URL);
        model.put("usager", MailTestMockObjects.USAGER);
        model.put("pkDemande", MailTestMockObjects.PK);
        model.put("utilisateur", MailTestMockObjects.UTILISATEUR);

        return model;
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
