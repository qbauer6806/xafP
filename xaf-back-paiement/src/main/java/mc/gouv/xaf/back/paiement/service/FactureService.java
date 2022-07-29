package mc.gouv.xaf.back.paiement.service;

import java.io.IOException;

public interface FactureService {

     void saveFacture(String reference, Integer demandeId) throws Exception;
}
