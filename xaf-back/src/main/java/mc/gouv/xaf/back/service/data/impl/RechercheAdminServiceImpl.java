package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mc.gouv.xaf.back.data.dao.RechercheCatConfigRepository;
import mc.gouv.xaf.back.data.dao.RechercheChampConfigRepository;
import mc.gouv.xaf.back.data.entity.RechercheCatConfigBO;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.model.ExportImportCategoryDTO;
import mc.gouv.xaf.back.data.model.ExportImportConfigDTO;
import mc.gouv.xaf.back.data.model.ExportImportConfigPropertyDTO;
import mc.gouv.xaf.back.data.model.RechercheCategoryDTO;
import mc.gouv.xaf.back.data.model.RechercheChampDTO;
import mc.gouv.xaf.back.exception.CategoryAlreadyExist;
import mc.gouv.xaf.back.exception.UsedCategoryException;
import mc.gouv.xaf.back.service.data.RechercheAdminService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.HTMLEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class RechercheAdminServiceImpl implements RechercheAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheAdminServiceImpl.class);

    @Autowired
    private RechercheChampConfigRepository rechercheChampConfigRepository;

    @Autowired
    private RechercheCatConfigRepository rechercheCatConfigRepository;

    @Override
    public List<RechercheChampDTO> getRechercheChamps() {
        // On récupère les catégories pour classer les propriétés
        List<RechercheCategoryDTO> categories = getCategories();
        Collections.sort(categories);

        List<RechercheChampDTO> rechercheChampDTOS = new ArrayList<>();

        List<RechercheChampConfigBO> rechercheChampConfigBOS = rechercheChampConfigRepository.findAll();

        for (RechercheChampConfigBO rechercheChampConfigBO : rechercheChampConfigBOS) {
            RechercheChampDTO rechercheChampDTO = new RechercheChampDTO();
            rechercheChampDTO.setCategoryId((rechercheChampConfigBO.getCategorie() != null)
                    ? rechercheChampConfigBO.getCategorie().getId()
                    : null);
            rechercheChampDTO.setEnabled(rechercheChampConfigBO.isEnabled());
            rechercheChampDTO.setEditable(rechercheChampConfigBO.isEditable());
            rechercheChampDTO.setAllCategories(categories);
            String escapedLabel = HTMLEscapeUtils.escape(rechercheChampConfigBO.getLibelle());
            rechercheChampDTO.setLabel(escapedLabel);
            rechercheChampDTO.setName(rechercheChampConfigBO.getCle());
            rechercheChampDTOS.add(rechercheChampDTO);
        }

        return rechercheChampDTOS;
    }

    @Override
    public void updateRechercheChamps(List<RechercheChampDTO> rechercheChampDTOS) {
        LOGGER.info("Début de la maj des propriétés");
        if (rechercheChampDTOS != null) {

            // Enregistrement des propriétés et catégories
            for (RechercheChampDTO rechercheChampDTO : rechercheChampDTOS) {
                updateRechercheChamp(rechercheChampDTO);
            }
        }
        LOGGER.info("Fin de la maj des propriétés");
    }

    private void updateRechercheChamp(RechercheChampDTO rechercheChampDTO) {
        String safeChamp = AfBackUtils.logSafe(rechercheChampDTO.getName());
        LOGGER.info("Début de la maj de la propriété {}", safeChamp);
        RechercheChampConfigBO champBo = rechercheChampConfigRepository.findByCle(rechercheChampDTO.getName());

        // Vérification de l'existence de la propriété
        if (champBo == null) {
            LOGGER.info("La propriété n'existe pas, création de la propriété\nClé: {}", safeChamp);
            champBo = new RechercheChampConfigBO();
            champBo.setCle(rechercheChampDTO.getName());
            champBo.setEditable(true);
        }

        // Association de la catégorie
        if (rechercheChampDTO.getCategoryId() != null) {
            Optional<RechercheCatConfigBO> catBoOp = rechercheCatConfigRepository.findById(
                    rechercheChampDTO.getCategoryId());
            if (catBoOp.isPresent()) {
                champBo.setCategorie(catBoOp.get());
            }
        } else {
            LOGGER.info("La propriété n'est pas associée à une catégorie");
        }

        // Sauvegarde de la propriété
        LOGGER.info("Enabled : {}", rechercheChampDTO.isEnabled());
        champBo.setEnabled(rechercheChampDTO.isEnabled());
        String safeLabel = AfBackUtils.logSafe(rechercheChampDTO.getLabel());
        LOGGER.info("Libelle : {}", safeLabel);
        champBo.setLibelle(rechercheChampDTO.getLabel());
        rechercheChampConfigRepository.save(champBo);

        LOGGER.info("Fin de la maj de la propriété {}", safeChamp);
    }

    @Override
    public RechercheCategoryDTO addCategory(String label) {
        String safeLabel = AfBackUtils.logSafe(label);
        LOGGER.info("Début de l'ajout de la catégorie {}", safeLabel);
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("Le libellé de la catégorie ne peut pas être vide");
        }

        RechercheCatConfigBO catBo = rechercheCatConfigRepository.findByLibelle(label);
        if (catBo != null) {
            throw new CategoryAlreadyExist("La catégorie " + label + " existe dejà");
        }

        RechercheCatConfigBO newCatBo = rechercheCatConfigRepository.save(new RechercheCatConfigBO(label, true));

        RechercheCategoryDTO category = new RechercheCategoryDTO(newCatBo.getId(), newCatBo.getLibelle(), true);
        LOGGER.info("Fin de l'ajout de la catégorie {}", safeLabel);
        return category;
    }

    @Override
    public void deleteCategory(Integer id) {

        LOGGER.info("Début de la suppression de la catégorie {}", id);
        List<RechercheChampConfigBO> properties = rechercheChampConfigRepository.findByCategorieId(id);
        if (properties != null && !properties.isEmpty()) {
            throw new UsedCategoryException("La catégorie est dejà utilisée");
        }

        Optional<RechercheCatConfigBO> categoryOpt = rechercheCatConfigRepository.findById(id);
        if (categoryOpt.isPresent() && categoryOpt.get().isEditable()) {
            rechercheCatConfigRepository.deleteById(id);
        }
        LOGGER.info("Fin de la suppression de la catégorie {}", id);
    }

    @Override
    public List<RechercheCategoryDTO> updateCategories(List<RechercheCategoryDTO> categories) {
        LOGGER.info("Début de la maj des catégories");
        if (categories != null) {
            List<RechercheCategoryDTO> cats = new ArrayList<>();
            for (RechercheCategoryDTO cat : categories) {
                cats.add(updateCategory(cat));
            }
            LOGGER.info("Fin de la maj des catégories");
            return cats;
        }
        LOGGER.info("Fin de la maj des catégories");
        return new ArrayList<>();
    }

    private RechercheCategoryDTO updateCategory(RechercheCategoryDTO category) {

        LOGGER.info("Début de la maj de la catégorie");
        if (category != null) {
            String safeLabel = AfBackUtils.logSafe(category.getLabel());
            LOGGER.info("Catégorie: libelle : {}, isEditable: {}", safeLabel, category.isEditable());
            Optional<RechercheCatConfigBO> catBoOpt = rechercheCatConfigRepository.findById(category.getId());
            if (catBoOpt.isPresent() && catBoOpt.get().isEditable()) {
                RechercheCatConfigBO catBo = catBoOpt.get();
                catBo.setLibelle(category.getLabel());
                RechercheCatConfigBO updatedCat = rechercheCatConfigRepository.save(catBo);
                return new RechercheCategoryDTO(updatedCat.getId(), updatedCat.getLibelle(), updatedCat.isEditable());
            }
        }
        LOGGER.info("Fin de la maj de la catégorie");
        return null;
    }

    @Override
    public List<RechercheCategoryDTO> getCategories() {
        return rechercheCatConfigRepository.findAll().stream()
                .map(cat -> new RechercheCategoryDTO(cat.getId(), HTMLEscapeUtils.escape(cat.getLibelle()),
                        cat.isEditable())).toList();
    }

    @Override
    public String exportConfig() throws IOException {

        LOGGER.info("Début de l'export de la configuration");

        ExportImportConfigDTO exportConfig = new ExportImportConfigDTO();
        Iterable<RechercheCatConfigBO> categoriesBo = rechercheCatConfigRepository.findAll();
        for (RechercheCatConfigBO catConfig : categoriesBo) {
            exportConfig.getCategories()
                    .add(new ExportImportCategoryDTO(catConfig.getLibelle(), catConfig.isEditable()));
        }

        Iterable<RechercheChampConfigBO> champsBo = rechercheChampConfigRepository.findAll();
        for (RechercheChampConfigBO configConfig : champsBo) {
            ExportImportConfigPropertyDTO exportConfigPropertyDTO = new ExportImportConfigPropertyDTO();
            if (configConfig.getCategorie() != null) {
                exportConfigPropertyDTO.setCategoryName(configConfig.getCategorie().getLibelle());
            }
            exportConfigPropertyDTO.setEditable(configConfig.isEditable());
            exportConfigPropertyDTO.setEnabled(configConfig.isEnabled());
            exportConfigPropertyDTO.setLabel(configConfig.getLibelle());
            exportConfigPropertyDTO.setName(configConfig.getCle());
            exportConfig.getProperties().add(exportConfigPropertyDTO);
        }

        ObjectMapper mapper = new ObjectMapper();
        String exportedConfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportConfig);
        LOGGER.debug("Fin de l'export de la configuration, fichier exporté {}", exportedConfig);
        return exportedConfig;
    }

    @Override
    public void importConfig(byte[] file) throws IOException {

        LOGGER.info("Début de l'import de la configuration");

        ObjectMapper mapper = new ObjectMapper();
        ExportImportConfigDTO config;
        try {
            config = mapper.readValue(file, ExportImportConfigDTO.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new BadRequestException("Le fichier ne respecte pas la structure des fichiers à importer");
        }

        if (config != null) {

            rechercheChampConfigRepository.deleteAll();
            rechercheCatConfigRepository.deleteAll();

            //https://stackoverflow.com/questions/42124030/delete-then-create-records-are-causing-a-duplicate-key-violation-with-spring-dat
            rechercheCatConfigRepository.findAll();
            rechercheChampConfigRepository.findAll();

            Map<String, RechercheCatConfigBO> categoriesMap = new HashMap<>();
            List<ExportImportCategoryDTO> categories = config.getCategories();
            if (categories != null) {
                for (ExportImportCategoryDTO category : categories) {
                    categoriesMap.put(category.getLabel(), rechercheCatConfigRepository.save(
                            new RechercheCatConfigBO(category.getLabel(), category.isEditable())));
                }
            }

            List<ExportImportConfigPropertyDTO> properties = config.getProperties();
            if (properties != null) {
                for (ExportImportConfigPropertyDTO property : properties) {

                    RechercheChampConfigBO champConfig = new RechercheChampConfigBO();
                    champConfig.setCle(property.getName());
                    champConfig.setCategorie(categoriesMap.get(property.getCategoryName()));
                    champConfig.setEditable(property.isEditable());
                    champConfig.setLibelle(property.getLabel());
                    champConfig.setEnabled(property.isEnabled());

                    rechercheChampConfigRepository.save(champConfig);
                }
            }
        }

        LOGGER.info("Fin de l'import de la configuration");

    }

    @Override
    public void refreshConfigs(JsonNode config, Map<String, String> rechercheAvancee) {
        refreshCategories(config);
        refreshChamps(config, rechercheAvancee);
    }

    private void refreshCategories(JsonNode config) {
        // On récupère les catégories existantes
        List<RechercheCategoryDTO> categories = rechercheCatConfigRepository.findAll().stream()
                .map(cat -> new RechercheCategoryDTO(cat.getId(), HTMLEscapeUtils.escape(cat.getLibelle()),
                        cat.isEditable())).toList();

        // Ensemble des libellés déjà présents (échappés)
        Set<String> existingLabels = categories.stream().map(RechercheCategoryDTO::getLabel)
                .collect(Collectors.toSet());

        // On récupère les titres des sections de la config
        JsonNode sections = config.path("recap").path("sections");

        if (sections.isArray()) {
            for (JsonNode section : sections) {
                String titre = section.path("titre").asText(null);
                if (titre != null) {
                    String titreEscape = HTMLEscapeUtils.escape(titre);
                    if (existingLabels.add(titreEscape)) { // add() retourne false si déjà présent
                        rechercheCatConfigRepository.save(new RechercheCatConfigBO(titre, true));
                    }
                }
            }
        }
    }

    private void refreshChamps(JsonNode config, Map<String, String> rechercheAvancee) {
        // Supprimer les champs dont la clé commence par "contenu" et qui ne sont pas dans la Map
        List<RechercheChampConfigBO> champsExistants = rechercheChampConfigRepository.findAll();
        Set<String> keysRechercheAvancee = rechercheAvancee.keySet();

        List<RechercheChampConfigBO> champsASupprimer = champsExistants.stream()
                .filter(champ -> champ.getCle() != null && champ.getCle().startsWith("contenu")
                        && !keysRechercheAvancee.contains(champ.getCle())).toList();

        rechercheChampConfigRepository.deleteAll(champsASupprimer);

        // On récupère les catégories existantes
        List<RechercheCategoryDTO> categories = rechercheCatConfigRepository.findAll().stream()
                .map(cat -> new RechercheCategoryDTO(cat.getId(), HTMLEscapeUtils.escape(cat.getLibelle()),
                        cat.isEditable())).toList();
        // On récupère les titres des sections de la config
        JsonNode sections = config.path("recap").path("sections");
        rechercheAvancee.forEach((path, label) -> {
            // Vérifie si un champ avec cette clé existe déjà
            boolean exists = rechercheChampConfigRepository.existsByCle(path);
            if (exists) {
                // Champ déjà présent, on ne fait rien
                return;
            }
            RechercheChampConfigBO bo = new RechercheChampConfigBO();
            bo.setCle(path);
            bo.setLibelle(label);
            bo.setEditable(true);
            bo.setEnabled(true);
            String categorieLibelle = HTMLEscapeUtils.escape(findSectionTitleByPath(sections, path));
            // Récupère l'entité existante depuis la BDD
            Integer catId = categories.stream().filter(c -> c.getLabel().equals(categorieLibelle))
                    .map(RechercheCategoryDTO::getId).findFirst().orElse(null);

            if (catId != null) {
                RechercheCatConfigBO catConfigBO = rechercheCatConfigRepository.findById(catId)
                        .orElseThrow(() -> new IllegalStateException("Catégorie non trouvée pour l'id: " + catId));
                bo.setCategorie(catConfigBO);

            }
            rechercheChampConfigRepository.save(bo);
        });

    }

    private String findSectionTitleByPath(JsonNode sections, String targetPath) {
        String[] suffixes = { "ligne1", "ligne2", "ligne3", "ville", "pays", "codePostal", "bic", "iban", "titulaire",
                "indicatif", "numero" };

        for (String suffix : suffixes) {
            String suffixPattern = "." + suffix;
            if (targetPath.endsWith(suffixPattern)) {
                targetPath = targetPath.substring(0, targetPath.length() - suffixPattern.length());
                break; // dès qu’on trouve un suffixe correspondant, on s’arrête
            }
        }
        for (JsonNode section : sections) {
            String titre = section.path("titre").asText(null);

            // Vérifie si un champ contient le path
            boolean match =
                    section.path("champs").findValuesAsText("path").contains(targetPath) || section.path("columns")
                            .findValuesAsText("path").contains(targetPath);

            if (match) {
                return titre;
            }
        }
        return null;
    }
}
