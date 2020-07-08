package mc.gouv.xaf.back.mail;

import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;

public class MailTestMockObjects {

    public static final EmailInfoDTO emailInfoDTOMock() {
        EmailInfoDTO dto = new EmailInfoDTO();
        dto.setBodyTemplateCode("MAIL_CREATION_DEMANDE_USAGER_OBJET");
        dto.setFrom("adresse0@gouv.mc", "MR TestMan");
        dto.setLangue("FR");
        dto.addBcc("bcc1@gouv.mc", "Test de BCC");
        dto.addCc("cc1@gouv.mc", "Carbon copy tester");
        dto.addTo("adresse1@gouv.mc", "Mr no one");

        return dto;
    }

    public static final String IDENTIFIER = "TestUserID";
    public static final String TITLE = "Mr";
    public static final String FIRST_NAME = "Bob";
    public static final String LAST_NAME = "TestMan";
    public static final String MOTIF = "TestMotif";
    public static final String COMMENT = "Celui est un commentaire";
    public static final String UTILISATEUR = "Smith";
    public static final String USAGER = "TestUsager";
    public static final String PK = "TEST_123456";
    public static final String FRONT_URL = "http://localhost:20760";
    public static final String BACK_URL = "http://localhost:30760";
}
