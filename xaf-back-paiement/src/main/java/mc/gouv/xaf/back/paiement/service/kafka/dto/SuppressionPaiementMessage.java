package mc.gouv.xaf.back.paiement.service.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.GUKafkaMessage;
import java.time.LocalDateTime;

/**
 * Sens : TS -> GU (topic ts-to-gichuni-payment)
 *
 * Avec l'arrivée du paiement il est nécessaire d'implémenter un service permettant
 * la mise à jour des différentes informations de paiement coté mon guichet
 *
 * @author xdecool
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SuppressionPaiementMessage extends GUKafkaMessage {

    private String onlineServiceId;
    private String userLegacyId;
    private String requestNumber;

    public SuppressionPaiementMessage() {
        super("suppression-paiement");
    }

    public SuppressionPaiementMessage(String onlineServiceId, String userLegacyId, String requestNumber) {
        this();
        this.onlineServiceId = onlineServiceId;
        this.userLegacyId = userLegacyId;
        this.requestNumber = requestNumber;
    }
}
