package mc.gouv.xaf.back.paiement.client.cir;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import org.junit.Ignore;
import org.junit.Test;

import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.impl.FactureApiClientImpl;

public class CirApiClientTest {

    FactureApiClientImpl cirApiClient = new FactureApiClientImpl(new PaiementPropertiesResolverTestImpl(),
            new OperationHelper(new PaiementPropertiesResolverTestImpl()), null);


    @Test
    @Ignore
    public void createFactureTest() {
        List<CirRequestDTO> lignes = new ArrayList<>();
        CirRequestDTO request = new CirRequestDTO();
        request.setNumTpe("TPEnum");
        request.setNumPermis("292093");
        request.setNumImmat("");
        request.setRegistre(0);
        request.setDateOperation("2023-04-09");
        request.setMontant(90.0);
        request.setNomPropr("Nom");
        request.setPrenomPropr("Prenom");
        request.setCodeTransaction("1111");
        request.setAutorisation("1111");
        request.setTransactionId("1111");
        request.setCodeReglement("Y");
        request.setEmail("mail");
        request.setCodeOperation("P2");
        request.setMontantOperation("20.0");
        lignes.add(request);
        String resultat = cirApiClient.createFacture(lignes, null).get();
        System.out.println(resultat);
    }

    @Test
    @Ignore
    public void checkTest() {
        String numFacture = "1125054";
        String resultat = cirApiClient.check(numFacture);
        System.out.println(resultat);

    }

    @Test
    @Ignore
    public void getFactureTest() throws Exception {
        String numFacture = "1125054";
        InputStream inputStream = cirApiClient.getFacture(numFacture, null).get();
        System.out.println("getFactureTest end");
    }
}
