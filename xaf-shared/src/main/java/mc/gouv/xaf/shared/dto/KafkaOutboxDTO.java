package mc.gouv.xaf.shared.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise un élément dans la boîte d'envoi pour le Guichet Unique via Kafka
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
public class KafkaOutboxDTO {

    private Integer pkKafkaOutbox;

    private String topic;

    private String key;

    private String contenu;

    private Date dateCreation;

    private Date dateLastAttempt;

    private Integer nbFailedAttempts;

    private String statut;

}
