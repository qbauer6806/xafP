package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.TemplateBO;
import mc.gouv.xaf.back.shared.dto.TemplateDTO;

/**
 * 
 * @author qdeme
 *
 */
public class TemplatesTransformer {

    public static TemplateDTO bo2Dto(TemplateBO bo) {
        TemplateDTO dto = new TemplateDTO();
        dto.setCode(bo.getCode());
        dto.setContenu(bo.getContenu());
        dto.setDemarcheId(bo.getDemarcheId());
        dto.setLangue(bo.getLangue());
        dto.setPkTemplates(bo.getPkTemplates());
        return dto;
    }
    
    public static TemplateBO dto2Bo(TemplateDTO dto) {
        TemplateBO bo = new TemplateBO();
        bo.setCode(dto.getCode());
        bo.setContenu(dto.getContenu());
        bo.setDemarcheId(dto.getDemarcheId());
        bo.setLangue(dto.getLangue());
        bo.setPkTemplates(dto.getPkTemplates());
        return bo;
    }
    
    public static List<TemplateDTO> bo2Dto(List<TemplateBO> bos) {
        ArrayList<TemplateDTO> dtos = new ArrayList<TemplateDTO>();
        for (TemplateBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }
    
    public static List<TemplateBO> dto2Bo(List<TemplateDTO> dtos) {
        ArrayList<TemplateBO> bos = new ArrayList<TemplateBO>();
        for (TemplateDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
    
}
