package mc.gouv.xaf.back.paiement.client.monetico;

import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;
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
        MoyenPaiementDTO moyenPaiementDTO = new MoyenPaiementDTO();
        moyenPaiementDTO.setMontantInitial(90.0);
        moyenPaiementDTO.setMontantRestant(90.0);
        moyenPaiementDTO.setMontantCapture(0.0);
        moyenPaiementDTO.setPkMoyenPaiement("XQXS2CeBYNrO");
        OperationDTO operation = new OperationDTO();
        operation.setMontant(90.0);
        moneticoApiClient.capture(moyenPaiementDTO, operation, null);
    }
}
