package mc.gouv.xaf.xaf12batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.servicerest.caching.PaysNationalitesCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DemandeTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeTransformer.class);

    public static final String DEFAULT_FRENCH_DATE_FORMAT = "dd/MM/yyyy";

    @Autowired
    private PaysNationalitesCache paysCache;

    public JsonNode getNodeFromPath(JsonNode contenu, String path) {
        String chemin = getCheminRelatif(path);
        return contenu.at(chemin);
    }

    public String getCheminRelatif(String path) {
        return path.replace("contenu.", "/").replace(".", "/");
    }

    public void setContenuTrad(JsonNode contenuTrad, JsonNode config) {
        JsonNode mappings = config.get("mappings");
        List<JsonNode> champsNodes = config.get("recap").findValues("champs");
        for (JsonNode champs : champsNodes) {
            for (JsonNode champ : champs) {
                JsonNode mapping = champ.get("mapping");
                String path = champ.get("path").asText();
                processContenuTrad(contenuTrad, mappings, mapping, champ, path);
            }
        }
        // récupérer aussi les champs tableau
        List<JsonNode> tableauxNodes = new ArrayList<>();
        extractTableauNodes(config.get("recap"), tableauxNodes);
        for (JsonNode tableau : tableauxNodes) {
            String rootPath = tableau.get("path").asText();
            // on regarde si dans le contenu on a une array correspondant à ce path
            JsonNode array = getNodeFromPath(contenuTrad, rootPath);
            for (JsonNode champ : tableau.get("columns")) {
                JsonNode mapping = champ.get("mapping");
                // il faut itérer sur chaque contenu du tableau
                for (int i = 0; i < array.size(); i++) {
                    String path = rootPath + "." + i + "." + champ.get("path").asText();
                    processContenuTrad(contenuTrad, mappings, mapping, champ, path);
                }

            }
        }
    }

    private void extractTableauNodes(JsonNode node, List<JsonNode> tableauNodes) {
        if (node.isObject()) {
            // Si le nœud est un objet JSON
            JsonNode columnsNode = node.get("columns");
            if (columnsNode != null && columnsNode.isArray()) {
                tableauNodes.add(node);
            }

            // Parcourir les enfants de l'objet
            node.fields().forEachRemaining(entry -> extractTableauNodes(entry.getValue(), tableauNodes));
        } else if (node.isArray()) {
            // Si le nœud est un tableau
            node.forEach(childNode -> extractTableauNodes(childNode, tableauNodes));
        }
    }

    private void processContenuTrad(JsonNode contenuTrad, JsonNode mappings, JsonNode mapping, JsonNode champ,
            String path) {
        // le champ a un mapping
        if (mapping != null) {
            // on récupère le champ correspondant dans le contenu s'il existe
            JsonNode enumKeyNode = getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull()) {
                String enumValue = "";
                String enumKey = enumKeyNode.asText();
                JsonNode isDynamic = champ.get("isDynamic");
                if (isDynamic != null && !isDynamic.asBoolean()) {
                    JsonNode enumFound = mappings.get(mapping.asText()).get("languages").get("fr").get("values")
                            .get(enumKey);
                    if (enumFound != null) {
                        // si on trouve l'enum, alors on récupère la valeur
                        enumValue = enumFound.asText();
                    } else {
                        // sinon cela veut dire que la traduction a déjà été effectuée du coup on peut réutiliser la valeur
                        enumValue = enumKey;
                    }
                } else if (mapping.asText().equals("nationalites")) {
                    enumValue = StringUtils.isBlank(enumKey)
                            ? ""
                            : paysCache.get(enumKey, "fr") != null
                                    ? paysCache.get(enumKey, "fr").getNationalite()
                                    : enumKey;
                } else if (mapping.asText().equals("pays")) {
                    enumValue = StringUtils.isBlank(enumKey)
                            ? ""
                            : paysCache.get(enumKey, "fr") != null ? paysCache.get(enumKey, "fr").getNom() : enumKey;
                }
                setNodeValue(contenuTrad, path, enumValue);
            }
        } else if (champ.get("type").asText().equals("adresse")) {
            // le champ est de type adresse donc on doit remplacer le pays
            path += ".pays";
            JsonNode enumKeyNode = getNodeFromPath(contenuTrad, path);
            if (enumKeyNode != null && !enumKeyNode.isNull()) {
                String enumKey = enumKeyNode.asText();
                String enumValue = StringUtils.isBlank(enumKey)
                        ? ""
                        : paysCache.get(enumKey, "fr") != null
                        ? paysCache.get(enumKey, "fr").getNom()
                                : enumKey;
                setNodeValue(contenuTrad, path, enumValue);
            }
        } else if (champ.get("type").asText().equals("date")) {
            JsonNode dateNode = getNodeFromPath(contenuTrad, path);
            if (dateNode != null && !dateNode.isNull()) {
                String date = dateNode.asText();
                setNodeValue(contenuTrad, path, changeDateStringFormat(date));
            }
        }
    }

    private String changeDateStringFormat(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern(DEFAULT_FRENCH_DATE_FORMAT));
        } catch (DateTimeParseException e) {
            // impossible de parser la date, elle est sûrement déjà au bon format
            return dateString;
        }
    }

    public void setNodeValue(JsonNode contenu, String path, String nouvelleValeur){
        // [contenu,donnee,demandeur,prenom]
        List<String> donneeExterneKeyArray = new ArrayList<>(Arrays.asList(path.split("\\.")));
        // [donnee,demandeur,prenom]
        donneeExterneKeyArray.removeFirst();
        //	 "[donnee,demandeur]" / field = prenom
        String field = donneeExterneKeyArray.removeLast();
        // "/donnee/demandeur"
        String p = "/" + String.join("/", donneeExterneKeyArray);
        ((ObjectNode) contenu.at(p)).put(field, nouvelleValeur);
    }


}
