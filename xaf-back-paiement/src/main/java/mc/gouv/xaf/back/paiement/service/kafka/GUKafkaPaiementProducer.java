package mc.gouv.xaf.back.paiement.service.kafka;

import java.time.LocalDateTime;

public interface GUKafkaPaiementProducer {

    void sendAffichagePaiementMessage(String userLegacyId,
            PaymentTypeEnum paymentType, String paymentMethodToken, LocalDateTime paymentDate, double paymentAmount,
            String paymentStatus, String requestObject, String requestNumber, LocalDateTime requestDate, String link);

    void sendSuppressionPaiementMessage(String userLegacyId, String requestNumber);
}
