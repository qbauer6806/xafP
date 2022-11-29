package mc.gouv.xaf.back.service.es.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesComplementsFilesEsTransformer {

    private DemandesComplementsFilesEsTransformer() {}

    public static DemandeFileEsDTO toEs(DemandesComplementsFilesBO bo, String parent) {
        DemandeFileEsDTO dto = new DemandeFileEsDTO(parent);
        dto.getFichiers().setName(bo.getName());
        dto.getFichiers().setUrl(bo.getUrl());
        dto.getFichiers().setMeta(bo.getMeta());
        return dto;
    }

    public static List<DemandeFileEsDTO> toEs(List<DemandesComplementsFilesBO> bos, String parent) {
        ArrayList<DemandeFileEsDTO> dtos = new ArrayList<>();
        for (DemandesComplementsFilesBO bo : bos) {
            dtos.add(toEs(bo, parent));
        }
        return dtos;
    }

    public static List<DemandeFileEsDTO> toEs(Set<DemandesComplementsFilesBO> bos, String parent) {
        ArrayList<DemandeFileEsDTO> dtos = new ArrayList<>();
        for (DemandesComplementsFilesBO bo : bos) {
            dtos.add(toEs(bo, parent));
        }
        return dtos;
    }

}
