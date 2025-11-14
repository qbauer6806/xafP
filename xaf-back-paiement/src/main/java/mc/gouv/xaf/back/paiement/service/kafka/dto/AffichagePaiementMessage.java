package mc.gouv.xaf.back.paiement.service.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class AffichagePaiementMessage extends GUKafkaMessage {

    private String onlineServiceId;
    private String procedureCode;
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
    private String expiryDate;
    private String paymentMethodAccount;
    private String effectiveBrand;
    private String link;

    public AffichagePaiementMessage() {
        super("affichage-paiement");
    }

    public AffichagePaiementMessage(String onlineServiceId, String procedureCode, String userLegacyId, PaymentTypeEnum paymentType,
            String paymentMethodToken, LocalDateTime paymentDate, double paymentAmount, String paymentStatus, String requestObject,
            String requestNumber, LocalDateTime requestDate, String paymentMethodExpiryDate, String paymentMethodAccount, String effectiveBrand, String link) {
        this();
        this.onlineServiceId = onlineServiceId;
        this.procedureCode = procedureCode;
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
        this.expiryDate = toShortYear(paymentMethodExpiryDate);
        this.paymentMethodAccount = paymentMethodAccount;
        this.link = link;
    }

    private String toShortYear(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/\\d{4}$")) {
            throw new IllegalArgumentException("Format attendu : MM/YYYY");
        }
        // Exemple : "11/2025"
        String[] parts = expiryDate.split("/");
        String month = parts[0];
        String year = parts[1].substring(2); // garde les 2 derniers chiffres
        return month + "/" + year; // "11/25"
    }
}
