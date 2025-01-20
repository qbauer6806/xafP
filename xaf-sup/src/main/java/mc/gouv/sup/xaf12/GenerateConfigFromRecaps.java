package mc.gouv.sup.xaf12;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateConfigFromRecaps {

    public static void main(String[] args) {
        System.out.println("------ Requête à copier -------");
        System.out.println();
        String resourceFolder = "xaf12";
        List<String> fileNames = getFileNamesInResourceFolder(resourceFolder);
        for (String buildId : extractBuildIds(fileNames)) {

            JsonNode recapBack;
            JsonNode recapFront;
            try {
                recapBack = readJsonFromFile("xaf12/recaps_" + buildId + ".json");
                recapFront = readJsonFromFile("xaf12/recapsFront_" + buildId + ".json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode config = mapper.createObjectNode();

            // buildId
            ((ObjectNode) config).put("buildId", recapFront.get("buildId"));

            // modelPaths
            JsonNode modelPaths = mapper.createObjectNode();
            // marqueurs
            ArrayNode marqueurs = mapper.createArrayNode();
            JsonNode displayFields = recapFront.get("initDonnees").get("projectDemande").get("displayFields");
            for (JsonNode displayField : displayFields) {
                String type = displayField.get("type").asText();
                JsonNode data = displayField.get("data");
                if (type.equals("adresse") || type.equals("adresseMc") || type.equals("iban") || type.equals(
                        "telephone")) {
                    for (JsonNode d : data) {
                        marqueurs.add(d.asText());
                    }
                } else if (!type.equals("fichier") && data.asText().startsWith("contenu.")) {
                    marqueurs.add(data.asText());
                }
            }
            // chercher les tableaux, dans le recapBack
            List<JsonNode> tableauxNodes = new ArrayList<>();
            extractTableauNodes(recapBack, tableauxNodes);
            for (JsonNode tableau : tableauxNodes) {
                String rootPath = tableau.get("path").asText();
                marqueurs.add(rootPath);
                for (JsonNode champ : tableau.get("columns")) {
                    // sur les types adresse, telephone... il n'y a pas de path
                    JsonNode pathNode = champ.get("path");
                    String path = rootPath + ".";
                    if (pathNode != null) {
                        path += champ.get("path").asText();
                    } else {
                        String type = champ.get("type").asText();
                        if (type.equals("adresse") || type.equals("adresseMc")) {
                            String suffix = "Ligne1";
                            String pathWithSuffix = champ.get("ligne1").asText();
                            path += pathWithSuffix.substring(0, pathWithSuffix.length() - suffix.length());
                        } else if (type.equals("iban")) {
                            String suffix = "Iban";
                            String pathWithSuffix = champ.get("iban").asText();
                            path += pathWithSuffix.substring(0, pathWithSuffix.length() - suffix.length());
                        } else if (type.equals("telephone")) {
                            String suffix = "Indicatif";
                            String pathWithSuffix = champ.get("indicatif").asText();
                            path += pathWithSuffix.substring(0, pathWithSuffix.length() - suffix.length());
                        }
                    }
                    addToPathByType(marqueurs, champ, path);
                }
            }

            ((ObjectNode) modelPaths).put("marqueurs", marqueurs);
            // pas besoin d'ajouter le noeud rechercheAvancee dans modelPaths car dans tous les cas un nouveau fichier config sera utilisé sur une montée de version XAF 12
            ((ObjectNode) config).put("modelPaths", modelPaths);

            JsonNode properties = recapFront.get("properties");
            cleanApostrophes((ObjectNode) properties);

            for (JsonNode n : recapBack) {
                if (n.get("name").asText().equals("projectDemandeRecap")) {

                    // recap
                    JsonNode recapConfig = mapper.createObjectNode();

                    // sections
                    ((ObjectNode) recapConfig).put("sections", n.get("sections"));
                    // update sections
                    JsonNode sections = recapConfig.get("sections");
                    for (JsonNode section : sections) {
                        // add titreKey
                        properties.get("fr").fields().forEachRemaining(property -> {
                            if (property.getValue().asText().equals(section.get("titre").asText())) {
                                ((ObjectNode) section).put("titreKey", property.getKey());
                            }
                        });

                        // section
                        if (section.get("type").asText().equals("champs")) {
                            for (JsonNode champ : section.get("champs")) {
                                traitementChamp(champ, recapFront);
                            }

                        }
                        // sous section
                        else if (section.get("type").asText().equals("sousSections")) {
                            for (JsonNode sousSection : section.get("sousSections")) {
                                String type = sousSection.get("type").asText();
                                if ("tableau".equals(type)) {
                                    for (JsonNode columns : sousSection.get("columns")) {
                                        traitementChamp(columns, recapFront);
                                    }
                                } else if ("champs".equals(type)) {
                                    for (JsonNode champ : sousSection.get("champs")) {
                                        traitementChamp(champ, recapFront);
                                    }
                                }
                            }
                        }
                    }

                    // fichiers
                    ArrayNode fichiers = mapper.createArrayNode();
                    JsonNode fichiersNode = mapper.createObjectNode();
                    ArrayNode champs = mapper.createArrayNode();
                    JsonNode fichiersRecapsFront = null;
                    // find fichiers
                    for (JsonNode sectionsRecapsFront : recapFront.get("initRecaps").get("projectDemandeRecap")
                            .get("sections")) {
                        if (sectionsRecapsFront.get("type").asText().equals("fichiers")) {

                            String titreKey = sectionsRecapsFront.get("titre").asText();
                            // find titre in properties
                            properties.get("fr").fields().forEachRemaining(property -> {
                                if (property.getKey().equals(titreKey)) {
                                    ((ObjectNode) fichiersNode).put("titre", property.getValue().asText());
                                }
                            });
                            ((ObjectNode) fichiersNode).put("titreKey", titreKey);
                            ((ObjectNode) fichiersNode).put("type", sectionsRecapsFront.get("type").asText());

                            fichiersRecapsFront = sectionsRecapsFront.get("fichiers");

                            // create champs
                            int fileIndexCounter = 0;
                            for (JsonNode column : fichiersRecapsFront.get("columns")) {
                                JsonNode champ = mapper.createObjectNode();
                                ((ObjectNode) champ).put("type", column.get("type").asText());
                                String idPrefix = column.get("idPrefix").asText();
                                ((ObjectNode) champ).put("idPrefix", idPrefix);
                                properties.get("fr").fields().forEachRemaining(property -> {
                                    if (property.getKey().contains("." + idPrefix + ".")) {
                                        ((ObjectNode) champ).put("label", property.getValue().asText());
                                        ((ObjectNode) champ).put("labelKey", property.getKey());
                                    }
                                });
                                // fileIndex
                                ((ObjectNode) champ).put("fileIndex", fileIndexCounter);
                                // path
                                ArrayNode paths = mapper.createArrayNode();
                                // sur des vieux recaps FO il n'y a pas nombre, donc on set à 1
                                JsonNode nombreNode = column.get("fichierParameter").get("nombre");
                                int nombre = nombreNode != null ? nombreNode.asInt() : 1;
                                for (int i = 0; i < nombre; fileIndexCounter++, i++) {
                                    paths.add("fichiers[" + fileIndexCounter + "]");
                                }
                                ((ObjectNode) champ).put("path", paths);

                                champs.add(champ);
                            }

                            break;
                        }
                    }
                    ((ObjectNode) fichiersNode).put("champs", champs);
                    fichiers.add(fichiersNode);

                    // idPrefix
                    if (fichiersRecapsFront != null) {
                        ((ObjectNode) fichiersNode).put("idPrefix", fichiersRecapsFront.get("idPrefix").asText());
                    }
                    ((ObjectNode) fichiersNode).put("path", "fichiers");
                    ((ObjectNode) fichiersNode).put("virtual", true);
                    ((ObjectNode) recapConfig).put("fichiers", fichiers);

                    // name
                    ((ObjectNode) recapConfig).put("name", n.get("name").asText());

                    // donnee
                    ((ObjectNode) recapConfig).put("donnee", n.get("donnee").asText());

                    ((ObjectNode) config).put("recap", recapConfig);

                    // mappings
                    ((ObjectNode) config).put("mappings", recapFront.get("mappings"));

                    // translations
                    ((ObjectNode) config).put("translations", properties);

                    break;
                }
            }
            System.out.println(
                    "INSERT INTO TSCode.DEM_DEMANDES_CONFIG (build_id, contenu) values (" + config.get("buildId")
                            .asText() + ",'" + config.toString().replace("'", "''")
                            + "') ON CONFLICT (build_id) DO UPDATE SET contenu = EXCLUDED.contenu;");
            System.out.println();
        }

        System.out.println("------ Fin requête -------");

    }

    private static void extractTableauNodes(JsonNode node, List<JsonNode> tableauNodes) {
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

    private static void addToPathByType(ArrayNode arrayNode, JsonNode champ, String path) {
        if (!path.isEmpty()) {
            String type = champ.get("type").asText();
            switch (type) {
                case "adresse" -> {
                    addToPath(arrayNode, path, "ligne1");
                    addToPath(arrayNode, path, "ligne2");
                    addToPath(arrayNode, path, "ligne3");
                    addToPath(arrayNode, path, "codePostal");
                    addToPath(arrayNode, path, "ville");
                    addToPath(arrayNode, path, "pays");
                }
                case "adresseMc" -> {
                    addToPath(arrayNode, path, "ligne1");
                    addToPath(arrayNode, path, "ligne2");
                    addToPath(arrayNode, path, "ligne3");
                }
                case "iban" -> {
                    addToPath(arrayNode, path, "iban");
                    addToPath(arrayNode, path, "bic");
                    addToPath(arrayNode, path, "titulaire");
                }
                case "telephone" -> {
                    addToPath(arrayNode, path, "indicatif");
                    addToPath(arrayNode, path, "numero");
                }
                default -> arrayNode.add(path);
            }
        }
    }

    private static void addToPath(ArrayNode arrayNode, String path, String suffixe) {
        arrayNode.add(path + "." + suffixe);
    }

    private static void cleanApostrophes(ObjectNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();

            if (valueNode.isObject()) {
                // Si le champ est un objet, on appelle la fonction récursive
                cleanApostrophes((ObjectNode) valueNode);
            } else if (valueNode.isTextual()) {
                // Si le champ est une chaîne de caractères, on nettoie les apostrophes
                String value = valueNode.asText();
                String cleanedValue = value.replaceAll("(^')|('$)", "");
                node.put(field.getKey(), cleanedValue);
            }
        }
    }

    public static List<String> extractBuildIds(List<String> fileNames) {
        Set<String> uniqueNumbers = new HashSet<>();
        Pattern pattern = Pattern.compile("\\d+");

        for (String fileName : fileNames) {
            Matcher matcher = pattern.matcher(fileName);
            while (matcher.find()) {
                uniqueNumbers.add(matcher.group());
            }
        }

        return new ArrayList<>(uniqueNumbers);
    }

    public static List<String> getFileNamesInResourceFolder(String folderName) {
        ClassLoader classLoader = GenerateConfigFromRecaps.class.getClassLoader();
        URL resource = classLoader.getResource(folderName);
        if (resource == null) {
            System.out.println("Le dossier " + folderName + " est vide ou n'existe pas.");
            return new ArrayList<>();
        }

        File folder = new File(resource.getFile());
        File[] listOfFiles = folder.listFiles();

        List<String> fileNames = new ArrayList<>();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        } else {
            System.out.println("Le dossier " + folderName + " est vide ou n'existe pas.");
        }

        return fileNames;
    }

    private static void traitementChamp(JsonNode champ, JsonNode recapFront) {
        // add path
        // dans les champs classique on a .ligne1 à la fin des path, mais dans les tableaux on a Ligne1
        if (champ.get("type").asText().equals("adresseMc") || champ.get("type").asText().equals("adresse")) {
            ((ObjectNode) champ).put("path",
                    champ.get("ligne1").asText().replace(".ligne1", "").replaceAll("Ligne1$", ""));
            ((ObjectNode) champ).remove("ligne1");
            ((ObjectNode) champ).remove("ligne2");
            ((ObjectNode) champ).remove("ligne3");
            ((ObjectNode) champ).remove("codePostal");
            ((ObjectNode) champ).remove("ville");
            ((ObjectNode) champ).remove("pays");
        } else if (champ.get("type").asText().equals("iban")) {
            ((ObjectNode) champ).put("path", champ.get("iban").asText().replace(".iban", "").replaceAll("Iban$", ""));
            ((ObjectNode) champ).remove("iban");
            ((ObjectNode) champ).remove("bic");
            ((ObjectNode) champ).remove("titulaire");
        } else if (champ.get("type").asText().equals("telephone")) {
            ((ObjectNode) champ).put("path",
                    champ.get("indicatif").asText().replace(".indicatif", "").replaceAll("Indicatif$", ""));
            ((ObjectNode) champ).remove("indicatif");
            ((ObjectNode) champ).remove("numero");
        }

        // find idPrefix, dans des vieux recaps il n'est pas présent, uniquement dans le recapFront
        String idPrefix = getIdPrefix(champ, recapFront);
        // add labelKey
        ((ObjectNode) champ).put("labelKey", "ts.donnee.projectDemande." + idPrefix);
    }

    private static String getIdPrefix(JsonNode champ, JsonNode recapFront) {
        String idPrefix = "";
        String idPrefixChamp = getIdPrefixChamp(champ, recapFront);
        if (StringUtils.isNotBlank(idPrefixChamp)) {
            idPrefix = idPrefixChamp.concat(".nomChamp");
        } else if (champ.get("path").asText().contains("Tableau")) {
            idPrefix = champ.get("path").asText() + ".nomHeader";
        }
        return idPrefix;
    }

    private static String getIdPrefixChamp(JsonNode champ, JsonNode recapFront) {
        String idPrefix = "";
        for (Iterator<Entry<String, JsonNode>> it = recapFront.get("initDonnees").get("projectDemande")
                .get("displayFields").fields(); it.hasNext(); ) {
            Entry<String, JsonNode> e = it.next();
            JsonNode data = e.getValue().get("data");
            String dataString = data.asText();
            if (data.isArray()) {
                dataString = data.get(0).asText();
            }
            if (dataString.equals(champ.get("path").asText()) || dataString.contains(
                    champ.get("path").asText() + ".")) {
                idPrefix = e.getKey();
                break;
            }

        }
        return idPrefix;
    }

    private static JsonNode readJsonFromFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Get the file from the resources folder as InputStream
        InputStream resourceAsStream = GenerateConfigFromRecaps.class.getClassLoader().getResourceAsStream(filePath);
        if (resourceAsStream == null) {
            throw new IOException("File not found: " + filePath);
        }

        // Read and parse the JSON file into JsonNode
        return objectMapper.readTree(resourceAsStream);
    }
}
