package mc.gouv.xaf.back.service.transformer;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.cache.MotifsCache;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeStatutEsDTO;
import mc.gouv.xaf.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.dem.data.entity.DemandesStatutsBO;
import mc.gouv.dem.shared.model.DemandeStatutDTO;
import mc.gouv.dem.shared.model.MotifDTO;

@Service
@Conditional(IndexationEnabledCondition.class)
public class DemandesStatutsEsTransformer {

    @Inject
    MotifsCache motifsCache;

    @Inject
    DemarchesDataProvider demarchesDataProvider;

    public DemandeStatutEsDTO bo2Dto(DemandesStatutsBO bo, Integer pkDemande) {
        if (bo == null) {
            return null;
        }
        DemandeStatutEsDTO dto = new DemandeStatutEsDTO();
        StatutPublicOuInterneDTO statutPublicOuInterneDTO = demarchesDataProvider.getStatutPublicOuInterne(pkDemande,
                bo.getLibelle());
        dto.setCode(statutPublicOuInterneDTO.getName());
        dto.setLibelle(statutPublicOuInterneDTO.getLibelle());
        dto.setCodeMotif(bo.getCodeMotif());
        dto.setCommentaire(bo.getCommentaire());
        dto.setTexteAEnvoyer(bo.getTexteAEnvoyer());
        MotifDTO motif = motifsCache.getMotif(bo.getCodeMotif(), "fr");
        dto.setLibelleMotif((motif != null) ? motif.getLibelle() : null);
        return dto;
    }

    public DemandeStatutEsDTO toEs(DemandeStatutDTO demandeStatutDTO, Integer pkDemande) {

        if (demandeStatutDTO == null) {
            return null;
        }
        DemandeStatutEsDTO dto = new DemandeStatutEsDTO();

        StatutPublicOuInterneDTO statutPublicOuInterneDTO = demarchesDataProvider.getStatutPublicOuInterne(pkDemande,
                demandeStatutDTO.getLibelle());
        dto.setCode(statutPublicOuInterneDTO.getName());
        dto.setLibelle(statutPublicOuInterneDTO.getLibelle());
        dto.setCodeMotif(demandeStatutDTO.getCodeMotif());
        dto.setCommentaire(demandeStatutDTO.getCommentaire());
        dto.setTexteAEnvoyer(demandeStatutDTO.getTexteAEnvoyer());
        MotifDTO motif = motifsCache.getMotif(demandeStatutDTO.getCodeMotif(), "fr");
        dto.setLibelleMotif((motif != null) ? motif.getLibelle() : null);
        return dto;

    }

    public Set<DemandeStatutEsDTO> bo2Dto(Set<DemandesStatutsBO> bos, Integer pkDemande) {
        Set<DemandeStatutEsDTO> dtos = new HashSet<>();
        for (DemandesStatutsBO bo : bos) {
            dtos.add(bo2Dto(bo, pkDemande));
        }
        return dtos;
    }

    public DemandeStatutEsDTO[] toEs(DemandeStatutDTO[] demandeStatutDTOs, Integer pkDemande) {
        if (demandeStatutDTOs == null || demandeStatutDTOs.length == 0)
            return new DemandeStatutEsDTO[0];
        DemandeStatutEsDTO[] dtos = new DemandeStatutEsDTO[demandeStatutDTOs.length];
        int i = 0;
        for (DemandeStatutDTO demandeStatutDTO : demandeStatutDTOs) {
            dtos[i] = toEs(demandeStatutDTO, pkDemande);
        }
        return dtos;
    }

}
