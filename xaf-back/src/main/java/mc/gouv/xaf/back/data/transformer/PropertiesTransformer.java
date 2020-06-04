package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.PropertiesBO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe permettant de lier les PropertiesBO et PropertiesDTO
 *
 * @author mboutelier.ext
 */
public class PropertiesTransformer {

    private PropertiesTransformer() {
    }

    public static PropertiesDTO bo2Dto(PropertiesBO bo) {
        PropertiesDTO dto = new PropertiesDTO();
        dto.setPkProperties(bo.getPkProperties());
        dto.setDemarcheId(bo.getDemarche().getPkDemarches());
        dto.setType(PropertiesTypeEnum.valueOf(bo.getType()));
        dto.setKey(bo.getKey());
        dto.setDescriptif(bo.getDescriptif());
        dto.setValue(bo.getValue());
        return dto;
    }

    public static PropertiesBO dto2Bo(PropertiesDTO dto) {
        PropertiesBO bo = new PropertiesBO();
        bo.setPkProperties(dto.getPkProperties());
        bo.setType(dto.getType().name());
        bo.setKey(dto.getKey());
        bo.setDescriptif(dto.getDescriptif());
        bo.setValue(dto.getValue());
        return bo;
    }

    public static List<PropertiesDTO> bo2Dto(List<PropertiesBO> bos) {
        List<PropertiesDTO> dtos = new ArrayList<>();
        for (PropertiesBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<PropertiesBO> dto2Bo(List<PropertiesDTO> dtos) {
        List<PropertiesBO> bos = new ArrayList<>();
        for (PropertiesDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
