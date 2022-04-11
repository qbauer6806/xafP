package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation abstraite du DemandeFilesCategorizer
 *
 * @author mboutelier.ext
 */
public abstract class AbstractDemandeFilesCategorizerImpl implements DemandeFilesCategorizer {

    private static final String JUSTIFICATIF_DEMANDE = "JUSTIFICATIF_DEMANDE";

    private List<DemandeFileDTO> fichiersDemandeInitiale(DemandeFileDTO[] demandeFiles) {
        List<DemandeFileDTO> files = new ArrayList<>();
        if (demandeFiles != null) {
            for (DemandeFileDTO file : demandeFiles) {
                if (StringUtils.isBlank(file.getMeta()) || file.getMeta().startsWith("FRONT_")) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    private List<DemandeFileDTO> fichiersComplementaires(DemandeComplementsDTO[] complements) {
        List<DemandeFileDTO> files = new ArrayList<>();
        if (complements != null) {
            for (DemandeComplementsDTO compl : complements) {
                if (compl.getReponse() != null) {
                    for (DemandeComplementsFileDTO complFile : compl.getReponse().getFichiers()) {
                        DemandeFileDTO file = new DemandeFileDTO();
                        file.setMeta(complFile.getMeta());
                        file.setName(complFile.getName());
                        file.setUrl(complFile.getUrl());
                        file.setDate(compl.getReponse().getDate());
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    private List<DemandeFileDTO> fichiersAdministration(DemandeFileDTO[] demandeFiles) {
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
                if (!StringUtils.isBlank(file.getMeta()) && !file.getMeta().startsWith("FRONT_") &&
                        !file.getMeta().contains(JUSTIFICATIF_DEMANDE)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    @Override
    public List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande) {
        DemandeFileDTO[] demandeFiles = demande.getFichiers();
        List<FileCategoryDTO> categories = new ArrayList<>();

        FileCategoryDTO catDemandeInitiale = new FileCategoryDTO();
        catDemandeInitiale.setName("Fichiers de la demande initiale");
        catDemandeInitiale.setFiles(fichiersDemandeInitiale(demandeFiles));
        categories.add(catDemandeInitiale);

        FileCategoryDTO catComplements = new FileCategoryDTO();
        catComplements.setName("Fichiers complémentaires");
        catComplements.setFiles(fichiersComplementaires(demande.getComplements()));
        categories.add(catComplements);

        FileCategoryDTO catFichiersRemisAdministration = new FileCategoryDTO();
        catFichiersRemisAdministration.setName("Fichiers remis par l'Administration");
        catFichiersRemisAdministration.setFiles(fichiersAdministration(demandeFiles));
        categories.add(catFichiersRemisAdministration);

        FileCategoryDTO catFichiersInternes = new FileCategoryDTO();
        catFichiersInternes.setName("Fichiers internes");
        catFichiersInternes.setFiles(fichiersInternes(demandeFiles));
        categories.add(catFichiersInternes);

        return categories;
    }
}
