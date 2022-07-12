package mc.gouv.xaf.back.paiement.client.monetico;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.net.Proxy;

public class MoneticoClientTest {
    MoneticoClient moneticoClient = new MoneticoClient(Proxy.NO_PROXY, new PaiementPropertiesResolverTestImpl());

    @Test
    @Ignore
    public void testCapture() throws Exception {
        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setMontantInitial(90.0);
        moyenPaiementBO.setMontantRestant(90.0);
        moyenPaiementBO.setMontantCapture(0.0);
        moyenPaiementBO.setPkMoyenPaiement("VViSk4JMstvd");
        OperationBO operation = new OperationBO();
        operation.setMontant(90.0);
        moneticoClient.capture(moyenPaiementBO, operation);
    }
}
