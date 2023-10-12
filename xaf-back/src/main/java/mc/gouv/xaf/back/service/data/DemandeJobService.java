package mc.gouv.xaf.back.service.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import mc.gouv.xaf.shared.dto.DemandeJobDTO;
import mc.gouv.xaf.shared.enums.JobNamesEnum;

public interface DemandeJobService {

    void launch(JobNamesEnum jobName);

    Page<DemandeJobDTO> list(Pageable pageable);
}
