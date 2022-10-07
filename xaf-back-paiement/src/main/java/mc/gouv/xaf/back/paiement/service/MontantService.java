package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public interface MontantService {

    BigDecimal getMontant(Integer demandeId);

    BigDecimal getMontant(DemandeDTO demandeDto);

    BigDecimal getMontant(Map<String, BigDecimal> objetMontants);

    HashMap<String, BigDecimal> getPaiements(DemandeDTO demandeDto);

    String getCodeFacturation(String montantKey);

}
