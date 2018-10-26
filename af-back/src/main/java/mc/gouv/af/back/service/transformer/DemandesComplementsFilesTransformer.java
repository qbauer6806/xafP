package mc.gouv.af.back.service.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mc.gouv.af.back.data.es.model.DemandeEsDTO.DemandeFileEsDTO;
import mc.gouv.dem.data.entity.DemandesComplementsFilesBO;
import mc.gouv.dem.shared.model.DemandeFileDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesComplementsFilesTransformer
        extends mc.gouv.dem.service.transformer.DemandesComplementsFilesTransformer {

    public static DemandeFileEsDTO toEs(DemandesComplementsFilesBO bo) {
        DemandeFileEsDTO dto = new DemandeFileEsDTO();
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        dto.setContent(bo.getContent());
        dto.setLanguage(bo.getLanguage());
        return dto;
    }

    public static List<DemandeFileEsDTO> toEs(List<DemandesComplementsFilesBO> bos) {
        ArrayList<DemandeFileEsDTO> dtos = new ArrayList<>();
        for (DemandesComplementsFilesBO bo : bos) {
            dtos.add(toEs(bo));
        }
        return dtos;
    }

    public static List<DemandeFileEsDTO> toEs(Set<DemandesComplementsFilesBO> bos) {
        ArrayList<DemandeFileEsDTO> dtos = new ArrayList<>();
        for (DemandesComplementsFilesBO bo : bos) {
            dtos.add(toEs(bo));
        }
        return dtos;
    }

    public static List<DemandeFileDTO> toDemandeFileDTO(Set<DemandesComplementsFilesBO> bos,
            DemandeFileEsDTO.TYPE type) {
        ArrayList<DemandeFileDTO> dtos = new ArrayList<>();
        if (bos != null) {
            for (DemandesComplementsFilesBO bo : bos) {
                dtos.add(toDemandeFileDTO(bo));
            }
        }
        return dtos;
    }

}
