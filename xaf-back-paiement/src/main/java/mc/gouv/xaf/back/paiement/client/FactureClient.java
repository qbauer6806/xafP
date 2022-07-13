package mc.gouv.xaf.back.paiement.client;

import java.io.InputStream;
import java.util.HashMap;

public interface FactureClient {

    String check(String numFacture);

    String createFacture(String numPermis, String numImmat,Double montant, String codeTransaction, Integer usagerId, HashMap<String, Double> objetMontants);

    InputStream getFacture(String numFacture);
}
