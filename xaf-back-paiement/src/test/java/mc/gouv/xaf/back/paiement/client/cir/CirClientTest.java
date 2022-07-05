package mc.gouv.xaf.back.paiement.client.cir;


import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.mock.UsagersCacheTestImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.net.Proxy;

public class CirClientTest {

    CirClient cirClient = new CirClient(new UsagersCacheTestImpl(), Proxy.NO_PROXY, new PaiementPropertiesResolverTestImpl());


    @Test
    @Ignore
    public void createFactureTest() {
        String numPermis = "292093";
        String numImmat = " ";
        double montant = 0.0;
        String codeTransaction = "1591658";
        String resultat = cirClient.createFacture(numPermis, numImmat, montant, codeTransaction, 1);
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
        InputStream inputStream = cirClient.getFacture(numFacture);
        System.out.println("a");
    }
}
