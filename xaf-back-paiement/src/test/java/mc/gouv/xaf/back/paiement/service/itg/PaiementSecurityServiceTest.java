package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaiementSecurityServiceTest {

    @Autowired
    private PaiementSecurityService paiementSecurityService;

    @Test
    public void contexteCommandeDTOtoBase64_Test() {
        String result = paiementSecurityService.contexteCommandeDTOtoBase64(new ContexteCommandeDTO());
        assertThat(result).isEqualTo("e30=");
    }

    @Test
    public void getHmacString_Test() {
        String result = paiementSecurityService.getHmacStringInterfaceAller(new PaiementDTO());
        assertThat(result).isEqualTo("e3e93094f85b8ab18ac66db5398bd4b1a7a529b5");
    }

    @Test
    public void getHmacStringInterfaceRetourTest() {
//        MoneticoResponseDTO responseDTOTest = new MoneticoResponseDTO();
//        responseDTOTest.setTpe("7527409");
//        responseDTOTest.setAuthentification("ewogICAiZGV0YWlscyIgOiB7CiAgICAgICJBUmVzIiA6ICJDIiwKICAgICAgIkNSZXMiIDogIlkiLAogICAgICAibGlhYmlsaXR5U2hpZnQiIDogIlkiLAogICAgICAibWVyY2hhbnRQcmVmZXJlbmNlIiA6ICJjaGFsbGVuZ2VfbWFuZGF0ZWQiLAogICAgICAidHJhbnNhY3Rpb25JRCIgOiAiMzQ1MTM2YzgtMmY5OC00ZjIxLWIxMDctOTlmNTZlMzM4YTU0IgogICB9LAogICAicHJvdG9jb2wiIDogIjNEU2VjdXJlIiwKICAgInN0YXR1cyIgOiAiYXV0aGVudGljYXRlZCIsCiAgICJ2ZXJzaW9uIiA6ICIyLjEuMCIKfQo=");
//        responseDTOTest.setBincb("00000100");
//        responseDTOTest.setBrand("VI");
//        responseDTOTest.setCbmasquee("00000100******02");
//        responseDTOTest.setCodeRetour("payetest");
//        responseDTOTest.setCvx("oui");
//        responseDTOTest.setDate("21/07/2022_a_15:56:26");
//        responseDTOTest.setEcard("non");
//        responseDTOTest.setHpancb("10181E9A22519D864FA98E154C3E2FFBC6F69C30");
//        responseDTOTest.setIpclient("82.113.11.254");
//        responseDTOTest.setModepaiement("CB");
//        responseDTOTest.setMontant("80.0EUR");
//        responseDTOTest.setOriginecb("FRA");
//        responseDTOTest.setOriginetr("MCO");
//        responseDTOTest.setReference("YnZEx97Puhvq");
//        responseDTOTest.setTypecompte("inconnu");
//        responseDTOTest.setUsage("inconnu");
//        responseDTOTest.setVld("1222");
//        responseDTOTest.setTexteLibre("Demander l echange d un permis de conduire etranger - Commande du 03/10/2022:16:23:48 - demandes [PEC-20221003-I0J9]");
//        String macTest = paiementSecurityService.getHmacStringInterfaceRetour(responseDTOTest);
//        System.out.println(macTest);

        MoneticoResponseDTO responseDTO = new MoneticoResponseDTO();
        responseDTO.setTpe("7527409");
        responseDTO.setAuthentification("ewogICAicHJvdG9jb2wiIDogIjNEU2VjdXJlIiwKICAgInN0YXR1cyIgOiAibm90X2Vucm9sbGVkIiwKICAgInZlcnNpb24iIDogIjEuMC4yIgp9Cg==");
        responseDTO.setBincb("00000100");
        responseDTO.setBrand("VI");
        responseDTO.setCbmasquee("00000100******02");
        responseDTO.setCodeRetour("payetest");
        responseDTO.setCvx("oui");
        responseDTO.setDate("26/09/2022_a_12:42:19");
        responseDTO.setEcard("non");
        responseDTO.setHpancb("086E11B15634C24CC4900E6A9F666A32DDCABE3F");
        responseDTO.setIpclient("82.113.11.254");
        responseDTO.setModepaiement("CB");
        responseDTO.setMontant("80EUR");
        responseDTO.setMotifrefus("");
        responseDTO.setOriginecb("FRA");
        responseDTO.setOriginetr("MCO");
        responseDTO.setReference("OsU7xquP8DTU");
        responseDTO.setTexteLibre("Demander l’échange d’un permis de conduire étranger - 26/09/2022:12:41:55 - demandes [PEC-20220926-TPBP]");
        responseDTO.setTypecompte("inconnu");
        responseDTO.setUsage("inconnu");
        responseDTO.setVld("1222");
        String mac = paiementSecurityService.getHmacStringInterfaceRetour(responseDTO);
        assertThat(mac).isEqualToIgnoringCase("0C2E747CF0DB6F4D6B42321804E5A27716F88B11");
    }

}
