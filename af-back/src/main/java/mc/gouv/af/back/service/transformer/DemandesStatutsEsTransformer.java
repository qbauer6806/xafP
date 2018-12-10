package mc.gouv.af.back.service.transformer;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import mc.gouv.af.back.cache.MotifsCache;
import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.data.es.model.DemandeStatutEsDTO;
import mc.gouv.dem.data.entity.DemandesStatutsBO;
import mc.gouv.dem.shared.model.DemandeStatutDTO;
import mc.gouv.dem.shared.model.MotifDTO;

@Service
@Conditional(IndexationEnabledCondition.class)
public class DemandesStatutsEsTransformer {

    @Inject
    MotifsCache motifsCache;

    public DemandeStatutEsDTO bo2Dto(DemandesStatutsBO bo) {
        if (bo == null) {
            return null;
        }
        DemandeStatutEsDTO dto = new DemandeStatutEsDTO();
        dto.setLibelle(bo.getLibelle());
        dto.setCodeMotif(bo.getCodeMotif());
        dto.setCommentaire(bo.getCommentaire());
        MotifDTO motif = motifsCache.getMotif(bo.getCodeMotif(), "fr");
        dto.setLibelleMotif((motif != null) ? motif.getLibelle() : null);
        return dto;
    }

    public DemandeStatutEsDTO toEs(DemandeStatutDTO demandeStatutDTO) {

        if (demandeStatutDTO == null) {
            return null;
        }
        DemandeStatutEsDTO dto = new DemandeStatutEsDTO();
        dto.setLibelle(demandeStatutDTO.getLibelle());
        dto.setCodeMotif(demandeStatutDTO.getCodeMotif());
        dto.setCommentaire(demandeStatutDTO.getCommentaire());
        MotifDTO motif = motifsCache.getMotif(demandeStatutDTO.getCodeMotif(), "fr");
        dto.setLibelleMotif((motif != null) ? motif.getLibelle() : null);
        return dto;

    }

    public Set<DemandeStatutEsDTO> bo2Dto(Set<DemandesStatutsBO> bos) {
        Set<DemandeStatutEsDTO> dtos = new HashSet<>();
        for (DemandesStatutsBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public DemandeStatutEsDTO[] toEs(DemandeStatutDTO[] demandeStatutDTOs) {
        if (demandeStatutDTOs == null || demandeStatutDTOs.length == 0)
            return new DemandeStatutEsDTO[0];
        DemandeStatutEsDTO[] dtos = new DemandeStatutEsDTO[demandeStatutDTOs.length];
        int i = 0;
        for (DemandeStatutDTO demandeStatutDTO : demandeStatutDTOs) {
            dtos[i] = toEs(demandeStatutDTO);
        }
        return dtos;
    }

}
