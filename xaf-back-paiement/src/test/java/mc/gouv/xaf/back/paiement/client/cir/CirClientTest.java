package mc.gouv.xaf.back.paiement.client.cir;


import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.mock.UsagersCacheTestImpl;
import org.junit.Test;

import java.io.InputStream;
import java.net.Proxy;

public class CirClientTest {

    CirClient cirClient = new CirClient(new UsagersCacheTestImpl(), Proxy.NO_PROXY, new PaiementPropertiesResolverTestImpl());


    @Test
    public void createFactureTest() {
        String numPermis = "292093";
        String numImmat = " ";
        double montant = 0.0;
        String codeTransaction = "1591658";
        String resultat = cirClient.createFacture(numPermis, numImmat, montant, codeTransaction, 1);
        System.out.println(resultat);
    }

    @Test
    public void checkTest() {
        String numFacture = "1124807";
        String resultat = cirClient.check(numFacture);
        System.out.println(resultat);

    }

    @Test
    public void getFactureTest() {
        String numFacture = "1124807";
        InputStream inputStream = cirClient.getFacture(numFacture);
    }
}
