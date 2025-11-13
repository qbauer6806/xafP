package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesConfigRepository;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.transformer.DemandesConfigTransformer;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.back.service.data.RechercheAdminService;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesConfigServiceImpl implements DemandesConfigService {

    private final DemandesConfigRepository demandesConfigRepository;
    private final MarqueursService marqueursService;
    private final BrouillonsService brouillonsService;
    private final DemandesConfigTransformer demandesConfigTransformer;
    private final RechercheAdminService rechercheAdminService;
    private final DemandesConfigHelperService demandesConfigHelperService;

    @Value("${maven.version}")
    private String mavenVersion;

    private static final Map<String, List<String>> TYPE_SUBPATHS = Map.of("adresse",
            List.of("ligne1", "ligne2", "ligne3", "codePostal", "ville", "pays"), "adresseMc",
            List.of("ligne1", "ligne2", "ligne3"), "iban", List.of("iban", "bic", "titulaire"), "telephone",
            List.of("indicatif", "numero"));

    private static final String MODEL_PATH = "modelPaths";

    @Override
    public List<DemandeConfigBO> getConfigsBO() {
        return demandesConfigRepository.findAllByOrderByBuildIdDesc();
    }

    @Override
    public DemandeConfigBO getConfig(String buildId) {
        return demandesConfigRepository.findOneByBuildId(buildId);
    }

    @Override
    public JsonNode saveConfig(JsonNode configNode) {
        ObjectMapper mapper = new ObjectMapper();
        String buildId = configNode.get("buildId").asText();
        // si la config existe et que son contenu et != null, on ne la sauvegarde pas
        DemandeConfigBO existingConfig = demandesConfigRepository.findOneByBuildId(buildId);

        if (existingConfig == null || existingConfig.getContenu() == null) {
            // on génère le noeud modelPaths
            JsonNode modelPaths = mapper.createObjectNode();
            ArrayNode marqueurs = mapper.createArrayNode();
            Map<String, String> rechercheAvancee = new HashMap<>();
            findPaths(configNode.get("recap"), marqueurs, rechercheAvancee);
            // le noeud marqueurs contient tous les chemins
            ((ObjectNode) modelPaths).put("marqueurs", marqueurs);
            ((ObjectNode) configNode).put("modelPaths", modelPaths);
            // on récupère la dernière config avant d'ajouter la nouvelle
            DemandeConfigBO lastConfig = demandesConfigHelperService.getLastConfig();
            String lastBuildId = lastConfig != null ? lastConfig.getBuildId() : null;
            // on sauvegarde la nouvelle config
            DemandeConfigBO newConfig = demandesConfigRepository.save(demandesConfigTransformer.json2Bo(configNode));
            if (lastBuildId != null) {
                // on vérifie par rapport à la dernière config si la nouvelle a le même modèle ou si le modèle a changé
                checkIfModelChanged(lastConfig, newConfig);
            }
            // on génère les marqueurs pour la nouvelle config
            marqueursService.copyOrGenerateMarqueurs(lastBuildId, buildId,
                    getModelPaths(configNode.get(MODEL_PATH).get("marqueurs")), configNode);

            // on refresh les cat config et champ config (recherche avancée)
            rechercheAdminService.refreshConfigs(configNode, rechercheAvancee);
        } else if (existingConfig.getVersion() != null && !existingConfig.getVersion().equals(mavenVersion)) {
            // si la config existe déjà, on met à jour la version avec la + récente si la version est différente
            existingConfig.setVersion(mavenVersion);
            demandesConfigRepository.save(existingConfig);
        }
        return mapper.createObjectNode();
    }

    private void findPaths(JsonNode recap, ArrayNode marqueurs, Map<String, String> rechercheAvancee) {
        List<JsonNode> champsNodes = recap.findValues("champs");
        for (JsonNode champs : champsNodes) {
            for (JsonNode champ : champs) {
                String type = champ.get("type").asText();
                if (!type.equals("tableau")) {
                    String path = champ.get("path").asText();
                    addToPathByType(marqueurs, type, path);
                    String label = champ.get("label").asText();
                    addToPathByType(rechercheAvancee, type, path, label);
                }

            }
        }
        // récupérer aussi les champs tableau
        List<JsonNode> tableauxNodes = new ArrayList<>();
        extractTableauNodes(recap, tableauxNodes);
        for (JsonNode tableau : tableauxNodes) {
            String rootPath = tableau.get("path").asText();
            marqueurs.add(rootPath);
            for (JsonNode champ : tableau.get("columns")) {
                String path = rootPath + "." + champ.get("path").asText();
                addToPathByType(marqueurs, champ.get("type").asText(), path);
            }
        }
    }

    private void addToPathByType(Map<String, String> rechercheAvancee, String type, String path, String label) {
        if (path.isEmpty()) {
            return;
        }

        List<String> subPaths = TYPE_SUBPATHS.get(type);
        if (subPaths != null) {
            for (String sub : subPaths) {
                rechercheAvancee.put(getCompletePath(path, sub), label + " - " + capitalize(sub));
            }
        } else {
            rechercheAvancee.put(path, label);
        }
    }

    private void addToPathByType(ArrayNode arrayNode, String type, String path) {
        if (path.isEmpty()) {
            return;
        }

        List<String> subPaths = TYPE_SUBPATHS.get(type);
        if (subPaths != null) {
            for (String sub : subPaths) {
                arrayNode.add(getCompletePath(path, sub));
            }
        } else {
            arrayNode.add(path);
        }
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String getCompletePath(String path, String suffixe) {
        return path + "." + suffixe;
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

    private void checkIfModelChanged(DemandeConfigBO lastConfig, DemandeConfigBO newConfig) {
        // on compare les noeuds recap et mappings
        JsonNode recap = lastConfig.getContenu().get("recap");
        JsonNode mappings = lastConfig.getContenu().get("mappings");
        JsonNode recapNewConfig = newConfig.getContenu().get("recap");
        JsonNode mappingsNewConfig = newConfig.getContenu().get("mappings");
        // Vérifier si les deux listes ont la même taille
        if (recap.equals(recapNewConfig) && mappings.equals(mappingsNewConfig)) {
            // même modèle, on se permet de mettre tous les brouillons associés à ce buildId au nouveau buildId
            // utile notamment pour savoir si un brouillon est obsolète
            brouillonsService.updateBrouillonsBuildId(lastConfig.getBuildId(), newConfig.getBuildId());
        }
    }

    @Override
    public List<String> getModelPathsMarqueurs(String buildId) {
        DemandeConfigBO configBO = demandesConfigRepository.findOneByBuildId(buildId);
        return getModelPaths(configBO.getContenu().get(MODEL_PATH).get("marqueurs"));
    }

    @Override
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
            throw new DemarcheException("Erreur lors de la récupération des chemins", e);
        }
    }

}
