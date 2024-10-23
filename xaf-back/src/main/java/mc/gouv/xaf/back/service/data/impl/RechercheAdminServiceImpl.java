package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import mc.gouv.xaf.back.service.data.RechercheChampService;
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

    @Autowired
    private RechercheChampService rechercheChampService;

    @Override
    public List<RechercheChampDTO> getRechercheChamps() {

        List<RechercheChampDTO> properties = rechercheChampService.getRechercheChamps();

        // Récupération des champs associés aux propriétés dans la BDD
        Map<String, RechercheChampConfigBO> champsMap = getChampsMap();

        // On récupère les catégories pour classer les propriétés
        List<RechercheCategoryDTO> categories = getCategories();
        Collections.sort(categories);

        // On ajoute les valeurs issues de la BDD dans les propriétés
        for (RechercheChampDTO property : properties) {
            RechercheChampConfigBO champBo = champsMap.get(property.getName());
            if (champBo != null) {
                String escapedLabel = HTMLEscapeUtils.escape(champBo.getLibelle());
                property.setLabel(escapedLabel);
                property.setCategoryId((champBo.getCategorie() != null) ? champBo.getCategorie().getId() : null);
                property.setEnabled(champBo.isEnabled());
                property.setEditable(champBo.isEditable());
            } else {
                property.setEditable(true);
            }
            property.setAllCategories(categories);
        }

        return properties;
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

    @Override
    public void updateRechercheChamp(RechercheChampDTO rechercheChampDTO) {
        String safeChamp = AfBackUtils.logSafe(rechercheChampDTO.getName());
        LOGGER.info("Début de la maj de la propriété {}", safeChamp);
        RechercheChampConfigBO champBo = rechercheChampConfigRepository.findByCle(rechercheChampDTO.getName());

        // Vérification de l'existance de la propriété
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

    @Override
    public RechercheCategoryDTO updateCategory(RechercheCategoryDTO category) {

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
        LOGGER.info("Début de la récupération des catégories");
        Iterable<RechercheCatConfigBO> categoriesBo = rechercheCatConfigRepository.findAll();
        List<RechercheCategoryDTO> categories = new ArrayList<>();
        for (RechercheCatConfigBO cat : categoriesBo) {
            String escapedLabel = HTMLEscapeUtils.escape(cat.getLibelle());
            categories.add(new RechercheCategoryDTO(cat.getId(), escapedLabel, cat.isEditable()));
        }
        LOGGER.info("Fin de la récupération des catégories");
        return categories;
    }

    @Override
    public Map<String, RechercheChampConfigBO> getChampsMap() {
        Iterable<RechercheChampConfigBO> champs = rechercheChampConfigRepository.findAll();
        Map<String, RechercheChampConfigBO> champsMap = new HashMap<>();
        for (RechercheChampConfigBO champ : champs) {
            champsMap.put(champ.getCle(), champ);
        }
        return champsMap;
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

}
