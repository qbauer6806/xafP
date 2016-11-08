package mc.gouv.af.servlet.properties;

public enum AfServletGouvProperty implements GouvProperty {

    DEM_ACCESSES_URL("mc.gouv.appfactory.demarchesws.accesses.url"),
    DEM_DEMANDES_URL("mc.gouv.appfactory.demarchesws.demandes.url"),
    DEM_MOTIFS_URL("mc.gouv.appfactory.demarchesws.motifs.url"),
    DEM_URL("mc.gouv.appfactory.demarchesws.url"),
    LOGIN_REST_URL("mc.gouv.appfactory.external.login.url"),
    PAYS_URL("mc.gouv.appfactory.external.pays.url"),
    FILE_URL("mc.gouv.appfactory.filews.file.url"),
    MAIL_URL("mc.gouv.appfactory.mailws.mail.url"),

    /**
     * SORTIR CES PROPERTIES PROPRE à HAB
     */
    FILE_USER("mc.gouv.hab.frontserver.file.user"),
    FILE_PWD("mc.gouv.hab.frontserver.file.pwd"),
    MAIL_USER("mc.gouv.hab.frontserver.mail.user"),
    MAIL_PWD("mc.gouv.hab.frontserver.mail.pwd"),
    DEMARCHES_USER("mc.gouv.hab.frontserver.dem.user"),
    DEMARCHES_PWD("mc.gouv.hab.frontserver.dem.pwd"),
    GOUV_CONTACT_EMAIL("mc.gouv.hab.frontserver.mail.contact"),
    /**
     * 
     */

    CAPTCHA_PRIVATE_KEY("mc.gouv.appfactory.captcha.privatekey");

    ;

    private String code;

    AfServletGouvProperty(String code) {
        this.code = code;

    }

    public String getCode() {
        return code;
    }

}
