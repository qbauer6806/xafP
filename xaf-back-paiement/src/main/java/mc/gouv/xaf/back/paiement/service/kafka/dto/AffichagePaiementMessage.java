package mc.gouv.xaf.back.paiement.service.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.paiement.service.kafka.PaymentTypeEnum;
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
public class AffichagePaiementMessage extends GUKafkaMessage {

    private String onlineServiceId;
    private String userLegacyId;

    private PaymentTypeEnum paymentType;
    private String paymentMethodToken;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;
    private double paymentAmount;
    private String paymentStatus;

    private String requestObject;
    private String requestNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;
    private String paymentMethodExpiryDate;
    private String paymentMethodAccount;
    private String effectiveBrand;
    private String link;

    public AffichagePaiementMessage() {
        super("affichage-paiement");
    }

    public AffichagePaiementMessage(String onlineServiceId, String userLegacyId, PaymentTypeEnum paymentType,
            String paymentMethodToken, LocalDateTime paymentDate, double paymentAmount, String paymentStatus, String requestObject,
            String requestNumber, LocalDateTime requestDate, String paymentMethodExpiryDate, String paymentMethodAccount, String effectiveBrand, String link) {
        this();
        this.onlineServiceId = onlineServiceId;
        this.userLegacyId = userLegacyId;
        this.paymentType = paymentType;
        this.paymentMethodToken = paymentMethodToken;
        this.paymentDate = paymentDate;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.requestObject = requestObject;
        this.requestNumber = requestNumber;
        this.requestDate = requestDate;
        this.effectiveBrand = effectiveBrand;
        this.paymentMethodExpiryDate = paymentMethodExpiryDate;
        this.paymentMethodAccount = paymentMethodAccount;
        this.link = link;
    }
}
