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

	BrouillonDTO saveOrUpdateBrouillon(BrouillonDTO brouillon, boolean partialUpdate) throws Exception;

	List<BrouillonDTO> getBrouillons(String demarcheId, Integer usagerId);

	BrouillonDTO getBrouillon(String demarcheId, Integer pkBrouillons);

	BrouillonBO getBrouillonBo(String demarcheId, Integer pkBrouillons);

	BrouillonDTO updateBrouillon(BrouillonDTO brouillon) throws IOException, SAXException;

	void deleteBrouillon(String demarcheId, Integer pkBrouillons) throws JsonProcessingException;

	Page<BrouillonDTO> getBrouillonsPageable(String demarcheId, Integer usagerId, PageParamDTO paramDTO);

}
