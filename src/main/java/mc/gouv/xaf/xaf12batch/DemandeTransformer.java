package mc.gouv.xaf.xaf12batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mc.gouv.servicerest.caching.PaysNationalitesCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                // le champ a un mapping
                if (mapping != null) {
                    // on récupère le champ correspondant dans le contenu s'il existe
                    JsonNode enumKeyNode = getNodeFromPath(contenuTrad, path);
                    if (enumKeyNode != null && !enumKeyNode.isNull()) {
                        String enumValue = "";
                        JsonNode isDynamic = champ.get("isDynamic");
                        String enumKey = enumKeyNode.asText();
                        if (isDynamic != null && !isDynamic.asBoolean()) {
                            enumValue = mappings.get(mapping.asText()).get("languages").get("fr").get("values").get(enumKey).asText();
                        } else if (mapping.asText().equals("nationalites")) {
                            enumValue = StringUtils.isBlank(enumKey) ? "" : paysCache.get(enumKey, "fr").getNationalite();
                        } else if (mapping.asText().equals("pays")) {
                            enumValue = StringUtils.isBlank(enumKey) ? "" : paysCache.get(enumKey, "fr").getNom();
                        }
                        setNodeValue(contenuTrad, path, enumValue);
                    }
                } else if (champ.get("type").asText().equals("adresse")) {
                    // le champ est de type adresse donc on doit remplacer le pays
                    path += ".pays";
                    JsonNode enumKeyNode = getNodeFromPath(contenuTrad, path);
                    if (enumKeyNode != null && !enumKeyNode.isNull()) {
                        String enumKey = enumKeyNode.asText();
                        String enumValue = StringUtils.isBlank(enumKey) ? "" : paysCache.get(enumKey, "fr").getNom();
                        setNodeValue(contenuTrad, path, enumValue);
                    }
                } else if (champ.get("type").asText().equals("date")) {
                    JsonNode dateNode = getNodeFromPath(contenuTrad, path);
                    if(dateNode != null && !dateNode.isNull()) {
                        String date = dateNode.asText();
                        setNodeValue(contenuTrad, path, changeDateStringFormat(date));
                    }
                }
            }
        }
    }

    private String changeDateStringFormat(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .format(DateTimeFormatter.ofPattern(DEFAULT_FRENCH_DATE_FORMAT));
    }

    public void setNodeValue(JsonNode contenu, String path, String nouvelleValeur){
        // [contenu,donnee,demandeur,prenom]
        List<String> donneeExterneKeyArray = new ArrayList<>(Arrays.asList(path.split("\\.")));
        // [donnee,demandeur,prenom]
        donneeExterneKeyArray.remove(0);
        //	 "[donnee,demandeur]" / field = prenom
        String field = donneeExterneKeyArray.remove(donneeExterneKeyArray.size() - 1);
        // "/donnee/demandeur"
        String p = "/" + String.join("/", donneeExterneKeyArray);
        ((ObjectNode) contenu.at(p)).put(field, nouvelleValeur);
    }


}
