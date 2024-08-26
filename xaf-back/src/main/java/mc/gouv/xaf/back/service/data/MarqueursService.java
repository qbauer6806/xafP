package mc.gouv.xaf.back.service.data;

import java.util.List;
import mc.gouv.xaf.shared.dto.MarqueurDTO;

public interface MarqueursService {

    List<MarqueurDTO> getMarqueurs(String buildId);

    MarqueurDTO saveOrUpdateMarqueur(MarqueurDTO marqueur);

    void deleteMarqueur(Integer pkMarqueur);

    void copyOrGenerateMarqueurs(String lastBuildId, String buildId, List<String> modelPaths);
    void resetMarqueurs();
}
