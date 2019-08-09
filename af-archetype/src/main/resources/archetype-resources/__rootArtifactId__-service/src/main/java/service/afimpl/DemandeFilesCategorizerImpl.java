#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.afimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.dto.FileCategoryDTO;
import mc.gouv.af.back.service.DemandeFilesCategorizer;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeComplementsFileDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeFileDTO;

/**
 * Service permettant de lister les catégories de fichiers ainsi que les
 * fichiers associés
 * 
 * @author qdeme
 *
 */
@Component
public class DemandeFilesCategorizerImpl implements DemandeFilesCategorizer {
	
	@Autowired
	private AfBackUtils afBackUtils;

	@Override
	public List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande) {

		List<FileCategoryDTO> categories = new ArrayList<FileCategoryDTO>();

		FileCategoryDTO catDemandeInitiale = new FileCategoryDTO();
		catDemandeInitiale.setName("Fichiers de la demande initiale");
		List<DemandeFileDTO> files = new ArrayList<DemandeFileDTO>();
		if (demande.getFichiers() != null) {
			for (DemandeFileDTO file : demande.getFichiers()) {
				if (afBackUtils.isFileCreatedByFront(file.getMeta())) {
					files.add(file);
				}
			}
		}
		catDemandeInitiale.setFiles(files);
		categories.add(catDemandeInitiale);

		FileCategoryDTO catComplements = new FileCategoryDTO();
		catComplements.setName("Fichiers complémentaires");
		files = new ArrayList<DemandeFileDTO>();
		if (demande.getComplements() != null) {
			for (DemandeComplementsDTO compl : demande.getComplements()) {
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
		catComplements.setFiles(files);
		categories.add(catComplements);

		FileCategoryDTO catFichiersInternes = new FileCategoryDTO();
		catFichiersInternes.setName("Fichiers internes");
		files = new ArrayList<DemandeFileDTO>();
		if (demande.getFichiers() != null) {
			for (DemandeFileDTO file : demande.getFichiers()) {
				if (afBackUtils.isFileCreatedByBack(file.getMeta())) {
					files.add(file);
				}
			}
		}
		catFichiersInternes.setFiles(files);
		categories.add(catFichiersInternes);

		return categories;
	}

}
