package mc.gouv.xaf.back.paiement.client.cir;


import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.mock.UsagersCacheTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.net.Proxy;
import java.util.HashMap;

public class CirClientTest {

    CirClient cirClient = new CirClient(new UsagersCacheTestImpl(), Proxy.NO_PROXY, new PaiementPropertiesResolverTestImpl(),
            new OperationHelper(new PaiementPropertiesResolverTestImpl()), null, null, null, null);


    @Test
    @Ignore
    public void createFactureTest() {
        String numPermis = "292093";
        String numImmat = " ";
        String codeTransaction = "1591658";
        HashMap<String, Double> objetMontants = new HashMap<>();
        objetMontants.put("a", 90.0);
        String resultat = cirClient.createFacture(numPermis, numImmat, 90.0, codeTransaction, 1, objetMontants, null, null).get();
        System.out.println(resultat);
    }

    @Test
    @Ignore
    public void checkTest() {
        String numFacture = "1125054";
        String resultat = cirClient.check(numFacture);
        System.out.println(resultat);

    }

    @Test
    @Ignore
    public void getFactureTest() {
        String numFacture = "1125054";
        InputStream inputStream = cirClient.getFacture(numFacture, null).get();
        System.out.println("getFactureTest end");
    }
}
