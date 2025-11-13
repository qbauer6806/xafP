package mc.gouv.xaf.back.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;

public interface MarqueursService {

    List<MarqueurDTO> getMarqueurs(String buildId);

    MarqueurDTO getMarqueur(String buildId, String marqueurId);

    MarqueurDTO saveOrUpdateMarqueur(MarqueurDTO marqueur, JsonNode configContenu);

    void deleteMarqueur(Integer pkMarqueur);

    void copyOrGenerateMarqueurs(String lastBuildId, String buildId, List<String> modelPaths, JsonNode sections);

    String exportConfig() throws IOException;

    void importConfig(byte[] file) throws IOException;

    JsonNode buildDemande(Map<String, String> donnees, List<Map<String, String>> donneesTableaux);

    JsonNode buildDemande(Map<String, String> donnees);

    String getMarqueurChoixTradFrOuEn(DemandeDTO demandeDTO, String marqueurIdentifiant);
}
