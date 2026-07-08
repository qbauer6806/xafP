package mc.gouv.xaf.backweb.ws;

import mc.gouv.xaf.back.service.data.DemandeJobService;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeJobDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.inject.Inject;

@GouvRestController
@Secured("ROLE_CONFIGURATION")
@RequestMapping("/ws/admin/job")
public class JobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    @Inject
    private DemandeJobService demandeJobService;

    @PostMapping(value = "/execute")
    public String execute(@RequestParam("jobName") String jobName) {
        LOGGER.info("Appel du webservice /ws/admin/job/execute pour le job {}", jobName);
        demandeJobService.launch(jobName);
        return "Demande d'exécution du job " + jobName + " prise en compte";
    }

    @GetMapping(value = "/list")
    public Page<DemandeJobDTO> list(Pageable pageable) {
        LOGGER.info("Appel du webservice /ws/admin/job/list");
        return demandeJobService.list(pageable);
    }
}
