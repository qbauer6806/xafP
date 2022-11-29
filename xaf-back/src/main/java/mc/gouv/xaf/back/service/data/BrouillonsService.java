package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;

public interface BrouillonsService {

	BrouillonDTO saveBrouillon(BrouillonDTO brouillon);

	BrouillonDTO saveOrUpdateBrouillon(BrouillonDTO brouillon, boolean partialUpdate);

	List<BrouillonDTO> getBrouillons(String demarcheId, Integer usagerId);

	BrouillonDTO getBrouillon(String demarcheId, Integer pkBrouillons);

	BrouillonBO getBrouillonBo(String demarcheId, Integer pkBrouillons);

	BrouillonDTO updateBrouillon(BrouillonDTO brouillon);

	void deleteBrouillon(String demarcheId, Integer pkBrouillons);

	Page<BrouillonDTO> getBrouillonsPageable(String demarcheId, Integer usagerId, PageParamDTO paramDTO);

	void deleteBrouillons(String demarcheId, Integer usagerId);

	long getNombreBrouillons();

}
