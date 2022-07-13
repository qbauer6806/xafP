package mc.gouv.xaf.back.paiement.client.monetico;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.net.Proxy;

public class MoneticoClientTest {
    MoneticoClient moneticoClient = new MoneticoClient(Proxy.NO_PROXY, new PaiementPropertiesResolverTestImpl());


    //Parameters [ MoyenPaiementBO MoyenPaiementBO{reference='XQXS2CeBYNrO', commande=CommandeBO{id=41, dateCreation=2022-07-12T22:39:06.432, montant=90.0}, dateLimite=2022-08-11T22:39:06.432, montantInitial=90.0, montantCapture=90.0, montantRestant=90.0, moyenPaiementType=null, moyenPaiementStatut=EN_ATTENTE_DE_VALIDATION, dateDerniereModification=2022-07-12T22:39:06.432}]

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
        moneticoClient.capture(moyenPaiementBO, operation);
    }
}
