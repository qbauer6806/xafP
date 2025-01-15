package mc.gouv.xaf.xaf12batch.marqueurs;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author uek
 */
@Service
public class MarqueursTransformer {

    private MarqueursTransformer() {
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

    public List<MarqueurBO> dtos2Bos(List<MarqueurDTO> dtos) {
        ArrayList<MarqueurBO> bos = new ArrayList<>();
        for (MarqueurDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
}
