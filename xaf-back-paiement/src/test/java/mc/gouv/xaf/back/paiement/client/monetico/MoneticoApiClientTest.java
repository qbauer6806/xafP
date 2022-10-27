package mc.gouv.xaf.back.paiement.client.monetico;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.monetico.MoneticoApiClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.Proxy;
import java.util.Collections;

public class MoneticoApiClientTest {
    MoneticoApiClient moneticoApiClient = new MoneticoApiClient(Proxy.NO_PROXY,
            new PaiementPropertiesResolverTestImpl(),
            new OperationHelper(new PaiementPropertiesResolverTestImpl()),
            null,
            null,
            null
    );


    @Test
    @Ignore
    public void testCapture() throws Exception {
        CommandeDTO commandeDTO = new CommandeDTO();
        commandeDTO.setMontantInitial(90.0);
        commandeDTO.setMontantRestant(90.0);
        commandeDTO.setMontantDejaCapture(0.0);
        MoyenPaiementDTO moyenPaiementDTO = new MoyenPaiementDTO();
        moyenPaiementDTO.setPkMoyenPaiements("XQXS2CeBYNrO");
        CommandeOperationDTO operation = new CommandeOperationDTO();
        operation.setMontant(90.0);
        commandeDTO.setMoyenPaiement(moyenPaiementDTO);
        commandeDTO.setOperations(Collections.singletonList(operation));
        moneticoApiClient.capture(commandeDTO, operation, null);
    }
}
