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
public class MailTemplateModelProviderImpl implements MailTemplateModelProvider{

	 private final String IDENTIFIER = "TestUserID";
	 private final String TITLE = "Mr";
	 private final String FIRST_NAME = "Bob";
	 private final String LAST_NAME = "TestMan";
	 private final String MOTIF = "TestMotif";
	 private final String COMMENT = "Celui c'est un commentaire";
	 private final String AGENT_NAME = "Smith";
	 private final String UESER = "TestUsager";
	 private final String PK = "TEST_123456";
	 private final String BODY_TEMPLATE_CODE = "testCode";
	 private final String SUBJECT_TEPLATE_CODE = "testTemplate";

	@Override
	public Entry<String, String> getMailTemplateCodesForAction(String action) {
		
		String bodyTemplateCode = BODY_TEMPLATE_CODE;
        String subjectTemplateCode = SUBJECT_TEPLATE_CODE;
        
        return new SimpleEntry<String, String>(bodyTemplateCode, subjectTemplateCode);
        
    }

	@Override
	public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
			Map<String, Object> bpmVariables, String codeMotif, String commentaire) {

		Map<String, Object> model = new HashMap<String, Object>();
        
        model.put("identifiant", IDENTIFIER);
        model.put("motif", MOTIF);
        model.put("commentaire", COMMENT);
        model.put("titre", TITLE);
        model.put("prenom", FIRST_NAME);
        model.put("nom", LAST_NAME);
        model.put("urlBack", "");
        model.put("urlFront",  "");
        model.put("usager", UESER);
        model.put("pkDemande", PK );
        model.put("utilisateur", AGENT_NAME);
        
        return model;
	}

}
