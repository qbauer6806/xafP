package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import mc.gouv.xaf.back.data.entity.DemandeJobBO;
import mc.gouv.xaf.shared.dto.DemandeJobDTO;

public class DemandeJobTransformer {

    private DemandeJobTransformer() {}

    public static DemandeJobDTO bo2Dto(DemandeJobBO jobBO) {
        if (jobBO == null) {
            return null;
        }
        DemandeJobDTO jobDTO = new DemandeJobDTO();
        jobDTO.setDateCreation(jobBO.getDateCreation());
        jobDTO.setDateDernModif(jobBO.getDateDernModif());
        jobDTO.setMsg(jobBO.getMsg());
        jobDTO.setJobName((jobBO.getJobName() != null) ? jobBO.getJobName().getLibelle() : null);
        jobDTO.setStatut((jobBO.getStatut() != null) ? jobBO.getStatut().getLibelle() : null);
        jobDTO.setStatutCode((jobBO.getStatut() != null) ? jobBO.getStatut().name() : null);
        return jobDTO;
    }

    public static Page<DemandeJobDTO> bo2Dto(Page<DemandeJobBO> jobBOs) {

        if (jobBOs == null) {
            return null;
        }

        Pageable newPageable = PageRequest.of(jobBOs.getNumber(), jobBOs.getSize(), jobBOs.getSort());

        List<DemandeJobDTO> jobDTOs = new ArrayList<>();

        for (DemandeJobBO jobBO : jobBOs) {
            if (jobBO != null) {
                jobDTOs.add(bo2Dto(jobBO));
            }
        }
        return new PageImpl<>(jobDTOs, newPageable, jobBOs.getTotalElements());
    }

}
