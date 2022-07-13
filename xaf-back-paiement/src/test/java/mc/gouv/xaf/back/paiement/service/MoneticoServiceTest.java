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
        assertThat(result).isEqualTo("7ce6942a39abfc5e5c9e9d237bad5033e1bc35df");
    }
}
