package mc.gouv.xaf.back.paiement.client;


import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;

import java.util.Date;

public interface SecurityService {

    String dateFormat(Date date);

    String contexteCommandeDTOtoBase64(ContexteCommandeDTO contexte);

    String getHmacString(PaiementDTO paiementDTO);

}
