package mc.gouv.xaf.shared.dto.mail;

import lombok.Getter;
import lombok.Setter;

/**
 * Classe servant à représenter une pièce jointe d'un email sans contenir ses données, mais
 * un lien MAIL à appeler pour la récupérer.
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class MailAttachmentLinkDTO {
    
    private String filename;
    
    private long size;
    
    private String contentType;
    
    private String url;

}
