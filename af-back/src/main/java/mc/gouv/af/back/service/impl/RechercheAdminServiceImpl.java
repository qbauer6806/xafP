package mc.gouv.af.back.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.data.es.model.ConfigCategoriesDTO;
import mc.gouv.af.back.data.es.model.ConfigPropertiesDTO;
import mc.gouv.af.back.data.es.model.ConfigPropertyDTO;
import mc.gouv.af.back.data.es.model.DemandeEsDTO;
import mc.gouv.af.back.data.es.model.EsCategory;
import mc.gouv.af.back.data.es.model.EsProperty;
import mc.gouv.af.back.data.es.model.ExportImportCategoryDTO;
import mc.gouv.af.back.data.es.model.ExportImportConfigDTO;
import mc.gouv.af.back.data.es.model.ExportImportConfigPropertyDTO;
import mc.gouv.af.back.exception.CategoryAlreadyExist;
import mc.gouv.af.back.exception.UsedCategoryException;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.af.back.service.RechercheAdminService;
import mc.gouv.af.back.service.RechercheDynamicJSService;
import mc.gouv.af.data.dao.RechercheCatConfigRepository;
import mc.gouv.af.data.dao.RechercheChampConfigRepository;
import mc.gouv.af.data.entity.RechercheCatConfigBo;
import mc.gouv.af.data.entity.RechercheChampConfigBo;

