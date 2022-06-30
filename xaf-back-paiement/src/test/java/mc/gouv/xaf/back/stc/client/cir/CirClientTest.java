package mc.gouv.xaf.back.stc.client.cir;


import mc.gouv.xaf.back.stc.mock.GouvPropertiesResolverTestImpl;
import mc.gouv.xaf.back.stc.mock.UsagersCacheTestImpl;
import org.junit.Test;

import java.io.InputStream;
import java.net.Proxy;

public class CirClientTest {

    CirClient cirClient = new CirClient(new UsagersCacheTestImpl(), Proxy.NO_PROXY, new CirPropertiesResolver(new GouvPropertiesResolverTestImpl()));


    @Test
    public void createFactureTest() {
        String numPermis = "777777";
        String numImmat = "SZ77";
        double montant = 78;
        String codeTransaction = "78770";
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
