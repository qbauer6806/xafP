package mc.gouv.xaf.back.paiement.service.kafka;

import mc.gouv.xaf.back.paiement.service.kafka.dto.AffichagePaiementMessage;
import mc.gouv.xaf.back.paiement.service.kafka.dto.PaymentTypeEnum;

import java.time.LocalDateTime;

public interface GUKafkaPaiementProducer {

    void sendAffichagePaiementMessage(AffichagePaiementMessage apm);

}
