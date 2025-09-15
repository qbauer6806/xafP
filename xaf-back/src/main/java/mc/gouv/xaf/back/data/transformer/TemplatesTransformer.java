package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.TemplateBO;
import mc.gouv.xaf.shared.dto.ExportTemplateDTO;
import mc.gouv.xaf.shared.dto.TemplateDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qdeme
 */
public class TemplatesTransformer {

    private TemplatesTransformer() {
    }

    public static TemplateDTO bo2Dto(TemplateBO bo) {
        TemplateDTO dto = new TemplateDTO();
        dto.setCode(bo.getCode());
        dto.setContenu(bo.getContenu());
        dto.setLangue(bo.getLangue());
        dto.setPkTemplates(bo.getPkTemplates());
        dto.setDateModif(bo.getDateModif());
        return dto;
    }

    public static ExportTemplateDTO bo2ExportDto(TemplateBO bo) {
        ExportTemplateDTO dto = new ExportTemplateDTO();
        dto.setCode(bo.getCode());
        dto.setContenu(bo.getContenu());
        dto.setLangue(bo.getLangue());
        dto.setDateModif(bo.getDateModif());
        return dto;
    }

    public static TemplateBO dto2Bo(TemplateDTO dto) {
        TemplateBO bo = new TemplateBO();
        bo.setCode(dto.getCode());
        bo.setContenu(dto.getContenu());
        bo.setLangue(dto.getLangue());
        bo.setPkTemplates(dto.getPkTemplates());
        bo.setDateModif(dto.getDateModif());
        return bo;
    }

    public static TemplateBO exportDto2Bo(ExportTemplateDTO dto) {
        TemplateBO bo = new TemplateBO();
        bo.setCode(dto.getCode());
        bo.setContenu(dto.getContenu());
        bo.setLangue(dto.getLangue());
        bo.setDateModif(dto.getDateModif());
        return bo;
    }

    public static List<TemplateDTO> bo2Dto(List<TemplateBO> bos) {
        ArrayList<TemplateDTO> dtos = new ArrayList<>();
        for (TemplateBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<ExportTemplateDTO> bo2ExportDto(List<TemplateBO> bos) {
        ArrayList<ExportTemplateDTO> dtos = new ArrayList<>();
        for (TemplateBO bo : bos) {
            dtos.add(bo2ExportDto(bo));
        }
        return dtos;
    }

    public static List<TemplateBO> dto2Bo(List<TemplateDTO> dtos) {
        ArrayList<TemplateBO> bos = new ArrayList<>();
        for (TemplateDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

    public static List<TemplateBO> exportDto2Bo(List<ExportTemplateDTO> dtos) {
        ArrayList<TemplateBO> bos = new ArrayList<>();
        for (ExportTemplateDTO dto : dtos) {
            bos.add(exportDto2Bo(dto));
        }
        return bos;
    }

}
