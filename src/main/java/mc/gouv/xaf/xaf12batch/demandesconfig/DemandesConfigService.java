package mc.gouv.xaf.xaf12batch.demandesconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.xaf12batch.dto.DemandeConfigBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesConfigService {

    @Autowired
    private DemandesConfigRepository demandesConfigRepository;

    public List<DemandeConfigBO> getConfigsBO() {
        return demandesConfigRepository.findAllByOrderByBuildIdDesc();
    }

    public List<String> getModelPaths(JsonNode modelPaths) {
        if (modelPaths == null || modelPaths.isNull()) {
            return new ArrayList<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {

        });
        try {
            return reader.readValue(modelPaths);
        } catch (IOException e) {
            System.out.println("error");
        }
        return new ArrayList<>();
    }

}
