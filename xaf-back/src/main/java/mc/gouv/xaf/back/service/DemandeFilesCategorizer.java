package mc.gouv.xaf.back.service;

import java.util.List;

import mc.gouv.xaf.back.dto.FileCategoryDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * Service permettant de lister les catégories de fichiers ainsi que les fichiers associés
 * 
 * @author qdeme
 *
 */
public interface DemandeFilesCategorizer {

	public List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande);
	
}
