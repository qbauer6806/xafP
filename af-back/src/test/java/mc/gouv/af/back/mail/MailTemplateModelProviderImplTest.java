package mc.gouv.af.back.mail;

import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import mc.gouv.af.back.mail.MailTemplateModelProvider;
import mc.gouv.dem.shared.model.DemandeDTO;

@ActiveProfiles("test")
@Component
public class MailTemplateModelProviderImplTest implements MailTemplateModelProvider{


	 
	@Override
	public Entry<String, String> getMailTemplateCodesForAction(String action) {
		
		String bodyTemplateCode = MailTemplateMock.accepteContentFRCode;
        String subjectTemplateCode = MailTemplateMock.acceptSubjectFRCode;
        
        return new SimpleEntry<String, String>(bodyTemplateCode, subjectTemplateCode);
       
    }

	@Override
	public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
			Map<String, Object> bpmVariables, String codeMotif, String commentaire) {

		Map<String, Object> model = new HashMap<String, Object>();
        
        model.put("identifiant", MailTestMockObjects.IDENTIFIER);
        model.put("motif", MailTestMockObjects.MOTIF);
        model.put("commentaire", MailTestMockObjects.COMMENT);
        model.put("titre", MailTestMockObjects.TITLE);
        model.put("prenom", MailTestMockObjects.FIRST_NAME);
        model.put("nom", MailTestMockObjects.LAST_NAME);
        model.put("urlBack", MailTestMockObjects.BACK_URL);
        model.put("urlFront",  MailTestMockObjects.FRONT_URL);
        model.put("usager", MailTestMockObjects.USAGER);
        model.put("pkDemande", MailTestMockObjects.PK );
        model.put("utilisateur", MailTestMockObjects.UTILISATEUR);
        
        return model;
	}

}
