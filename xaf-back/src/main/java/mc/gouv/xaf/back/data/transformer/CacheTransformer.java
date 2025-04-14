package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.CacheBO;
import mc.gouv.xaf.shared.dto.CacheDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Transformer pour les valeurs mises en cache en en base de données.
 * 
 * Cf. commentaire de la classe CacheService afin d'en connaître l'utilité.
 * 
 * @author qdeme
 */
public class CacheTransformer {

    private CacheTransformer() {
    }

    public static CacheDTO bo2Dto(CacheBO bo) {
        if (bo == null) {
            return null;
        }
        CacheDTO dto = new CacheDTO();
        dto.setPkCache(bo.getPkCache());
        dto.setData(bo.getData());
        dto.setDateMaj(bo.getDateMaj());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction
     */
    public static CacheBO dto2Bo(CacheDTO dto) {
        if (dto == null) {
            return null;
        }
        CacheBO bo = new CacheBO();
        bo.setPkCache(dto.getPkCache());
        ObjectMapper objectMapper = new ObjectMapper();
        bo.setData(objectMapper.valueToTree(dto.getData()));
        bo.setDateMaj(dto.getDateMaj());
        return bo;
    }

    public static List<CacheDTO> bo2Dto(List<CacheBO> bos) {
        ArrayList<CacheDTO> dtos = new ArrayList<>();
        for (CacheBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static Set<CacheDTO> bo2Dto(Set<CacheBO> bos) {
        Set<CacheDTO> dtos = new HashSet<>();
        for (CacheBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<CacheBO> dto2Bo(List<CacheDTO> dtos) {
        ArrayList<CacheBO> bos = new ArrayList<>();
        for (CacheDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
