package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;

/**
 * 
 * @author qdeme
 *
 */
public class BrouillonsFilesTransformer {

    private BrouillonsFilesTransformer() {
    }

    public static BrouillonFileDTO bo2Dto(BrouillonsFilesBO bo) {
        BrouillonFileDTO dto = new BrouillonFileDTO();
        dto.setPkBrouillonsFiles(bo.getPkBrouillonsFiles());
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        dto.setDate(bo.getDate());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction
     * 
     * @param dto
     * @return
     */
    public static BrouillonsFilesBO dto2Bo(BrouillonFileDTO dto) {
        BrouillonsFilesBO bo = new BrouillonsFilesBO();
        bo.setPkBrouillonsFiles(dto.getPkBrouillonsFiles());
        bo.setName(dto.getName());
        bo.setUrl(dto.getUrl());
        bo.setMeta(dto.getMeta());
        bo.setDate(dto.getDate());
        return bo;
    }

    public static List<BrouillonFileDTO> bo2Dto(List<BrouillonsFilesBO> bos) {
        ArrayList<BrouillonFileDTO> dtos = new ArrayList<>();
        for (BrouillonsFilesBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<BrouillonsFilesBO> dto2Bo(List<BrouillonFileDTO> dtos) {
        ArrayList<BrouillonsFilesBO> bos = new ArrayList<>();
        for (BrouillonFileDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
