package mc.gouv.xaf.back.mail;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitaire pour la classe MailService
 * 
 * @author dsaidiparto.ext
 *
 */
@Disabled
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class MailServiceTest {

    private final String FR_CHECK_TIITLE_FAMILY_NAME_PATTERN = "Bonjour (\\w*) (\\w*) (\\w*),";
    private final String FR_CHECK_MAILSUBJECT_IDENTIFIER_PATTERN = "Accusé de réception de votre candidature (\\w*)' ";
    private final String FR_CHECK_MAILBODY_IDENTIFIER_PATTERN = "Nous avons bien reçu votre candidature (\\w*).";
    private final String FR_CHECK_PK_DEMAND_PATTERN = "Concernant le demande (\\w*)";
    private final String FR_CHECK_MOTIF_PATTERN = "Pour le motif (\\w*).";
    private final String FR_CHECK_UTILISATEUR_PATTERN = " en contactant (\\w*)";
    private final String FR_CHECK_USAGER_PATTERN = " concerant l'usager : (\\w*)";
    private final String FR_CHECK_URL_BACK_PATTERN = "sur le site <a href=\"(http://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\">www.gouv.mc</a>";
    private final String FR_CHECK_URL_FRONT_PATTERN = "le site du Gouvernement <a href=\"(http://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\">www.gouv.mc</a>";
    private final String FR_CHECK_COMMENT_PATTERN = " Commentaire : (.*).<br/>";

    private final String EN_CHECK_TIITLE_FAMILY_NAME_PATTERN = "hello dear (\\w*) (\\w*) (\\w*),";
    private final String EN_CHECK_MAILSUBJECT_IDENTIFIER_PATTERN = "receipt your application (\\w*)' ";
    private final String EN_CHECK_MAILBODY_IDENTIFIER_PATTERN = "your application for (\\w*).";
    private final String EN_CHECK_PK_DEMAND_PATTERN = " the vacancy (\\w*)";
    private final String EN_CHECK_MOTIF_PATTERN = " Based on (\\w*).";
    private final String EN_CHECK_UTILISATEUR_PATTERN = " contacting : (\\w*)";
    private final String EN_CHECK_USAGER_PATTERN = " mentioning : (\\w*)";
    private final String EN_CHECK_URL_BACK_PATTERN = " the website <a href=\"(http://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\">www.gouv.mc</a>";
    private final String EN_CHECK_URL_FRONT_PATTERN = "government's website <a href=\"(http://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\">www.gouv.mc</a>";
    private final String EN_CHECK_COMMENT_PATTERN = " PS : (.*).<br/>";

    @Autowired
    private MailService mailService;

    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;

    /**
     * En fournissant le template il retourne un array de String conentant le contenu de email et le sujet d'email.
     * 
     * @param lang
     * @return Stting[] email preview text
     * @throws Exception
     */
    private String[] getEmailPreview(String lang) throws Exception {

        Entry<String, String> templateCodes = mailTemplateModelProvider.getMailTemplateCodesForAction("action", null);
        String bodyTemplateCode = templateCodes.getValue();
        String subjectTemplateCode = templateCodes.getKey();

        Map<String, Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode,
                new DemandeDTO(), null, MailTestMockObjects.MOTIF, MailTestMockObjects.COMMENT);

        return mailService.getMailPreview(bodyTemplateCode, subjectTemplateCode, lang, model);
    }

    /**
     * Pour un email en fraçais verifie que l'identifiant de usager est bien placé dans le sujet
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectIdentifierInSubject() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_MAILSUBJECT_IDENTIFIER_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[1]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.IDENTIFIER, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que l'identifiant de usager est bien placé dans le sujet
     * 
     * @throws Exception
     */

    @Test
    void shouldHaveCorrectIdentifierInSubjectEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_MAILSUBJECT_IDENTIFIER_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[1]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.IDENTIFIER, matcher.group(1));
    }

    /**
     * Pour un email en fraçais verifie que l'identifiant de usager est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectIdentifierInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_MAILBODY_IDENTIFIER_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.IDENTIFIER, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que l'identifiant de usager est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectIdentifierInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_MAILBODY_IDENTIFIER_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);
        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.IDENTIFIER, matcher.group(1));
    }

    /**
     * Pour un email en fraçais verifie que le Titre,Nom et prénom de usager sont bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectTitleLastFirstNameInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_TIITLE_FAMILY_NAME_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.TITLE, matcher.group(1));
        assertEquals(MailTestMockObjects.LAST_NAME, matcher.group(2));
        assertEquals(MailTestMockObjects.FIRST_NAME, matcher.group(3));
    }

    /**
     * Pour un email en anglais verifie que le Titre,Nom et prénom de usager sont bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectTitleLastFirstNameInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_TIITLE_FAMILY_NAME_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.TITLE, matcher.group(1));
        assertEquals(MailTestMockObjects.LAST_NAME, matcher.group(2));
        assertEquals(MailTestMockObjects.FIRST_NAME, matcher.group(3));
    }

    /**
     * Pour un email en frnaçais verifie que le code de demarche de usager est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectPkDemandInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_PK_DEMAND_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.PK, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que le code de demarche de usager est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectPkDemandInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_PK_DEMAND_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.PK, matcher.group(1));
    }

    /**
     * Pour un email en frnaçais verifie que le motif de email envoyé est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectMotifInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_MOTIF_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.MOTIF, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que le motif de email envoyé est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shouldHaveCorrectMotifInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_MOTIF_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.MOTIF, matcher.group(1));
    }

    /**
     * Pour un email en français verifie que le nom d'utilisateur est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectUtilisateurInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_UTILISATEUR_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.UTILISATEUR, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que le nom d'utilisateur est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectUtilisateurInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_UTILISATEUR_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.UTILISATEUR, matcher.group(1));
    }

    /**
     * Pour un email en français verifie que le nom d'usager est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectUsagerInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_USAGER_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);
        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.USAGER, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que le nom d'usager est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectUsagerInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_USAGER_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.USAGER, matcher.group(1));
    }

    /**
     * Pour un email en français verifie que le lien FRONT_URL est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectFrontUlrInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_URL_FRONT_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.FRONT_URL, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que le lien FRONT_URL est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectFrontUlrInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_URL_FRONT_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.FRONT_URL, matcher.group(1));
    }

    /**
     * Pour un email en français verifie que le lien BACK_URL est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectBackUlrInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_URL_BACK_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.BACK_URL, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que le lien BACK_URL est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectBackUlrInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_URL_BACK_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.BACK_URL, matcher.group(1));
    }

    /**
     * Pour un email en français verifie que Commentaire est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectCommentInBody() throws Exception {
        Pattern pattern = Pattern.compile(FR_CHECK_COMMENT_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("fr")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.COMMENT, matcher.group(1));
    }

    /**
     * Pour un email en anglais verifie que Commentaire est bien placé dans le contenu
     * 
     * @throws Exception
     */
    @Test
    void shoudlHaveCorrectCommentInBodyEN() throws Exception {
        Pattern pattern = Pattern.compile(EN_CHECK_COMMENT_PATTERN);
        Matcher matcher = pattern.matcher(getEmailPreview("en")[0]);

        assertTrue(matcher.find());
        assertEquals(MailTestMockObjects.COMMENT, matcher.group(1));
    }
}
