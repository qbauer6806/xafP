package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@ExtendWith(MockitoExtension.class)
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
        assertThat(result).isEqualTo("bb3cac15490e59ef100b4b34998dc765dd5bfb50");
    }

    @Test
    @Disabled("Resultat différent sur maven install dans intellij : CB0BD147C7FEAC57D1ABFEC970ECE7992341FE0E")
    public void getHmacStringInterfaceRetourTest() {
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
        assertThat(mac).isEqualTo("0C2E747CF0DB6F4D6B42321804E5A27716F88B11");
    }

    @Test
    public void getHmacStringInterfaceRetourTest2() {
        MoneticoResponseDTO responseDTO = new MoneticoResponseDTO();
        responseDTO.setTpe("7527409");
        responseDTO.setAuthentification("ewogICAicHJvdG9jb2wiIDogIjNEU2VjdXJlIiwKICAgInN0YXR1cyIgOiAibm90X2Vucm9sbGVkIiwKICAgInZlcnNpb24iIDogIjEuMC4yIgp9Cg==");
        responseDTO.setBincb("00000100");
        responseDTO.setBrand("VI");
        responseDTO.setCbmasquee("00000100******02");
        responseDTO.setCodeRetour("payetest");
        responseDTO.setCvx("oui");
        responseDTO.setDate("04/10/2022_a_10:35:18");
        responseDTO.setEcard("non");
        responseDTO.setHpancb("086E11B15634C24CC4900E6A9F666A32DDCABE3F");
        responseDTO.setIpclient("82.113.11.254");
        responseDTO.setModepaiement("CB");
        responseDTO.setMontant("80EUR");
        responseDTO.setMotifrefus("");
        responseDTO.setOriginecb("FRA");
        responseDTO.setOriginetr("MCO");
        responseDTO.setReference("KFTETuyvrf9G");
        responseDTO.setTexteLibre("Demander l echange d un permis de conduire etranger - Commande du 04/10/2022:10:34:52 - demandes [PEC-20221004-6VBJ]");
        responseDTO.setTypecompte("inconnu");
        responseDTO.setUsage("inconnu");
        responseDTO.setVld("0125");
        String mac = paiementSecurityService.getHmacStringInterfaceRetour(responseDTO);
        assertThat(mac).isEqualTo("FD9EA1C30518B9D4E9688C41C57D7A9CE840EAAF");
    }

    @Test
    public void getHmacStringInterfaceRetourTest3() {
        MoneticoResponseDTO responseDTO = new MoneticoResponseDTO();
        responseDTO.setTpe("7527409");
        responseDTO.setDate("24/01/2023_a_13:47:08");
        responseDTO.setMontant("0.11EUR");
        responseDTO.setReference("S4QBaERt4SVa");
        responseDTO.setAuthentification("ewogICAicHJvdG9jb2wiIDogIjNEU2VjdXJlIiwKICAgInN0YXR1cyIgOiAibm90X2Vucm9sbGVkIiwKICAgInZlcnNpb24iIDogIjEuMC4yIgp9Cg==");
        responseDTO.setBrand("VI");
        responseDTO.setCbmasquee("00000100******01");
        responseDTO.setCodeRetour("Annulation");
        responseDTO.setCvx("oui");
        responseDTO.setVld("1223");
        responseDTO.setTexteLibre("Demander l echange d un permis de conduire etranger - Commande du 24/01/2023:13:46:36 - demandes [PEC-20230124-6B5V]");
        responseDTO.setMotifrefus("Refus");
        responseDTO.setMotifrefusautorisation("Refus test");
        responseDTO.setTypecompte("inconnu");
        responseDTO.setUsage("inconnu");
        responseDTO.setEcard("non");
        responseDTO.setBincb("00000100");
        responseDTO.setHpancb("280452D8743BC9070D85BEF2E1D686945680F9F7");
        responseDTO.setIpclient("82.113.11.254");
        responseDTO.setModepaiement("CB");
        responseDTO.setOriginecb("FRA");
        responseDTO.setOriginetr("MCO");
        responseDTO.setNumauto("");
        String mac = paiementSecurityService.getHmacStringInterfaceRetour(responseDTO);
        assertThat(mac).isEqualTo("C0C5EF71F8E4F1C1725062EEDB8181E400D388EC");
    }

}
