package mc.gouv.xaf.back.paiement.service.itg;


import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;

import java.util.Date;

public interface PaiementSecurityService {

    String dateFormat(Date date);

    String contexteCommandeDTOtoBase64(ContexteCommandeDTO contexte);

    String getHmacStringInterfaceAller(PaiementDTO paiementDTO);

    String getHmacStringInterfaceRetour(MoneticoResponseDTO moneticoResponseDTO);

}
