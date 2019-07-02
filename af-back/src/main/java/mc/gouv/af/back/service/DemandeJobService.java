package mc.gouv.af.back.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import mc.gouv.af.back.dto.JobDTO;
import mc.gouv.af.data.enums.JobNamesEnum;

public interface DemandeJobService {

    void launch(JobNamesEnum jobName);

    Page<JobDTO> list(Pageable pageable);
}
