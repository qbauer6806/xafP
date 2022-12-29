package mc.gouv.xaf.back.service.data;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;

public interface BrouillonsService {

	BrouillonDTO saveBrouillon(BrouillonDTO brouillon) throws Exception;

	BrouillonDTO saveOrUpdateBrouillon(BrouillonDTO brouillon, Integer usagerId, boolean partialUpdate) throws Exception;

	List<BrouillonDTO> getBrouillons(String demarcheId, Integer usagerId);

	BrouillonDTO getBrouillon(String demarcheId, Integer pkBrouillons, Integer usagerId);

	BrouillonBO getBrouillonBo(String demarcheId, Integer pkBrouillons);

	/**
	 * Permet de mettre à jour un brouillon
	 * @param brouillon Objet DTO pour mettre à jour le brouillon
	 * @param usagerId ID de l'usager faisant la mise à jour, à vérifier avec celui dans le brouillon
	 */
	BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId) throws IOException, SAXException;

	/**
	 * Permet de supprimer un brouillon
	 * @param demarcheId ID de la démarche contenant le brouillon
	 * @param pkBrouillons PK du brouillon à supprimer
	 * @param usagerId ID de l'usager faisant la suppression, à vérifier avec celui dans le brouillon
	 */
	void deleteBrouillon(String demarcheId, Integer pkBrouillons, Integer usagerId);

	Page<BrouillonDTO> getBrouillonsPageable(String demarcheId, Integer usagerId, PageParamDTO paramDTO);

	void deleteBrouillons(String demarcheId, Integer usagerId);

}
