package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.KafkaOutboxBO;
import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * 
 * @author qdeme
 *
 */
public class KafkaOutboxTransformer {

    private KafkaOutboxTransformer() {
    }
    
    public static KafkaOutboxDTO bo2Dto(KafkaOutboxBO bo) {
    	KafkaOutboxDTO dto = new KafkaOutboxDTO();
        dto.setContenu(bo.getContenu());
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateLastAttempt(bo.getDateLastAttempt());
        dto.setKey(bo.getKey());
        dto.setNbFailedAttempts(bo.getNbFailedAttempts());
        dto.setPkKafkaOutbox(bo.getPkKafkaOutbox());
        dto.setStatut(bo.getStatut());
        dto.setTopic(bo.getTopic());
        return dto;
    }
    
    public static KafkaOutboxBO dto2Bo(KafkaOutboxDTO dto) {
    	KafkaOutboxBO bo = new KafkaOutboxBO();
    	bo.setContenu(dto.getContenu());
    	bo.setDateCreation(dto.getDateCreation());
    	bo.setDateLastAttempt(dto.getDateLastAttempt());
    	bo.setKey(dto.getKey());
    	bo.setNbFailedAttempts(dto.getNbFailedAttempts());
    	bo.setPkKafkaOutbox(dto.getPkKafkaOutbox());
    	bo.setStatut(dto.getStatut());
    	bo.setTopic(dto.getTopic());
        return bo;
    }
    
    public static List<KafkaOutboxDTO> bo2Dto(List<KafkaOutboxBO> bos) {
        ArrayList<KafkaOutboxDTO> dtos = new ArrayList<KafkaOutboxDTO>();
        for (KafkaOutboxBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }
    
    public static List<KafkaOutboxBO> dto2Bo(List<KafkaOutboxDTO> dtos) {
        ArrayList<KafkaOutboxBO> bos = new ArrayList<KafkaOutboxBO>();
        for (KafkaOutboxDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
    
}
