package mc.gouv.xaf.back.service.es.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.RechercheCatConfigRepository;
import mc.gouv.xaf.back.data.dao.RechercheChampConfigRepository;
import mc.gouv.xaf.back.data.entity.RechercheCatConfigBO;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.es.model.ConfigCategoriesDTO;
import mc.gouv.xaf.back.data.es.model.ConfigPropertiesDTO;
import mc.gouv.xaf.back.data.es.model.ConfigPropertyDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsDTO;
import mc.gouv.xaf.back.data.es.model.EsCategory;
import mc.gouv.xaf.back.data.es.model.EsProperty;
import mc.gouv.xaf.back.data.es.model.ExportImportCategoryDTO;
import mc.gouv.xaf.back.data.es.model.ExportImportConfigDTO;
import mc.gouv.xaf.back.data.es.model.ExportImportConfigPropertyDTO;
import mc.gouv.xaf.back.exception.CategoryAlreadyExist;
import mc.gouv.xaf.back.exception.UsedCategoryException;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.RechercheAdminService;
import mc.gouv.xaf.back.service.es.RechercheDynamicJSService;
import mc.gouv.xaf.back.service.utils.HTMLEscapeUtils;

@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class RechercheAdminServiceImpl implements RechercheAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheAdminServiceImpl.class);

    @Autowired
    RechercheChampConfigRepository rechercheChampConfigRepository;

    @Autowired
    RechercheCatConfigRepository rechercheCatConfigRepository;

    @Autowired
    IndexedDemandeService indexedDemandeService;

    @Autowired
    RechercheDynamicJSService rechercheDynamicJSService;

    @Override
    public List<EsProperty> getPropertiesWithLabels() {

        List<EsProperty> properties = new ArrayList<>(indexedDemandeService.getProperties(false));

        Map<String, RechercheChampConfigBO> champsMap = getChampsMap();
        properties.removeIf(p -> p.getType().equals(EsProperty.BOOLEAN_TYPE)
                || p.getName().startsWith(DemandeEsDTO.JOIN_FIELD_NAME));
        Map<String, EsProperty> complementsFichiersPropertiesMap = addComplementsFilesAndInternalFilesProperties(
                properties);
        List<EsCategory> categories = getCategories();
        Collections.sort(categories);
        for (EsProperty property : properties) {
            RechercheChampConfigBO champBo = champsMap.get(property.getName());
            if (champBo != null) {
                String escapedLabel = HTMLEscapeUtils.escape(champBo.getLibelle());
                property.setLabel(escapedLabel);
                property.setCategoryId((champBo.getCategorie() != null) ? champBo.getCategorie().getId() : null);
                property.setEnabled(champBo.isEnabled());
                property.setEditable(champBo.isEditable());
                if (property.getName().startsWith(IndexedEsDemandeServiceImpl.FILE_PROPERTIES_PREFIX)) {
                    complementsFichiersPropertiesMap.get(
                            IndexedEsDemandeServiceImpl.FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + champBo.getCle())
                            .setEnabled(champBo.isEnabled());
                    complementsFichiersPropertiesMap.get(
                            IndexedEsDemandeServiceImpl.INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX + champBo.getCle())
                            .setEnabled(champBo.isEnabled());
                    complementsFichiersPropertiesMap.get(
                            IndexedEsDemandeServiceImpl.COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX + champBo.getCle())
                            .setEnabled(champBo.isEnabled());
                }
            } else {
                property.setEditable(true);
            }
            property.setAllCategories(categories);
        }

        return properties;
    }

    private Map<String, EsProperty> addComplementsFilesAndInternalFilesProperties(List<EsProperty> properties) {
        Map<String, EsProperty> complementsFilesAndInternalFilesPropertiesMap = new HashMap<>();
        List<EsProperty> complementsAndInternalFilesProperties = new ArrayList<>();
        for (EsProperty property : properties) {
            if (property.getName().startsWith(IndexedEsDemandeServiceImpl.FILE_PROPERTIES_PREFIX)) {
                EsProperty complementProperty = new EsProperty(
                        IndexedEsDemandeServiceImpl.FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                        property.getType(), property.getFields());
                EsProperty internalFileProperty = new EsProperty(
                        IndexedEsDemandeServiceImpl.INTERNAL_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                        property.getType(), property.getFields());
                EsProperty courrierProperty = new EsProperty(
                        IndexedEsDemandeServiceImpl.COURRIER_FILE_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                        property.getType(), property.getFields());

                complementsAndInternalFilesProperties.add(complementProperty);
                complementsAndInternalFilesProperties.add(internalFileProperty);
                complementsAndInternalFilesProperties.add(courrierProperty);
                complementsFilesAndInternalFilesPropertiesMap.put(complementProperty.getName(), complementProperty);
                complementsFilesAndInternalFilesPropertiesMap.put(internalFileProperty.getName(), internalFileProperty);
                complementsFilesAndInternalFilesPropertiesMap.put(courrierProperty.getName(), courrierProperty);
            }
        }

        if (!complementsAndInternalFilesProperties.isEmpty()) {
            properties.addAll(complementsAndInternalFilesProperties);
        }

        return complementsFilesAndInternalFilesPropertiesMap;

    }

    @Override
    public void updateProperties(ConfigPropertiesDTO properties) {
        LOGGER.info("Début de la maj des propriétés");
        if (properties != null && properties.getProperties() != null) {
            for (ConfigPropertyDTO property : properties.getProperties()) {
                updateProperty(property);
            }
            indexedDemandeService.loadProperties();
            rechercheDynamicJSService.createJsFile();
        }
        LOGGER.info("Fin de la maj des propriétés");
    }

    @Override
    public void updateProperty(ConfigPropertyDTO property) {

        LOGGER.info("Début de la maj de la propriété {}", property.getName());
        RechercheChampConfigBO champBo = rechercheChampConfigRepository.findByCle(property.getName());

        if (champBo == null) {
            LOGGER.info("La propriété n'existe pas");
            champBo = new RechercheChampConfigBO();
            LOGGER.info("Création de la propriété\nClé: {}", property.getName());
            champBo.setCle(property.getName());

            champBo.setEditable(true);
        }
        if (property.getCategoryId() != null) {

            Optional<RechercheCatConfigBO> catBoOp = rechercheCatConfigRepository.findById(property.getCategoryId());
            if (catBoOp.isPresent()) {
                champBo.setCategorie(catBoOp.get());
            }
        } else {
            LOGGER.info("La propriété n'est pas associée à une catégorie");
        }

        LOGGER.info("Enabled : {}", property.isEnabled());
        champBo.setEnabled(property.isEnabled());
        LOGGER.info("Libelle : {}", property.getLabel());
        champBo.setLibelle(property.getLabel());

        rechercheChampConfigRepository.save(champBo);

        LOGGER.info("Fin de la maj de la propriété {}", property.getName());
    }

    @Override
    public EsCategory addCategory(String label) {

        LOGGER.info("Début de l'ajout de la catégorie {}", label);
        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("Le libellé de la catégorie ne peut pas être vide");
        }

        RechercheCatConfigBO catBo = rechercheCatConfigRepository.findByLibelle(label);
        if (catBo != null) {
            throw new CategoryAlreadyExist("La catégorie " + label + " existe dejà");
        }

        RechercheCatConfigBO newCatBo = rechercheCatConfigRepository.save(new RechercheCatConfigBO(label, true));

        EsCategory category = new EsCategory(newCatBo.getId(), newCatBo.getLibelle(), true);
        LOGGER.info("Fin de l'ajout de la catégorie {}", label);
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
    public List<EsCategory> updateCategories(ConfigCategoriesDTO categories) {

        LOGGER.info("Début de la maj des catégories");
        if (categories != null && categories.getCategories() != null) {
            List<EsCategory> cats = new ArrayList<>();
            for (EsCategory cat : categories.getCategories()) {
                cats.add(updateCategory(cat));
            }
            rechercheDynamicJSService.createJsFile();
            LOGGER.info("Fin de la maj des catégories");
            return cats;
        }
        LOGGER.info("Fin de la maj des catégories");
        return new ArrayList<>();
    }

    @Override
    public EsCategory updateCategory(EsCategory category) {

        LOGGER.info("Début de la maj de la catégorie");
        if (category != null) {
            LOGGER.info("Catégorie: libelle : {}, isEditable: {}", category.getLabel(), category.isEditable());
            Optional<RechercheCatConfigBO> catBoOpt = rechercheCatConfigRepository.findById(category.getId());
            if (catBoOpt.isPresent() && catBoOpt.get().isEditable()) {
                RechercheCatConfigBO catBo = catBoOpt.get();
                catBo.setLibelle(category.getLabel());
                RechercheCatConfigBO updatedCat = rechercheCatConfigRepository.save(catBo);
                return new EsCategory(updatedCat.getId(), updatedCat.getLibelle(), updatedCat.isEditable());
            }
        }
        LOGGER.info("Fin de la maj de la catégorie");
        return null;
    }

    @Override
    public List<EsCategory> getCategories() {

        LOGGER.info("Début de la récupération des catégories");
        Iterable<RechercheCatConfigBO> categoriesBo = rechercheCatConfigRepository.findAll();
        List<EsCategory> categories = new ArrayList<>();

        if (categoriesBo != null) {
            for (RechercheCatConfigBO cat : categoriesBo) {
                String escapedLabel = HTMLEscapeUtils.escape(cat.getLibelle());
                categories.add(new EsCategory(cat.getId(), escapedLabel, cat.isEditable()));
            }
        }

        LOGGER.info("Fin de la récupération des catégories");
        return categories;
    }

    @Override
    public Map<String, RechercheChampConfigBO> getChampsMap() {

        Iterable<RechercheChampConfigBO> champs = rechercheChampConfigRepository.findAll();
        if (champs != null) {
            Map<String, RechercheChampConfigBO> champsMap = new HashMap<>();
            for (RechercheChampConfigBO champ : champs) {
                champsMap.put(champ.getCle(), champ);
            }

            return champsMap;
        }
        return new HashMap<>();
    }

    @Override
    public String exportConfig() throws JsonGenerationException, JsonMappingException, IOException {

        LOGGER.info("Début de l'export de la configuration");

        ExportImportConfigDTO exportConfig = new ExportImportConfigDTO();
        Iterable<RechercheCatConfigBO> categoriesBo = rechercheCatConfigRepository.findAll();

        if (categoriesBo != null) {

            for (RechercheCatConfigBO catConfig : categoriesBo) {
                exportConfig.getCategories()
                        .add(new ExportImportCategoryDTO(catConfig.getLibelle(), catConfig.isEditable()));
            }
        }

        Iterable<RechercheChampConfigBO> champsBo = rechercheChampConfigRepository.findAll();

        if (champsBo != null) {
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
        }

        ObjectMapper mapper = new ObjectMapper();

        String exportedConfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportConfig);

        LOGGER.info("Fin de l'export de la configuration, fichier exporté {}", exportedConfig);

        return exportedConfig;
    }

    @Override
    public void importConfig(byte[] file) throws IOException {

        LOGGER.info("Début de l'import de la configuration");

        ObjectMapper mapper = new ObjectMapper();
        ExportImportConfigDTO config = null;
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
                    categoriesMap.put(category.getLabel(), rechercheCatConfigRepository
                            .save(new RechercheCatConfigBO(category.getLabel(), category.isEditable())));
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

            indexedDemandeService.loadProperties();
            rechercheDynamicJSService.createJsFile();
        }

        LOGGER.info("Fin de l'import de la configuration");

    }

}
