package mc.gouv.xaf.backweb.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.KafkaProperties;
import mc.gouv.xaf.shared.enums.JobNamesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/gestion/jobs")
@Secured("ROLE_CONFIGURATION")
@RequiredArgsConstructor
public class GestionJobsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionJobsController.class);

    private final KafkaProperties kafkaProperties;

    @GetMapping
    public ModelAndView form() {
        LOGGER.info("Appel de la page gestion/jobs. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/jobs/gestionjobs");
        mav.addObject("jobs", filterJobList(Arrays.asList(JobNamesEnum.values())));
        LOGGER.info("======================= Fin gestion/jobs. Méthode form");
        return mav;
    }

    // Ne pas afficher dans la liste des jobs, ceux concernant Kafka, si kafkaEnabled=false
    private List<JobNamesEnum> filterJobList(List<JobNamesEnum> jobList) {
        List<JobNamesEnum> newList = new ArrayList<>();
        boolean kafkaEnabled = kafkaProperties.isKafkaEnabled();
        for (JobNamesEnum job : jobList) {
            if (JobNamesEnum.SYNCHRONISATION_GLOBALE_GU.name().equals(job.name())
                    || JobNamesEnum.TRAITEMENT_DEAD_LETTER_TOPIC_GU_KAFKA.name().equals(job.name())
                    || JobNamesEnum.TRAITEMENT_OUTBOX_KAFKA.name().equals(job.name())) {
                if (kafkaEnabled) {
                    newList.add(job);
                }
            } else {
                newList.add(job);
            }
        }
        return newList;
    }

}
