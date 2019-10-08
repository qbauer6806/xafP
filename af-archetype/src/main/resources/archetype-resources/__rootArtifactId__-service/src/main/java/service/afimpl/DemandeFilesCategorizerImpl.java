#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import mc.gouv.af.back.dto.FileCategoryDTO;
import mc.gouv.af.back.service.DemandeFilesCategorizer;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeComplementsFileDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeFileDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Service permettant de lister les catégories de fichiers ainsi que les
 * fichiers associés
 * 
 * @author mpavone
 *
 */
@Component
public class DemandeFilesCategorizerImpl implements DemandeFilesCategorizer {

	private static final String JUSTIFICATIF_DEMANDE = "JUSTIFICATIF_DEMANDE";

	@Override
	public List<FileCategoryDTO> getCategoriesAndFiles(DemandeDTO demande) {

		List<FileCategoryDTO> categories = new ArrayList<FileCategoryDTO>();

		FileCategoryDTO catDemandeInitiale = new FileCategoryDTO();
		catDemandeInitiale.setName("Fichiers de la demande initiale");
		List<DemandeFileDTO> files = new ArrayList<DemandeFileDTO>();
		if (demande.getFichiers() != null) {
			for (DemandeFileDTO file : demande.getFichiers()) {
				if (StringUtils.isBlank(file.getMeta()) || file.getMeta().startsWith("FRONT_")) {
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

		FileCategoryDTO catFichiersRemisAdministration = new FileCategoryDTO();
		catFichiersRemisAdministration.setName("Fichiers remis par l'Administration");
		files = new ArrayList<DemandeFileDTO>();
		if (demande.getFichiers() != null) {
			for (DemandeFileDTO file : demande.getFichiers()) {
				if (!StringUtils.isBlank(file.getMeta()) && file.getMeta().contains(JUSTIFICATIF_DEMANDE)) {
					files.add(file);
				}
			}
		}
		catFichiersRemisAdministration.setFiles(files);
		categories.add(catFichiersRemisAdministration);

		FileCategoryDTO catFichiersInternes = new FileCategoryDTO();
		catFichiersInternes.setName("Fichiers internes");
		files = new ArrayList<DemandeFileDTO>();
		if (demande.getFichiers() != null) {
			for (DemandeFileDTO file : demande.getFichiers()) {
				if (!StringUtils.isBlank(file.getMeta()) && !file.getMeta().startsWith("FRONT_") &&
						!file.getMeta().contains(JUSTIFICATIF_DEMANDE)) {
					files.add(file);
				}
			}
		}
		catFichiersInternes.setFiles(files);
		categories.add(catFichiersInternes);

		return categories;
	}

}
