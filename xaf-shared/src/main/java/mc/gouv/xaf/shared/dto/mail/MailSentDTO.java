package mc.gouv.xaf.shared.dto.mail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Classe pour modélisation JSON d'une réponse du WS de mail
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailSentDTO {

    /**
     * ID du mail qui vient d'être créé
     */
    private Integer id;

    /**
     * Statut du mail qui vient d'être créé (ENVOYE, ECHEC)
     */
    private String statut;

}
