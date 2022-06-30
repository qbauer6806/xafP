package mc.gouv.xaf.back.stc.mock;

import mc.gouv.xaf.back.stc.client.PaiementClient;
import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementClientTestImpl implements PaiementClient {
    @Override
    public String capture(MoyenPaiementBO paiement, double montant) {
        return "capture001";
    }
}
