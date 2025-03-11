package mc.gouv.xaf.back.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import mc.gouv.xaf.shared.dto.FileSubCategoryDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implémentation du DemandeFilesCategorizer
 *
 * @author mboutelier.ext
 */
@Component
public class DemandeFilesCategorizerImpl implements DemandeFilesCategorizer {

    private static final String JUSTIFICATIF_DEMANDE = "JUSTIFICATIF_DEMANDE";
    private static final String XAF_SECTIONS_FICHIERS_DEMANDE = "XAF_SECTIONS_FICHIERS_DEMANDE";

    @Autowired
    private PropertiesService propertiesService;

    /**
     * @param demande
     * @return
     */
    @Override
    public List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande) {
        return getCategoriesAndFiles(demande, new FileSubCategoryComparator());
    }

    /**
     * {@inheritDoc}
     *
     * @param demande
     * @param comparator
     * @return
     */
    @Override
    public List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande, @NotNull Comparator<FileSubCategoryDTO> comparator) {
        DemandeFileDTO[] demandeFiles = demande.getFichiers();
        List<FileCategoryDTO> categories = new ArrayList<>();

        FileCategoryDTO catDemandeInitiale = new FileCategoryDTO();
        catDemandeInitiale.setName(FileUtils.CAT_INITIALE);
        catDemandeInitiale.setTypedoc(true);
        // Sous-catégories
        PropertiesDTO prop = propertiesService.getProperty(XAF_SECTIONS_FICHIERS_DEMANDE);
        String propValue = prop != null ? prop.getValue() : "";
        Map<String, String> mapClesSections = AfBackUtils.getListFromDemProperty(propValue);
        Map<String, FileSubCategoryDTO> listeSections = new HashMap<>();

        if (demandeFiles != null) {
            for (DemandeFileDTO file : demande.getFichiers()) {
                if (file.getMeta() == null || file.getMeta().startsWith(FileUtils.META_FRONT)) {
                    String idSection = AfBackUtils.getSectionFromMetaFichier(file.getMeta());
                    listeSections.computeIfAbsent(idSection, key -> this.creerSubCategory(key, mapClesSections))
                            .getFiles().add(file);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(listeSections.values())) {
            List<FileSubCategoryDTO> subCats = new ArrayList<>(listeSections.values());
            subCats.sort(comparator);

            catDemandeInitiale.setSubCategories(subCats);
        }
        catDemandeInitiale.setFiles(new ArrayList<>());
        categories.add(catDemandeInitiale);

        FileCategoryDTO catComplements = new FileCategoryDTO();
        catComplements.setName(FileUtils.CAT_COMPLEMENTS);
        catComplements.setTypedoc(true);
        catComplements.setFiles(fichiersComplementaires(demande.getComplements()));
        categories.add(catComplements);

        FileCategoryDTO catFichiersRemisAdministration = new FileCategoryDTO();
        catFichiersRemisAdministration.setName(FileUtils.CAT_ADMINISTRATION);
        catFichiersRemisAdministration.setFiles(fichiersAdministration(demandeFiles));
        categories.add(catFichiersRemisAdministration);

        FileCategoryDTO catFichiersInternes = new FileCategoryDTO();
        catFichiersInternes.setName(FileUtils.CAT_INTERNES);
        catFichiersInternes.setFiles(fichiersInternes(demandeFiles));
        catFichiersInternes.setTypedoc(false);
        categories.add(catFichiersInternes);

        return categories;
    }

    private List<DemandeFileDTO> fichiersComplementaires(DemandeComplementsDTO[] complements) {
        List<DemandeFileDTO> files = new ArrayList<>();
        if (complements != null) {
            for (DemandeComplementsDTO compl : complements) {
                if (compl.getReponse() != null) {
                    List<DemandeComplementsFileDTO> filesList = Arrays.asList(compl.getReponse().getFichiers());
                    files.addAll(DemandesComplementsFilesTransformer.toDemandeFileDTOCategorizer(filesList,
                            compl.getReponse().getDate()));
                }
            }
        }
        return files;
    }

    @Override
    public List<DemandeFileDTO> fichiersAdministration(DemandeFileDTO[] demandeFiles) {
        List<DemandeFileDTO> files = new ArrayList<>();
        if (demandeFiles != null) {
            for (DemandeFileDTO file : demandeFiles) {
                if (!StringUtils.isBlank(file.getMeta()) && file.getMeta().contains(JUSTIFICATIF_DEMANDE)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    private List<DemandeFileDTO> fichiersInternes(DemandeFileDTO[] demandeFiles) {
        List<DemandeFileDTO> files = new ArrayList<>();
        if (demandeFiles != null) {
            for (DemandeFileDTO file : demandeFiles) {
                if (!StringUtils.isBlank(file.getMeta()) && !file.getMeta().startsWith(FileUtils.META_FRONT)
                        && !file.getMeta().contains(JUSTIFICATIF_DEMANDE)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public static class FileSubCategoryComparator implements Comparator<FileSubCategoryDTO> {

        @Override
        public int compare(FileSubCategoryDTO c1, FileSubCategoryDTO c2) {
            return c1.getKey().compareTo(c2.getKey());
        }

    }

    private FileSubCategoryDTO creerSubCategory(String idSection, Map<String, String> mapClesSections) {
        FileSubCategoryDTO subCat = new FileSubCategoryDTO();
        subCat.setKey(idSection);
        subCat.setName(mapClesSections.getOrDefault(idSection, "Documents"));
        subCat.setTypedoc(true);
        subCat.setFiles(new ArrayList<>());
        return subCat;
    }
}
