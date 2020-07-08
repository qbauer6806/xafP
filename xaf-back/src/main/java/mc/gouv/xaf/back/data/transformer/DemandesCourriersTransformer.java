package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesCourriersTransformer {

    public static DemandeCourrierDTO bo2Dto(DemandesCourriersBO bo) {
        if (bo == null) {
            return null;
        }
        DemandeCourrierDTO dto = new DemandeCourrierDTO();
        dto.setPkCourrier(bo.getPkDemandesCourriers());
        dto.setDemandeId(bo.getFkDemandes().getPkDemandes());
        dto.setFkStatut(DemandesStatutsTransformer.bo2Dto(bo.getFkDemandesStatuts()));
        dto.setDateCreation(bo.getDateCreation());
        dto.setName(bo.getName());
        dto.setUrl(bo.getUrl());
        dto.setMeta(bo.getMeta());
        dto.setDatePrinted(bo.getDatePrinted());
        dto.setIdentifiant(bo.getIdentifiant());
        dto.setDemandeIdentifiant(bo.getFkDemandes().getIdentifiant());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction Mapper le statut concerné après
     * appel à cette fonction, si besoin
     * 
     * @param dto
     * @return
     */
    public static DemandesCourriersBO dto2Bo(DemandeCourrierDTO dto) {
        if (dto == null) {
            return null;
        }
        DemandesCourriersBO bo = new DemandesCourriersBO();
        bo.setPkDemandesCourriers(dto.getPkCourrier());
        bo.setDateCreation(dto.getDateCreation());
        bo.setName(dto.getName());
        bo.setUrl(dto.getUrl());
        bo.setMeta(dto.getMeta());
        bo.setDatePrinted(dto.getDatePrinted());
        bo.setIdentifiant(dto.getIdentifiant());
        return bo;
    }

    public static List<DemandeCourrierDTO> bo2Dto(List<DemandesCourriersBO> bos) {
        ArrayList<DemandeCourrierDTO> dtos = new ArrayList<DemandeCourrierDTO>();
        for (DemandesCourriersBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static Set<DemandeCourrierDTO> bo2Dto(Set<DemandesCourriersBO> bos) {
        Set<DemandeCourrierDTO> dtos = new HashSet<>();
        for (DemandesCourriersBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandesCourriersBO> dto2Bo(List<DemandeCourrierDTO> dtos) {
        ArrayList<DemandesCourriersBO> bos = new ArrayList<DemandesCourriersBO>();
        for (DemandeCourrierDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
