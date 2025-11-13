package mc.gouv.xaf.back.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;

import java.util.List;

public interface DemandesConfigService {

    List<String> getModelPathsMarqueurs(String buildId);

    List<DemandeConfigBO> getConfigsBO();

    DemandeConfigBO getConfig(String buildId);

    JsonNode saveConfig(JsonNode config);

    List<String> getModelPaths(JsonNode modelPaths);
}
