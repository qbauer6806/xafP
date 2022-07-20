package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.HashMap;

public interface MontantService {
    double getMontant(Integer demandeId);

    double getMontant(DemandeDTO demandeDto);

    double getMontant(HashMap<String, Double> objetMontants);

    HashMap<String, Double> getPaiements(DemandeDTO demandeDto);
}
