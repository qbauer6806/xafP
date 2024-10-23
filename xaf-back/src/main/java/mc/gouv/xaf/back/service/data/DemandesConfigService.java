package mc.gouv.xaf.back.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;

public interface DemandesConfigService {

    List<String> getModelPathsRechercheAvancee();

    List<String> getModelPathsRechercheAvancee(String buildId);

    List<DemandeConfigBO> getConfigsBO();

    DemandeConfigBO getLastConfig();

    JsonNode saveConfig(JsonNode config);

    String getLastBuildId();

    List<String> getModelPaths(JsonNode modelPaths);
}
