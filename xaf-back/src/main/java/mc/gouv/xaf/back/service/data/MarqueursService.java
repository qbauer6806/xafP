package mc.gouv.xaf.back.service.data;

import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.shared.dto.BuildDemandeFromMarqueursDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MappingOptionDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;

public interface MarqueursService {

    List<MarqueurDTO> getMarqueurs(String buildId);

    MarqueurDTO getMarqueur(String buildId, String marqueurId);

    MarqueurDTO saveOrUpdateMarqueur(MarqueurDTO marqueur, JsonNode configContenu);

    void deleteMarqueur(Integer pkMarqueur);

    void copyOrGenerateMarqueurs(String lastBuildId, String buildId, List<String> modelPaths, JsonNode sections);

    String exportConfig() throws IOException;

    void importConfig(byte[] file) throws IOException;

    JsonNode buildDemande(BuildDemandeFromMarqueursDTO buildDemandeFromMarqueursDTO);

    String getMarqueurChoixTradFrOuEn(DemandeDTO demandeDTO, String marqueurIdentifiant);

    List<MappingOptionDTO> recupererMappingOptions(DemandeDTO demandeDTO, String marqueurId);

    Optional<MappingOptionDTO> recupererOptionDepuisValeur(DemandeDTO demandeDTO, String marqueurId, String valeur);
}
