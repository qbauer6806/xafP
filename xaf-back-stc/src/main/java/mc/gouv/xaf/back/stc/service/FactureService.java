package mc.gouv.xaf.back.stc.service;

import java.io.IOException;

public interface FactureService {

     void saveFacture(String reference, Integer demandeId) throws IOException;
}
