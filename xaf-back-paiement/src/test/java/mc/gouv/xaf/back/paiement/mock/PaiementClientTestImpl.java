package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.client.PaiementClient;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class PaiementClientTestImpl implements PaiementClient {
    @Override
    public String capture(MoyenPaiementBO paiement, double montant) {
        return "reference=lATKsKoVw1Rr\n" +
                "cdr=0\n" +
                "lib=commande non authentifiee";
    }
}
