package mc.gouv.xaf.back.service.itg.mail.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise un email à être envoyé par le MailService d'AfBack
 *
 * @author qdeme
 */
@Getter
@ToString
public class EmailInfoDTO {

    /**
     * Template correspondant au corps du mail Ce template doit être en HTML
     */
    @Setter
    private String bodyTemplateCode;

    /**
     * Template correspondant au sujet du mail
     */
    @Setter
    private String subjectTemplateCode;

    /**
     * Sert à identifier dans quelle langue on doit prendre les templates
     */
    @Setter
    private String langue;

    private final List<EmailInfoAddressDTO> to = new ArrayList<>();

    private final List<EmailInfoAddressDTO> cc = new ArrayList<>();

    private final List<EmailInfoAddressDTO> bcc = new ArrayList<>();

    private EmailInfoAddressDTO from;

    private EmailInfoAddressDTO replyto;

    /**
     * Liste de métadonnées optionnelles à donner au service de mail
     */
    private final List<EmailInfoParamDTO> params = new ArrayList<>();

    public void setFrom(String address, String name) {
        from = new EmailInfoAddressDTO(address, name);
    }

    public void setReplyto(String address, String name) {
        this.replyto = new EmailInfoAddressDTO(address, name);
    }

    public void addTo(String address, String name) {
        EmailInfoAddressDTO addr = new EmailInfoAddressDTO(address, name);
        to.add(addr);
    }

    public void addCc(String address, String name) {
        EmailInfoAddressDTO addr = new EmailInfoAddressDTO(address, name);
        cc.add(addr);
    }

    public void addBcc(String address, String name) {
        EmailInfoAddressDTO addr = new EmailInfoAddressDTO(address, name);
        bcc.add(addr);
    }

    public void addParam(String key, String value) {
        EmailInfoParamDTO param = new EmailInfoParamDTO(key, value);
        params.add(param);
    }

}
