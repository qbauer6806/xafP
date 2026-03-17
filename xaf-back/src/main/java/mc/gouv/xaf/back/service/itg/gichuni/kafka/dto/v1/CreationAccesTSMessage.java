package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Sens : TS -> GU (topic ts-to-gichuni)
 *
 * Lorsqu'un usager se connecte pour la première fois sur un TS, l'usager est invité à accepter les CGU. Le TS envoie
 * alors un message au GU afin qu'il mette à jour sa liste de correspondance entre les usagers et les TS sur lesquels
 * ils sont inscrits.
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationAccesTSMessage extends GUKafkaMessage {

    private String demarcheId;

    private String usagerId;

    public CreationAccesTSMessage() {
        super("creation-acces-ts");
    }

    public CreationAccesTSMessage(String demarcheId, String usagerId) {
        this();
        this.demarcheId = demarcheId;
        this.usagerId = usagerId;
    }

}
