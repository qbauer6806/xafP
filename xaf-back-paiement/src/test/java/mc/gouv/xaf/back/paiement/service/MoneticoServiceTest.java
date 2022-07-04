package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.shared.stc.dto.ContexteCommandeDTO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
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
    private MoneticoService moneticoService;


    @Test
    public void contexteCommandeDTOtoBase64_Test() {
        String result = moneticoService.contexteCommandeDTOtoBase64(new ContexteCommandeDTO());
        assertThat(result).isEqualTo("eyJiaWxsaW5nIjp7ImZpcnN0TmFtZSI6IkFkYSIsImxhc3ROYW1lIjoiTG92ZWxhY2UiLCJhZGRyZXNzTGluZTEiOiIxMDEgUnVlIGRlIFJvaXNlbCIsIm1vYmlsZVBob25lIjoiKzMzLTYxMjM0NTY3OCIsImNpdHkiOiJZIiwicG9zdGFsQ29kZSI6IjgwMTkwIiwiY291bnRyeSI6IkZSIn19");
    }

    @Test
    public void getHmacString_Test() {
        String result = moneticoService.getHmacString(new PaiementDTO());
        assertThat(result).isEqualTo("e9d6de4413e1c05602cbe19b44aadeaae98961f7");
    }
}
