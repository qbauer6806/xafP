package mc.gouv.xaf.back.paiement.client.monetico;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.monetico.MoneticoApiClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.Proxy;

public class MoneticoApiClientTest {
    MoneticoApiClient moneticoApiClient = new MoneticoApiClient(Proxy.NO_PROXY,
            new PaiementPropertiesResolverTestImpl(),
            new OperationHelper(new PaiementPropertiesResolverTestImpl()),
            null,
            null,
            null,
            null
    );


    @Test
    @Ignore
    public void testCapture() throws Exception {
        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setMontantInitial(90.0);
        moyenPaiementBO.setMontantRestant(90.0);
        moyenPaiementBO.setMontantCapture(0.0);
        moyenPaiementBO.setPkMoyenPaiement("XQXS2CeBYNrO");
        OperationBO operation = new OperationBO();
        operation.setMontant(90.0);
        moneticoApiClient.capture(moyenPaiementBO, operation, null);
    }
}
