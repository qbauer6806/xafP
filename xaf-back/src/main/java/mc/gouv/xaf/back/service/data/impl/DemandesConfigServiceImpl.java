package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.back.data.dao.DemandesConfigRepository;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.transformer.DemandesConfigTransformer;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesConfigServiceImpl implements DemandesConfigService {

    @Autowired
    private DemandesConfigRepository demandesConfigRepository;

    @Autowired
    private MarqueursService marqueursService;

    @Autowired
    private BrouillonsService brouillonsService;

    @Autowired
    private DemandesConfigTransformer demandesConfigTransformer;

    @Value("${maven.version}")
    private String mavenVersion;

    private static final String MODEL_PATH = "modelPaths";

    @Override
    public List<DemandeConfigBO> getConfigsBO() {
        return demandesConfigRepository.findAllByOrderByBuildIdDesc();
    }

    @Override
    public String getLastBuildId() {
        DemandeConfigBO configBO = demandesConfigRepository.findFirstByOrderByBuildIdDesc();
        return configBO != null ? configBO.getBuildId() : null;
    }

    @Override
    public DemandeConfigBO getLastConfig() {
        return demandesConfigRepository.findFirstByOrderByBuildIdDesc();
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
            ArrayNode rechercheAvancee = mapper.createArrayNode();
            findPaths(configNode.get("recap"), marqueurs, rechercheAvancee);
            // le noeud marqueurs contient tous les chemins
            ((ObjectNode) modelPaths).put("marqueurs", marqueurs);
            // le noeud rechercheAvancee ne contient pas les chemins des tableaux
            ((ObjectNode) modelPaths).put("rechercheAvancee", rechercheAvancee);
            ((ObjectNode) configNode).put("modelPaths", modelPaths);
            // on récupère la dernière config avant d'ajouter la nouvelle
            DemandeConfigBO lastConfig = getLastConfig();
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
        } else if (existingConfig.getVersion() != null && !existingConfig.getVersion().equals(mavenVersion)) {
            // si la config existe déjà, on met à jour la version avec la + récente si la version est différente
            existingConfig.setVersion(mavenVersion);
            demandesConfigRepository.save(existingConfig);
        }
        return mapper.createObjectNode();
    }

    private void findPaths(JsonNode recap, ArrayNode marqueurs, ArrayNode rechercheAvancee) {
        List<JsonNode> champsNodes = recap.findValues("champs");
        for (JsonNode champs : champsNodes) {
            for (JsonNode champ : champs) {
                String path = champ.get("path").asText();
                addToPathByType(marqueurs, champ, path);
                addToPathByType(rechercheAvancee, champ, path);
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
                addToPathByType(marqueurs, champ, path);
            }
        }
    }

    private void addToPathByType(ArrayNode arrayNode, JsonNode champ, String path) {
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

    private void addToPath(ArrayNode arrayNode, String path, String suffixe) {
        arrayNode.add(path + "." + suffixe);
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
    public List<String> getModelPathsRechercheAvancee() {
        DemandeConfigBO configBO = demandesConfigRepository.findOneByBuildId(getLastBuildId());
        return getModelPaths(configBO.getContenu().get(MODEL_PATH).get("rechercheAvancee"));
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
