package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.shared.dto.AccessDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * @author qdeme
 */
public class AccessTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTransformer.class);

    private AccessTransformer() {
    }

    public static AccessDTO bo2Dto(AccessBO bo) {
        if (bo == null) {
            return null;
        }
        AccessDTO dto = new AccessDTO();
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerModif(bo.getDateDerModif());
        dto.setPkAccess(bo.getPkAccess());
        ObjectMapper mapper = new ObjectMapper();
        dto.setContenu(mapper.readTree(bo.getContenu()));
        dto.setUsagerId(bo.getUsagerId());
        return dto;
    }

    public static AccessBO dto2Bo(AccessDTO dto) {
        if (dto == null) {
            return null;
        }
        AccessBO bo = new AccessBO();
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerModif(dto.getDateDerModif());
        bo.setPkAccess(dto.getPkAccess());
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setContenu(mapper.writeValueAsString(dto.getContenu()));
        } catch (JacksonException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        bo.setUsagerId(dto.getUsagerId());
        return bo;
    }

}
