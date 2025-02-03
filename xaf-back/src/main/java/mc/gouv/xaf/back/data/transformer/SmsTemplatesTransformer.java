package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.SmsTemplateBO;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;

/**
 * @author qdeme
 */
public class SmsTemplatesTransformer {

    private SmsTemplatesTransformer() {
    }

    public static SmsTemplateDTO bo2Dto(SmsTemplateBO bo) {
    	SmsTemplateDTO dto = new SmsTemplateDTO();
        dto.setCode(bo.getCode());
        dto.setContenu(bo.getContenu());
        dto.setLangue(bo.getLangue());
        dto.setPkSmsTemplates(bo.getPkSmsTemplates());
        dto.setDateModif(bo.getDateModif());
        dto.setSender(bo.getSender());
        return dto;
    }

    public static SmsTemplateBO dto2Bo(SmsTemplateDTO dto) {
    	SmsTemplateBO bo = new SmsTemplateBO();
        bo.setCode(dto.getCode());
        bo.setContenu(dto.getContenu());
        bo.setLangue(dto.getLangue());
        bo.setPkSmsTemplates(dto.getPkSmsTemplates());
        bo.setDateModif(dto.getDateModif());
        bo.setSender(dto.getSender());
        return bo;
    }

    public static List<SmsTemplateDTO> bo2Dto(List<SmsTemplateBO> bos) {
        ArrayList<SmsTemplateDTO> dtos = new ArrayList<>();
        for (SmsTemplateBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<SmsTemplateBO> dto2Bo(List<SmsTemplateDTO> dtos) {
        ArrayList<SmsTemplateBO> bos = new ArrayList<>();
        for (SmsTemplateDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
