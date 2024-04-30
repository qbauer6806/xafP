package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;

import java.util.List;

public interface BrouillonsService {

	BrouillonDTO saveBrouillon(BrouillonDTO brouillon);

	BrouillonDTO saveOrUpdateBrouillon(BrouillonDTO brouillon, Integer usagerId, boolean partialUpdate);

	List<BrouillonDTO> getBrouillons(String demarcheId, Integer usagerId);

	List<BrouillonDTO> getAllBrouillons(String demarcheId);

	BrouillonDTO getBrouillon(String demarcheId, Integer pkBrouillons, Integer usagerId);

	BrouillonBO getBrouillonBo(String demarcheId, Integer pkBrouillons);

	/**
	 * Permet de mettre à jour un brouillon
	 * @param brouillon Objet DTO pour mettre à jour le brouillon
	 * @param usagerId ID de l'usager faisant la mise à jour, à vérifier avec celui dans le brouillon
	 */
	BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId);

	/**
	 * Permet de supprimer un brouillon
	 * @param demarcheId ID de la démarche contenant le brouillon
	 * @param pkBrouillons PK du brouillon à supprimer
	 * @param usagerId ID de l'usager faisant la suppression, à vérifier avec celui dans le brouillon
	 */
	void deleteBrouillon(String demarcheId, Integer pkBrouillons, Integer usagerId);

	Page<BrouillonDTO> getBrouillonsPageable(String demarcheId, Integer usagerId, PageParamDTO paramDTO);

	void deleteBrouillons(String demarcheId, Integer usagerId);

	long getNombreBrouillons();

}
