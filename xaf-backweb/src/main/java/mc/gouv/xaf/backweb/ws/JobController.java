package mc.gouv.xaf.backweb.ws;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.service.data.DemandeJobService;
import mc.gouv.xaf.shared.dto.DemandeJobDTO;
import mc.gouv.xaf.shared.dto.JobNamesEnum;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

@GouvRestController
@Conditional(IndexationEnabledCondition.class)
@Secured("ROLE_CONFIGURATION")
@RequestMapping("/ws/admin/job")
public class JobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    @Inject
    DemandeJobService demandeJobService;

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    public String execute(@RequestParam("jobName") String jobName) {
        LOGGER.info("Appel du webservice /ws/admin/job/execute");
        demandeJobService.launch(JobNamesEnum.getByName(jobName));
        return "Demande d'exécution du job " + jobName + " prise en compte";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Page<DemandeJobDTO> list(Pageable pageable) {
        LOGGER.info("Appel du webservice /ws/admin/job/list");
        return demandeJobService.list(pageable);
    }
}
