package mc.gouv.xaf.back.paiement.service.kafka;

import mc.gouv.xaf.back.paiement.service.kafka.dto.PaymentTypeEnum;

import java.time.LocalDateTime;

public interface GUKafkaPaiementProducer {

    void sendAffichagePaiementMessage(String userLegacyId,
                                      PaymentTypeEnum paymentType, String paymentMethodToken, LocalDateTime paymentDate, double paymentAmount,
                                      String paymentStatus, String requestObject, String requestNumber, LocalDateTime requestDate, String paymentMethodExpiryDate, String paymentMethodAccount, String effectiveBrand, String link);

}
