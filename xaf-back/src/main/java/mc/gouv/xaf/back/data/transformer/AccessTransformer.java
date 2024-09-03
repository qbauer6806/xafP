package mc.gouv.xaf.back.data.transformer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.shared.dto.AccessDTO;

/**
 * 
 * @author qdeme
 *
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
        try {
            dto.setContenu(mapper.readTree(bo.getContenu()));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
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
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        bo.setUsagerId(dto.getUsagerId());
        return bo;
    }
    
}
