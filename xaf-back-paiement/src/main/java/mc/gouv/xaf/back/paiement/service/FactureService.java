package mc.gouv.xaf.back.paiement.service;

public interface FactureService {

    void saveFacture(String reference, Integer demandeId) throws Exception;
}
