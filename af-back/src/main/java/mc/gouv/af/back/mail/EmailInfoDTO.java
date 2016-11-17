package mc.gouv.af.back.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Modélise un email à être envoyé par le MailService d'AfBack
 * 
 * @author qdeme
 *
 */
public class EmailInfoDTO {

    /**
     * Template correspondant au corps du mail
     * Ce template doit être en HTML
     */
    private String bodyTemplateCode;
    
    /**
     * Template correspondant au sujet du mail
     */
    private String subjectTemplateCode;
    
    /**
     * Sert à identifier dans quelle langue on doit prendre les templates
     */
    private String langue;
    
    private List<EmailInfoAddressDTO> to = new ArrayList<EmailInfoAddressDTO>();

    private List<EmailInfoAddressDTO> cc = new ArrayList<EmailInfoAddressDTO>();

    private List<EmailInfoAddressDTO> bcc = new ArrayList<EmailInfoAddressDTO>();

    private EmailInfoAddressDTO from;

    private EmailInfoAddressDTO replyto;
    
    /**
     * Liste de métadonnées optionnelles à donner au service de mail
     */
    private List<EmailInfoParamDTO> params = new ArrayList<EmailInfoParamDTO>();
    
    public EmailInfoDTO() {
        
    }

    public String getBodyTemplateCode() {
        return bodyTemplateCode;
    }

    public void setBodyTemplateCode(String bodyTemplateCode) {
        this.bodyTemplateCode = bodyTemplateCode;
    }

    public String getSubjectTemplateCode() {
        return subjectTemplateCode;
    }

    public void setSubjectTemplateCode(String subjectTemplateCode) {
        this.subjectTemplateCode = subjectTemplateCode;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public List<EmailInfoAddressDTO> getTo() {
        return to;
    }

    public List<EmailInfoAddressDTO> getCc() {
        return cc;
    }

    public List<EmailInfoAddressDTO> getBcc() {
        return bcc;
    }

    public EmailInfoAddressDTO getFrom() {
        return from;
    }

    public void setFrom(String address, String name) {
        from = new EmailInfoAddressDTO(address, name);
    }

    public EmailInfoAddressDTO getReplyto() {
        return replyto;
    }

    public void setReplyto(String address, String name) {
        this.replyto = new EmailInfoAddressDTO(address, name);
    }

    public List<EmailInfoParamDTO> getParams() {
        return params;
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

    @Override
    public String toString() {
        return "EmailInfoDTO [bodyTemplateCode=" + bodyTemplateCode + ", subjectTemplateCode=" + subjectTemplateCode
                + ", langue=" + langue + ", to=" + to + ", cc=" + cc + ", bcc=" + bcc + ", from=" + from + ", replyto="
                + replyto + ", params=" + params + "]";
    }
    
}
