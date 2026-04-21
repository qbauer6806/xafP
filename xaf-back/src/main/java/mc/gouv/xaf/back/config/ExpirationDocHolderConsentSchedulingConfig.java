package mc.gouv.xaf.back.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.scheduling.ExpirationDocHolderConsentSchedulingJob;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpirationDocHolderConsentSchedulingConfig {

    private static final String EXPIRATION_DOCHOLDER_SCHEDULING_CRON_EXPRESSION = "EXPIRATION_DOCHOLDER_SCHEDULING_CRON_EXPRESSION";

    private final GouvSchedulerService schedulerService;
    private final PropertiesService propertiesService;

    @PostConstruct
    private void init() throws SchedulerException {
        PropertiesDTO prop = propertiesService.getProperty(EXPIRATION_DOCHOLDER_SCHEDULING_CRON_EXPRESSION);
        JobDetail jobDetail = schedulerService.buildJobDetail(ExpirationDocHolderConsentSchedulingJob.class,
                "ExpirationDocHolderConsentSchedulingJob");
        Trigger trigger = schedulerService.buildJobTrigger(jobDetail, "ExpirationDocHolderConsentSchedulingTrigger",
                prop.getValue());

        schedulerService.startOrUpdateScheduledJob(jobDetail, trigger);
    }
}
