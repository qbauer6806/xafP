package mc.gouv.xaf.back.service;

import java.util.Comparator;
import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import mc.gouv.xaf.shared.dto.FileSubCategoryDTO;

/**
 * Service permettant de lister les catégories de fichiers ainsi que les fichiers associés
 *
 * @author qdeme
 */
public interface DemandeFilesCategorizer {

    List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande);

    /**
     * Permets de construire la liste des catégories et leurs fichiers en utilisant le comparateur donné en entrée
     * pour trier les sub catégories
     *
     * @param demande    la demande qui les contient les fichiers
     * @param comparator pour trier les sub catégories
     * @return la liste des catégories construites
     */
    List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande, Comparator<FileSubCategoryDTO> comparator);

    List<DemandeFileDTO> fichiersAdministration(DemandeFileDTO[] demandeFiles);

    List<DemandeFileDTO> fichiersFront(DemandeFileDTO[] demandeFiles);

}
