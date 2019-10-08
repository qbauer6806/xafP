#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.service.transformer.IndexedDemandeDataJsonNodeTransformer;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@Conditional(IndexationEnabledCondition.class)
public class ${artifactIdCamelCase}IndexedDemandeDataJsonNodeTransformer implements IndexedDemandeDataJsonNodeTransformer {

    private static final String NUMERO_ORDRE_NODE = "numeroOrdre";

    @Override
    public JsonNode transform(JsonNode jsonNode) {

        JsonNode indexedJsonNode;
        if (jsonNode == null) {
            ObjectMapper mapper = new ObjectMapper();
            indexedJsonNode = mapper.createObjectNode();
        } else {
            indexedJsonNode = jsonNode.deepCopy();
        }

        if (isMissingNode(indexedJsonNode.path(NUMERO_ORDRE_NODE))) {
            ObjectNode dataNode = (ObjectNode) indexedJsonNode;
            dataNode.put(NUMERO_ORDRE_NODE, "");
        }

        return indexedJsonNode;
    }

    private boolean isMissingNode(JsonNode node) {
        return node instanceof MissingNode || node instanceof NullNode;
    }

}
