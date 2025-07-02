package mc.gouv.xaf.back.service.itg.sms.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Modélise un SMS destiné à être envoyé par le SmsService
 *
 * @author qdeme
 */
public class SmsInfoDTO {

    /**
     * Template correspondant au corps du SMS
     */
    private String bodyTemplateCode;

    /**
     * Sert à identifier dans quelle langue on doit prendre les templates
     */
    private String langue;

    private final List<String> to = new ArrayList<>();

    /**
     * Liste de métadonnées optionnelles à donner au service de SMS
     */
    private final List<SmsInfoParamDTO> params = new ArrayList<>();

    public String getBodyTemplateCode() {
        return bodyTemplateCode;
    }

    public void setBodyTemplateCode(String bodyTemplateCode) {
        this.bodyTemplateCode = bodyTemplateCode;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public List<String> getTo() {
        return to;
    }

    public List<SmsInfoParamDTO> getParams() {
        return params;
    }

    public void addTo(String numero) {
        to.add(numero);
    }
    
    public void addParam(String key, String value) {
        SmsInfoParamDTO param = new SmsInfoParamDTO(key, value);
        params.add(param);
    }

}
