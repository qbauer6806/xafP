package mc.gouv.xaf.back.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import mc.gouv.xaf.back.dto.JobDTO;
import mc.gouv.xaf.data.enums.JobNamesEnum;

public interface DemandeJobService {

    void launch(JobNamesEnum jobName);

    Page<JobDTO> list(Pageable pageable);
}
