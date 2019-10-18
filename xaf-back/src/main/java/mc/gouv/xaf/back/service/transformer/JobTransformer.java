package mc.gouv.xaf.back.service.transformer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import mc.gouv.xaf.back.dto.JobDTO;
import mc.gouv.xaf.data.entity.DemandeJobBO;

public class JobTransformer {

    public static JobDTO bo2Dto(DemandeJobBO jobBO) {
        if (jobBO == null) {
            return null;
        }
        JobDTO jobDTO = new JobDTO();
        jobDTO.setDateCreation(jobBO.getDateCreation());
        jobDTO.setDateDernModif(jobBO.getDateDernModif());
        jobDTO.setMsg(jobBO.getMsg());
        jobDTO.setJobName((jobBO.getJobName() != null) ? jobBO.getJobName().getLibelle() : null);
        jobDTO.setStatut((jobBO.getStatut() != null) ? jobBO.getStatut().getLibelle() : null);
        jobDTO.setStatutCode((jobBO.getStatut() != null) ? jobBO.getStatut().name() : null);
        return jobDTO;
    }

    public static Page<JobDTO> bo2Dto(Page<DemandeJobBO> jobBOs) {

        if (jobBOs == null) {
            return null;
        }

        Pageable newPageable = PageRequest.of(jobBOs.getNumber(), jobBOs.getSize(), jobBOs.getSort());

        List<JobDTO> jobDTOs = new ArrayList<>();

        for (DemandeJobBO jobBO : jobBOs) {
            if (jobBO != null) {
                jobDTOs.add(bo2Dto(jobBO));
            }
        }
        return new PageImpl<>(jobDTOs, newPageable, jobBOs.getTotalElements());
    }

}
