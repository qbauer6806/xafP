package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MoneticoServiceTest {

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
        assertThat(result).isEqualTo("0191c42b24bff809057e0b165f380cb2a5228fa1");
    }
}
