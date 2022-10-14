package mc.gouv.xaf.back.paiement.client.cir;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeArticleDTO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.cir.CirApiApiClient;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

public class CirApiClientTest {

    CirApiApiClient cirApiClient = new CirApiApiClient(Proxy.NO_PROXY, new PaiementPropertiesResolverTestImpl(),
            new OperationHelper(new PaiementPropertiesResolverTestImpl()), null, null, null, null);


    @Test
    @Ignore
    public void createFactureTest() {
        String numPermis = "292093";
        String numImmat = " ";
        String codeTransaction = "1591658";
        CommandeDemandeArticleDTO articleDTO = new CommandeDemandeArticleDTO();
        articleDTO.setCodeTarif("a");
        articleDTO.setMontant(90.0);
        List<CommandeDemandeArticleDTO> articles = new ArrayList<>();
        articles.add(articleDTO);
        InformationFacturationDTO infoFacturation = new InformationFacturationDTO();
        infoFacturation.setNomTitulaire("Nom");
        infoFacturation.setPrenomTitulaire("Prenom");
        infoFacturation.setEmailUsager("mail");
        String resultat = cirApiClient.createFacture(numPermis, numImmat, 90.0, codeTransaction, infoFacturation, articles, null, null).get();
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
