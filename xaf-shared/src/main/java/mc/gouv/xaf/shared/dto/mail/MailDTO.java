package mc.gouv.xaf.shared.dto.mail;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Classe pour modélisation JSON d'un mail à l'entrée du WS.
 * 
 * @author qdeme
 *
 */
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailDTO {

    @Setter
    @Getter
    private Integer id;

    @Setter
    @Getter
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;

    @Setter
    @Getter
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateEmission;

    @Setter
    @Getter
    private String statut;

    @Setter
    @Getter
    @NotNull
    @Size(min = 1, max = 1000)
    @Valid
    private AddressBlockDTO[] to;

    @Setter
    @Getter
    @Size(max = 1000)
    private AddressBlockDTO[] cc;

    @Setter
    @Getter
    @Size(max = 1000)
    private AddressBlockDTO[] bcc;

    @Setter
    @Getter
    @NotNull
    @Valid
    private AddressBlockDTO from;

    @Setter
    @Getter
    @Valid
    private AddressBlockDTO replyto;

    @Setter
    @Getter
    @NotNull
    @Size(min = 1, max = 998)
    private String subject;

    @Setter
    @Getter
    private String text;

    @Setter
    @Getter
    private String html;

    @Valid
    private ParamDTO[] paramDTOs;
    
    @Setter
    @Getter
    private MailAttachmentLinkDTO[] mailAttachmentLinks;

    public ParamDTO[] getParams() {
        return paramDTOs;
    }

    public void setParams(ParamDTO[] params) {
        this.paramDTOs = params;
    }

}
