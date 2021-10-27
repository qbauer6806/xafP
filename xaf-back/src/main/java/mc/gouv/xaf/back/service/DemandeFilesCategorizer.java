package mc.gouv.xaf.back.service;

import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;

/**
 * Service permettant de lister les catégories de fichiers ainsi que les fichiers associés
 * 
 * @author qdeme
 *
 */
public interface DemandeFilesCategorizer {

	List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande);

}
