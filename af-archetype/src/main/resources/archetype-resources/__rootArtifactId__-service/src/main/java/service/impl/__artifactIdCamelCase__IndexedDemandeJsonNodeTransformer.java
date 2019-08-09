#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.impl;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.service.transformer.IndexedDemandeJsonNodeTransformer;

@Service
@Conditional(IndexationEnabledCondition.class)
public class ${artifactIdCamelCase}IndexedDemandeJsonNodeTransformer implements IndexedDemandeJsonNodeTransformer {

    private static final String DONNEE_NODE = "donnee";
    private static final String VEHICULE_NODE = "vehicule";

    private static final String PRENOM_PROPRIETAIRE_NODE = "prenomproprietaire";
    private static final String NOM_PROPRIETAIRE_NODE = "nomproprietaire";
    private static final String NOM_AFFICHAGE_PROPRIETAIRE_NODE = "nomaffichageproprietaire";

    private static final String USAGER_NODE = "usager";
    private static final String NOM_USAGER_NODE = "nom";
    private static final String PRENOM_USAGER_NODE = "prenom";
    private static final String NOM_AFFICHAGE_USAGER_NODE = "nomaffichage";

    @Override
    public JsonNode transform(JsonNode jsonNode) {
        JsonNode indexedJsonNode = jsonNode.deepCopy();

        ObjectNode vehiculeNode = (ObjectNode) indexedJsonNode.path(DONNEE_NODE).path(VEHICULE_NODE);

        if (!isMissingNode(vehiculeNode)) {
            JsonNode prenomProprietaireNode = vehiculeNode.get(PRENOM_PROPRIETAIRE_NODE);
            JsonNode nomProprietaireNode = vehiculeNode.get(NOM_PROPRIETAIRE_NODE);
            String prenomProprietaire = (isMissingNode(prenomProprietaireNode)) ? ""
                    : prenomProprietaireNode.textValue();
            String nomProprietaire = (isMissingNode(nomProprietaireNode)) ? "" : nomProprietaireNode.textValue();
            vehiculeNode.put(NOM_AFFICHAGE_PROPRIETAIRE_NODE, prenomProprietaire + " " + nomProprietaire);
        }
        ObjectNode usagerNode = (ObjectNode) indexedJsonNode.path(USAGER_NODE);

        if (!isMissingNode(usagerNode)) {
            JsonNode prenomUsagerNode = usagerNode.get(PRENOM_USAGER_NODE);
            JsonNode nomUsagerNode = usagerNode.get(NOM_USAGER_NODE);
            String prenomUsager = isMissingNode(prenomUsagerNode) ? "" : prenomUsagerNode.textValue();
            String nomUsager = isMissingNode(nomUsagerNode) ? "" : nomUsagerNode.textValue();
            usagerNode.put(NOM_AFFICHAGE_USAGER_NODE, prenomUsager + " " + nomUsager);
        }
        return indexedJsonNode;
    }

    private boolean isMissingNode(JsonNode node) {
        return node == null || node instanceof MissingNode || node instanceof NullNode;
    }

}
