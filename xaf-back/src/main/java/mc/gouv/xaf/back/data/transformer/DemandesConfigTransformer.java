package mc.gouv.xaf.back.data.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author uek
 */
@Service
public class DemandesConfigTransformer {

    @Value("${maven.version}")
    private String mavenVersion;

    private DemandesConfigTransformer() {
    }

    public JsonNode bo2Json(DemandeConfigBO bo) {
        if (bo == null) {
            return null;
        }
        return bo.getContenu();
    }

    public DemandeConfigBO json2Bo(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        DemandeConfigBO bo = new DemandeConfigBO();
        bo.setBuildId(jsonNode.get("buildId").asText());
        bo.setContenu(jsonNode);
        bo.setVersion(mavenVersion);
        return bo;
    }
}
