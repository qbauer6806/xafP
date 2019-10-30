package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesComplementsFilesTransformer {

    public static DemandeComplementsFileDTO bo2Dto(DemandesComplementsFilesBO bo) {
        DemandeComplementsFileDTO dto = new DemandeComplementsFileDTO();
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        return dto;
    }

    public static DemandeFileDTO toDemandeFileDTO(DemandesComplementsFilesBO bo) {
        DemandeFileDTO dto = new DemandeFileDTO();
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        return dto;
    }

    public static DemandeFileDTO toDemandeFileDTO(DemandeComplementsFileDTO demandeComplementsFileDTO) {
        DemandeFileDTO dto = new DemandeFileDTO();
        dto.setName(demandeComplementsFileDTO.getName());
        dto.setUrl(demandeComplementsFileDTO.getUrl());
        dto.setMeta(demandeComplementsFileDTO.getMeta());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeComplementsBO après l'appel à cette fonction
     * 
     * @param dto
     * @return
     */
    public static DemandesComplementsFilesBO dto2Bo(DemandeComplementsFileDTO dto) {
        DemandesComplementsFilesBO bo = new DemandesComplementsFilesBO();
        bo.setName(dto.getName());
        bo.setUrl(dto.getUrl());
        bo.setMeta(dto.getMeta());
        return bo;
    }

    public static List<DemandeComplementsFileDTO> bo2Dto(List<DemandesComplementsFilesBO> bos) {
        ArrayList<DemandeComplementsFileDTO> dtos = new ArrayList<>();
        for (DemandesComplementsFilesBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandeFileDTO> toDemandeFileDTO(Set<DemandesComplementsFilesBO> bos) {
        ArrayList<DemandeFileDTO> dtos = new ArrayList<>();
        if (bos != null) {
            for (DemandesComplementsFilesBO bo : bos) {
                dtos.add(toDemandeFileDTO(bo));
            }
        }
        return dtos;
    }

    public static List<DemandeFileDTO> toDemandeFileDTO(List<DemandeComplementsFileDTO> demandeComplementsFileDTOs) {
        ArrayList<DemandeFileDTO> demandeFileDTOs = new ArrayList<>();
        if (demandeComplementsFileDTOs != null) {
            for (DemandeComplementsFileDTO demandeComplementsFileDTO : demandeComplementsFileDTOs) {
                demandeFileDTOs.add(toDemandeFileDTO(demandeComplementsFileDTO));
            }
        }
        return demandeFileDTOs;
    }

    public static List<DemandesComplementsFilesBO> dto2Bo(List<DemandeComplementsFileDTO> dtos) {
        ArrayList<DemandesComplementsFilesBO> bos = new ArrayList<>();
        for (DemandeComplementsFileDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
