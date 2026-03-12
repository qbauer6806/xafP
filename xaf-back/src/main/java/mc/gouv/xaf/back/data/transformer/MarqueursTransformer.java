package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import org.springframework.stereotype.Service;

/**
 * @author uek
 */
@Service
public class MarqueursTransformer {

    private MarqueursTransformer() {
    }

    public MarqueurDTO bo2Dto(MarqueurBO bo) {
        if (bo == null) {
            return null;
        }
        MarqueurDTO dto = new MarqueurDTO();
        dto.setPkMarqueur(bo.getPkMarqueur());
        dto.setDescription(bo.getDescription());
        dto.setIdentifiant(bo.getIdentifiant());
        dto.setChemin(bo.getChemin());
        dto.setBuildId(bo.getBuildId());
        dto.setType(bo.getType());
        dto.setOptions(bo.getOptions());
        return dto;
    }

    public MarqueurBO dto2Bo(MarqueurDTO dto) {
        if (dto == null) {
            return null;
        }
        MarqueurBO bo = new MarqueurBO();
        bo.setPkMarqueur(dto.getPkMarqueur());
        bo.setDescription(dto.getDescription());
        bo.setIdentifiant(dto.getIdentifiant());
        bo.setChemin(dto.getChemin());
        bo.setBuildId(dto.getBuildId());
        bo.setType(dto.getType());
        bo.setOptions(dto.getOptions());
        return bo;
    }

    public List<MarqueurDTO> bos2Dtos(List<MarqueurBO> bos) {
        ArrayList<MarqueurDTO> dtos = new ArrayList<>();
        for (MarqueurBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public List<MarqueurDTO> bos2Dtos(Set<MarqueurBO> bos) {
        ArrayList<MarqueurDTO> dtos = new ArrayList<>();
        for (MarqueurBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public List<MarqueurBO> dtos2Bos(List<MarqueurDTO> dtos) {
        ArrayList<MarqueurBO> bos = new ArrayList<>();
        for (MarqueurDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
}
