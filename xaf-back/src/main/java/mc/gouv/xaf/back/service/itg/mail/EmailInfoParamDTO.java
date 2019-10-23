package mc.gouv.xaf.back.service.itg.mail;

/**
 * 
 * Modélise une métadonnée d'un email à être envoyé par le MailService d'AfBack
 * 
 * @author qdeme
 *
 */
public class EmailInfoParamDTO {

    private String key;
    
    private String value;
    
    public EmailInfoParamDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EmailInfoParamDTO [key=" + key + ", value=" + value + "]";
    }
    
}
