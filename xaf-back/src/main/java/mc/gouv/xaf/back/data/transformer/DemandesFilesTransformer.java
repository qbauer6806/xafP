package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesFilesTransformer {

    private DemandesFilesTransformer() {
    }

    public static DemandeFileDTO bo2Dto(DemandesFilesBO bo) {
        DemandeFileDTO dto = new DemandeFileDTO();
        dto.setPkDemandesFiles(bo.getPkDemandesFiles());
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        dto.setDate(bo.getDate());
        dto.setTypedoc(bo.getTypedoc());
        dto.setVerification(bo.isVerification());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction
     */
    public static DemandesFilesBO dto2Bo(DemandeFileDTO dto) {
        DemandesFilesBO bo = new DemandesFilesBO();
        bo.setPkDemandesFiles(dto.getPkDemandesFiles());
        bo.setName(dto.getName());
        bo.setUrl(dto.getUrl().startsWith("/") ? dto.getUrl() : "/" + dto.getUrl());
        bo.setMeta(dto.getMeta());
        bo.setDate(dto.getDate());
        bo.setTypedoc(dto.getTypedoc());
        bo.setVerification(dto.isVerification());
        bo.setContenu(dto.getContenu());
        return bo;
    }

    public static List<DemandeFileDTO> bo2Dto(List<DemandesFilesBO> bos) {
        ArrayList<DemandeFileDTO> dtos = new ArrayList<>();
        for (DemandesFilesBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandesFilesBO> dto2Bo(List<DemandeFileDTO> dtos) {
        ArrayList<DemandesFilesBO> bos = new ArrayList<>();
        for (DemandeFileDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
