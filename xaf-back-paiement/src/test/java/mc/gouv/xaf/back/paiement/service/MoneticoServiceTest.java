package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.client.SecurityService;
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
    private SecurityService securityService;


    @Test
    public void contexteCommandeDTOtoBase64_Test() {
        String result = securityService.contexteCommandeDTOtoBase64(new ContexteCommandeDTO());
        assertThat(result).isEqualTo("e30=");
    }

    @Test
    public void getHmacString_Test() {
        String result = securityService.getHmacString(new PaiementDTO());
        assertThat(result).isEqualTo("aa8f6174ef669c98a9a7d4dfcb5e90f86240deba");
    }
}
