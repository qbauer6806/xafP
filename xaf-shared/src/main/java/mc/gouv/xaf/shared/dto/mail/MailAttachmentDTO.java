package mc.gouv.xaf.shared.dto.mail;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe modélisant une pièce jointe.
 *
 * @author qdeme
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MailAttachmentDTO {

    private InputStream inputStream;

    private String filename;

    private String contentType;

    private long size;

}
