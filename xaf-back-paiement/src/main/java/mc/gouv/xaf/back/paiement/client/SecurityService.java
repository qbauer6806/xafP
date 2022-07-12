package mc.gouv.xaf.back.paiement.client;

import mc.gouv.xaf.shared.stc.dto.ContexteCommandeDTO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;

import java.util.Date;

public interface SecurityService {

    String dateFormat(Date date);

    String contexteCommandeDTOtoBase64(ContexteCommandeDTO contexte);

    String getHmacString(PaiementDTO paiementDTO);

}
