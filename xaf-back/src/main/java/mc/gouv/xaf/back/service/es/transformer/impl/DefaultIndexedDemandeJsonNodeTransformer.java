package mc.gouv.xaf.back.service.es.transformer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.servicerest.pays.model.PaysBean;
import mc.gouv.xaf.back.service.es.transformer.IndexedDemandeJsonNodeTransformer;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DefaultIndexedDemandeJsonNodeTransformer implements IndexedDemandeJsonNodeTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIndexedDemandeJsonNodeTransformer.class);
    private static final String NC = "NC";

    @Autowired
    private PaysCache paysCache;

    protected void createOrUpdatePays(JsonNode node, String key) {
        if (!isMissingNode(node)) {
            if (canUpdate(node.path(key))) {
                String paysCode = node.path(key).textValue();
                PaysBean paysBean = paysCache.get(paysCode, "fr");
                if (paysBean != null) {
                    ((ObjectNode) node).put(key, paysBean.getNom());
                } else {
                    LOGGER.error("Impossible de convertir le code pays en libelle");
                }
            } else {
                ((ObjectNode) node).put(key, NC);
            }
        }
    }

    protected void forcerLaCasse(JsonNode node, String key, String newKey) {
        if (!isMissingNode(node)) {
            String newTexte = NC;
            if (canUpdate(node.path(key))) {
                String texte = node.path(key).textValue();
                if (StringUtils.isNotBlank(texte)) {
                    newTexte = StringUtils.capitalize(texte);
                } else {
                    LOGGER.error("Impossible d'ajouter une majuscule à {}", key);
                }
            }
            ((ObjectNode) node).put(newKey, newTexte);
        }
    }

    protected boolean canUpdate(JsonNode node) {
        boolean isNotNull = node != null && node.textValue() != null;
        boolean isNotMissing = !isMissingNode(node);
        boolean isNotEmpty = isNotNull && !node.textValue().isEmpty();
        return isNotNull && isNotMissing && isNotEmpty;
    }

    protected boolean isMissingNode(JsonNode node) {
        return node instanceof MissingNode;
    }

    protected void parseToFloat(JsonNode node, String key, String newKey) {
        Double parsed = 0.0;
        if (canUpdate(node.path(key))) {
            parsed = AfBackUtils.parseDoubleSafe(node.path(key).textValue());
        }
        ((ObjectNode) node).put(newKey, parsed);
    }

    protected void parseToNumber(JsonNode node, String key, String newKey) {
        Integer parsed = 0;
        if (canUpdate(node.path(key))) {
            String texte = node.path(key).textValue();
            if (StringUtils.isNotBlank(texte)) {
                parsed = Integer.parseInt(texte);
            } else {
                LOGGER.error("Impossible de parser en nombre {}", key);
            }
        }
        ((ObjectNode) node).put(newKey, parsed);
    }

}
