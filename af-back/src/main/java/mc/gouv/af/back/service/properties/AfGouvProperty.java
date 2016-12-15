package mc.gouv.af.back.service.properties;

public enum AfGouvProperty implements GouvProperty {

    DEM_URL("mc.gouv.af.back.dem.url"),
    DEM_USER("mc.gouv.hab.backserver.dem.user"),
    DEM_PWD("mc.gouv.hab.backserver.dem.pwd"),
    DEMARCHE_ID("mc.gouv.hab.backserver.demarcheId"),
    PROCESS_DEFINITION_KEY("mc.gouv.hab.backserver.processDefinitionKey"),
    USAGERS_REST_URL("mc.gouv.demarches.external.usagers.url"),
    FILE_URL("mc.gouv.af.back.file.url"),
    FILE_USER("mc.gouv.hab.backserver.file.user"),
    FILE_PWD("mc.gouv.hab.backserver.file.pwd"),
    PAYS_REST_URL("mc.gouv.demarches.external.pays.url"),
    DEM_JMS_HOST("mc.gouv.af.back.dem.jms.host"),
    DEM_JMS_PORT("mc.gouv.af.back.dem.jms.port"),
    MAIL_URL("mc.gouv.af.back.mail.url"),
    MAIL_USER("mc.gouv.hab.backserver.mail.user"),
    MAIL_PWD("mc.gouv.hab.backserver.mail.pwd"),
    FRONT_URL("mc.gouv.af.back.front.url");

    private String code;

    AfGouvProperty(String code) {
        this.code = code;

    }

    public String getCode() {
        return code;
    }

}