@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class RechercheAdminServiceImpl implements RechercheAdminService {

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

        Map<String, RechercheChampConfigBo> champsMap = getChampsMap();
        properties.removeIf(p -> p.getType().equals(EsProperty.BOOLEAN_TYPE)
                || p.getName().startsWith(DemandeEsDTO.JOIN_FIELD_NAME));
        Map<String, EsProperty> complementsFichiersPropertiesMap = addComplementsFichiersDemandeProperties(properties);
        List<EsCategory> categories = getCategories();
        Collections.sort(categories);
        for (EsProperty property : properties) {
            RechercheChampConfigBo champBo = champsMap.get(property.getName());
            if (champBo != null) {
                property.setLabel(champBo.getLibelle());
                property.setCategoryId((champBo.getCategorie() != null) ? champBo.getCategorie().getId() : null);
                property.setEnabled(champBo.isEnabled());
                property.setEditable(champBo.isEditable());
                if (property.getName().startsWith(IndexedEsDemandeServiceImpl.FILE_PROPERTIES_PREFIX)) {
                    complementsFichiersPropertiesMap.get(
                            IndexedEsDemandeServiceImpl.FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + champBo.getCle())
                            .setEnabled(champBo.isEnabled());
                }
            } else {
                property.setEditable(true);
            }
            property.setAllCategories(categories);
        }

        return properties;
    }

    private Map<String, EsProperty> addComplementsFichiersDemandeProperties(List<EsProperty> properties) {
        Map<String, EsProperty> complementsFichiersPropertiesMap = new HashMap<>();
        List<EsProperty> complementsProperties = new ArrayList<>();
        for (EsProperty property : properties) {
            if (property.getName().startsWith(IndexedEsDemandeServiceImpl.FILE_PROPERTIES_PREFIX)) {
                EsProperty complementProperty = new EsProperty(
                        IndexedEsDemandeServiceImpl.FILE_COMPLEMENT_HIGHLIGHT_AND_FACET_PREFIX + property.getName(),
                        property.getType(), property.getFields());
                complementsProperties.add(complementProperty);
                complementsFichiersPropertiesMap.put(complementProperty.getName(), complementProperty);
            }
        }

        if (!complementsProperties.isEmpty()) {
            properties.addAll(complementsProperties);
        }

        return complementsFichiersPropertiesMap;

    }

    @Override
    public void updateProperties(ConfigPropertiesDTO properties) {
        if (properties != null && properties.getProperties() != null) {
            for (ConfigPropertyDTO property : properties.getProperties()) {
                updateProperty(property);
            }
            indexedDemandeService.loadPropertiesToExclude();
            rechercheDynamicJSService.createJsFile();
        }
    }

    @Override
    public void updateProperty(ConfigPropertyDTO property) {
        RechercheChampConfigBo champBo = rechercheChampConfigRepository.findByCle(property.getName());

        if (champBo == null) {
            champBo = new RechercheChampConfigBo();
            champBo.setCle(property.getName());
            champBo.setEditable(true);
        }
        if (property.getCategoryId() != null) {
            Optional<RechercheCatConfigBo> catBoOp = rechercheCatConfigRepository.findById(property.getCategoryId());
            if (catBoOp.isPresent()) {
                champBo.setCategorie(catBoOp.get());
            }
        }

        champBo.setEnabled(property.isEnabled());
        champBo.setLibelle(property.getLabel());

        rechercheChampConfigRepository.save(champBo);
    }

    @Override
    public EsCategory addCategory(String label) {

        if (StringUtils.isBlank(label)) {
            throw new IllegalArgumentException("Le libellé de la catégorie ne peut pas être vide");
        }

        RechercheCatConfigBo catBo = rechercheCatConfigRepository.findByLibelle(label);
        if (catBo != null) {
            throw new CategoryAlreadyExist("La catégorie " + label + " existe dejà");
        }

        RechercheCatConfigBo newCatBo = rechercheCatConfigRepository.save(new RechercheCatConfigBo(label, true));

        return new EsCategory(newCatBo.getId(), newCatBo.getLibelle(), true);
    }

    @Override
    public void deleteCategory(Integer id) {

        List<RechercheChampConfigBo> properties = rechercheChampConfigRepository.findByCategorieId(id);
        if (properties != null && !properties.isEmpty()) {
            throw new UsedCategoryException("La catégorie est dejà utilisée");
        }

        Optional<RechercheCatConfigBo> categoryOpt = rechercheCatConfigRepository.findById(id);
        if (categoryOpt.isPresent() && categoryOpt.get().isEditable()) {
            rechercheCatConfigRepository.deleteById(id);
        }
    }

    @Override
    public List<EsCategory> updateCategories(ConfigCategoriesDTO categories) {

        if (categories != null && categories.getCategories() != null) {
            List<EsCategory> cats = new ArrayList<>();
            for (EsCategory cat : categories.getCategories()) {
                cats.add(updateCategory(cat));
            }
            rechercheDynamicJSService.createJsFile();
            return cats;
        }
        return new ArrayList<>();
    }

    @Override
    public EsCategory updateCategory(EsCategory category) {

        if (category != null) {
            Optional<RechercheCatConfigBo> catBoOpt = rechercheCatConfigRepository.findById(category.getId());
            if (catBoOpt.isPresent() && catBoOpt.get().isEditable()) {
                RechercheCatConfigBo catBo = catBoOpt.get();
                catBo.setLibelle(category.getLabel());
                RechercheCatConfigBo updatedCat = rechercheCatConfigRepository.save(catBo);
                return new EsCategory(updatedCat.getId(), updatedCat.getLibelle(), updatedCat.isEditable());
            }
        }
        return null;
    }

    @Override
    public List<EsCategory> getCategories() {

        Iterable<RechercheCatConfigBo> categoriesBo = rechercheCatConfigRepository.findAll();
        List<EsCategory> categories = new ArrayList<>();

        if (categoriesBo != null) {
            for (RechercheCatConfigBo cat : categoriesBo) {
                categories.add(new EsCategory(cat.getId(), cat.getLibelle(), cat.isEditable()));
            }
        }

        return categories;
    }

    @Override
    public Map<String, RechercheChampConfigBo> getChampsMap() {

        Iterable<RechercheChampConfigBo> champs = rechercheChampConfigRepository.findAll();
        if (champs != null) {
            Map<String, RechercheChampConfigBo> champsMap = new HashMap<>();
            for (RechercheChampConfigBo champ : champs) {
                champsMap.put(champ.getCle(), champ);
            }

            return champsMap;
        }
        return new HashMap<>();
    }

    @Override
    public String exportConfig() throws JsonGenerationException, JsonMappingException, IOException {

        ExportImportConfigDTO exportConfig = new ExportImportConfigDTO();
        Iterable<RechercheCatConfigBo> categoriesBo = rechercheCatConfigRepository.findAll();

        if (categoriesBo != null) {
            for (RechercheCatConfigBo catConfig : categoriesBo) {
                exportConfig.getCategories()
                        .add(new ExportImportCategoryDTO(catConfig.getLibelle(), catConfig.isEditable()));
            }
        }

        Iterable<RechercheChampConfigBo> champsBo = rechercheChampConfigRepository.findAll();

        if (champsBo != null) {
            for (RechercheChampConfigBo configConfig : champsBo) {

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

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportConfig);
    }

    @Override
    public void importConfig(byte[] file) throws IOException {

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

            Map<String, RechercheCatConfigBo> categoriesMap = new HashMap<>();
            List<ExportImportCategoryDTO> categories = config.getCategories();
            if (categories != null) {
                for (ExportImportCategoryDTO category : categories) {
                    categoriesMap.put(category.getLabel(), rechercheCatConfigRepository
                            .save(new RechercheCatConfigBo(category.getLabel(), category.isEditable())));
                }
            }

            List<ExportImportConfigPropertyDTO> properties = config.getProperties();
            if (properties != null) {
                for (ExportImportConfigPropertyDTO property : properties) {

                    RechercheChampConfigBo champConfig = new RechercheChampConfigBo();
                    champConfig.setCle(property.getName());
                    champConfig.setCategorie(categoriesMap.get(property.getCategoryName()));
                    champConfig.setEditable(property.isEditable());
                    champConfig.setLibelle(property.getLabel());
                    champConfig.setEnabled(property.isEnabled());

                    rechercheChampConfigRepository.save(champConfig);
                }
            }
        }

    }

}
