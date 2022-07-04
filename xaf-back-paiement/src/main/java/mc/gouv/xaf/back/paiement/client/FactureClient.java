package mc.gouv.xaf.back.paiement.client;

import java.io.InputStream;

public interface FactureClient {

    String check(String numFacture);

    String createFacture(String numPermis, String numImmat, double montant, String codeTransaction, Integer usagerId);

    InputStream getFacture(String numFacture);
}
