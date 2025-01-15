package mc.gouv.xaf.back.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;

import java.util.List;

public interface DemandesConfigService {

    List<String> getModelPathsRechercheAvancee();

    List<String> getModelPathsMarqueurs(String buildId);

    List<DemandeConfigBO> getConfigsBO();

    DemandeConfigBO getLastConfig();

    DemandeConfigBO getConfig(String buildId);

    JsonNode saveConfig(JsonNode config);

    String getLastBuildId();

    List<String> getModelPaths(JsonNode modelPaths);
}
