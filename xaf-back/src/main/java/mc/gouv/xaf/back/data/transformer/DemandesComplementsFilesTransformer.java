package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.Date;
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

    private DemandesComplementsFilesTransformer() {}

    public static DemandeComplementsFileDTO bo2Dto(DemandesComplementsFilesBO bo) {
        DemandeComplementsFileDTO dto = new DemandeComplementsFileDTO();
        dto.setPkDemandesComplementsFiles(bo.getPkDemandesComplementsFiles());
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        dto.setTypedoc(bo.getTypedoc());
        dto.setVerification(bo.isVerification());
        dto.setContenu(bo.getContenu());
        return dto;
    }

    public static DemandeFileDTO toDemandeFileDTO(DemandeComplementsFileDTO demandeComplementsFileDTO) {
        DemandeFileDTO dto = new DemandeFileDTO();
        dto.setPkDemandesComplementsFiles(demandeComplementsFileDTO.getPkDemandesComplementsFiles());
        dto.setName(demandeComplementsFileDTO.getName());
        dto.setUrl(demandeComplementsFileDTO.getUrl());
        dto.setMeta(demandeComplementsFileDTO.getMeta());
        dto.setTypedoc(demandeComplementsFileDTO.getTypedoc());
        dto.setCompFile(true);
        dto.setVerification(demandeComplementsFileDTO.isVerification());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeComplementsBO après l'appel à cette fonction
     */
    public static DemandesComplementsFilesBO dto2Bo(DemandeComplementsFileDTO dto) {
        DemandesComplementsFilesBO bo = new DemandesComplementsFilesBO();
        bo.setPkDemandesComplementsFiles(dto.getPkDemandesComplementsFiles());
        bo.setName(dto.getName());
        bo.setUrl(dto.getUrl());
        bo.setMeta(dto.getMeta());
        bo.setTypedoc(dto.getTypedoc());
        bo.setVerification(dto.isVerification());
        bo.setContenu(dto.getContenu());
        return bo;
    }

    public static List<DemandeComplementsFileDTO> bo2Dto(List<DemandesComplementsFilesBO> bos) {
        ArrayList<DemandeComplementsFileDTO> dtos = new ArrayList<>();
        for (DemandesComplementsFilesBO bo : bos) {
            dtos.add(bo2Dto(bo));
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

    public static List<DemandeFileDTO> toDemandeFileDTOCategorizer(List<DemandeComplementsFileDTO> demandeComplementsFileDTOs, Date date) {
        ArrayList<DemandeFileDTO> demandeFileDTOs = new ArrayList<>();
        if (demandeComplementsFileDTOs != null) {
            for (DemandeComplementsFileDTO demandeComplementsFileDTO : demandeComplementsFileDTOs) {
                DemandeFileDTO file = toDemandeFileDTO(demandeComplementsFileDTO);
                file.setDate(date);
                demandeFileDTOs.add(file);
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
