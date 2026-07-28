package mc.gouv.xaf.back.data.transformer;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.entity.TacheBO;
import mc.gouv.xaf.shared.dto.TacheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mboutelier.ext
 */
public class TachesTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TachesTransformer.class);

    private TachesTransformer() {
    }

    public static TacheDTO bo2dto(TacheBO bo) {
        TacheDTO dto = new TacheDTO();
        dto.setPkTaches(bo.getPkTaches());
        dto.setFkDemande(bo.getDemande().getPkDemandes());
        dto.setStatutAgent(bo.getCodeStatutAgent());
        dto.setStatutValideur(bo.getCodeStatutValideur());
        dto.setCodeMotif(bo.getCodeMotif());
        dto.setCommentaire(bo.getCommentaire());
        dto.setCodeType(bo.getCodeType());
        dto.setLocked(bo.isLocked());

        // Mapper le contenu de la tache
        ObjectMapper mapper = new ObjectMapper();
        dto.setContenu(mapper.readTree(bo.getContenu()));

        return dto;
    }

    public static TacheBO dto2bo(TacheDTO dto) {
        TacheBO bo = new TacheBO();
        bo.setPkTaches(dto.getPkTaches());
        bo.setCodeStatutAgent(null != dto.getStatutAgent() ? dto.getStatutAgent().name() : null);
        bo.setCodeStatutValideur(null != dto.getStatutValideur() ? dto.getStatutValideur().name() : null);
        bo.setCodeMotif(dto.getCodeMotif());
        bo.setCommentaire(dto.getCommentaire());
        bo.setCodeType(dto.getCodeType());
        bo.setLocked(dto.isLocked());

        // Mapper le contenu de la tache
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setContenu(mapper.writeValueAsString(dto.getContenu()));
        } catch (JacksonException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }

        return bo;
    }

    public static List<TacheDTO> bos2Dtos(List<TacheBO> bos) {
        List<TacheDTO> dtos = new ArrayList<>();
        for (TacheBO bo : bos) {
            dtos.add(bo2dto(bo));
        }
        return dtos;
    }

    public static List<TacheBO> dtos2Bos(List<TacheDTO> dtos) {
        List<TacheBO> bos = new ArrayList<>();
        for (TacheDTO dto : dtos) {
            bos.add(dto2bo(dto));
        }
        return bos;
    }

}
