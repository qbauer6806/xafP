package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandeJobRepository;
import mc.gouv.xaf.back.data.entity.DemandeJobBO;
import mc.gouv.xaf.back.data.transformer.DemandeJobTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.KafkaOutboxTraitementJob;
import mc.gouv.xaf.back.service.data.DemandeJobService;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.GouvSchedulerService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaDLTConsumer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.purge.PurgeBrouillonsService;
import mc.gouv.xaf.shared.dto.DemandeJobDTO;
import mc.gouv.xaf.shared.enums.JobNamesEnum;
import mc.gouv.xaf.shared.enums.JobStatutsEnum;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandeJobServiceImpl implements DemandeJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeJobServiceImpl.class);

    private final DemandeJobRepository demandeJobRepository;
    private final ApplicationContext context;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final KafkaOutboxTraitementJob kafkaOutboxTraitementJob;
    private final GUKafkaUtils guKafkaUtils;
    private final GUKafkaProducer guKafkaProducer;
    private final KafkaOutboxService kafkaOutboxService;
    private final PurgeBrouillonsService purgeBrouillonsService;
    private final GouvSchedulerService gouvSchedulerService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void launch(JobNamesEnum jobName) {
        launch(jobName != null ? jobName.name() : null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void launch(String jobName) {

        DemandeJobBO job = new DemandeJobBO();

        try {

            if (jobName == null) {
                throw new IllegalArgumentException("Aucun job à lancer");
            }

            JobNamesEnum jobEnum = JobNamesEnum.getByName(jobName);
            String libelle = jobEnum != null ? jobEnum.getLibelle() : jobName;
            LOGGER.info("Début du lancement du job {}", libelle);

            logExecutionStart(job, jobName);

            context.getBean(DemandeJobServiceImpl.class).launch(job);

        } catch (IllegalArgumentException e) {
            LOGGER.error("Une erreur est survenue lors du lancement du job.");
            throw e;
        } catch (Exception e) {
            context.getBean(DemandeJobServiceImpl.class).logErrors(job.getId(), e);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void launch(DemandeJobBO job) {
        try {
            String msg = "";

            JobNamesEnum jobEnum = JobNamesEnum.getByName(job.getJobName());

            if (jobEnum != null) {
                switch (jobEnum) {
                    case TRAITEMENT_DEAD_LETTER_TOPIC_GU_KAFKA:
                        if (gouvPropertiesResolver.isBackserver()) {
                            // Pas d'@Inject ni d'@Autowired car l'API doit pouvoir démarrer sans ça
                            msg = context.getBean(GUKafkaDLTConsumer.class).traiterDLT();
                        } else {
                            throw new DemarchesServiceException("Ce job doit être lancé par le backserver",
                                    HttpStatus.UNAUTHORIZED);
                        }
                        break;
                    case TRAITEMENT_OUTBOX_KAFKA:
                        msg = kafkaOutboxTraitementJob.execute();
                        break;
                    case SYNCHRONISATION_GLOBALE_GU:
                        List<UsagerDemandesRecapDTO> usagerDemandesRecaps = guKafkaUtils.getUsagerDemandesRecapList();
                        guKafkaProducer.sendSynchronisationDemandesMessage(usagerDemandesRecaps);
                        msg = "Message placé dans l'Outbox Kafka pour envoi";
                        break;
                    case RECUPERATION_NOMBRE_MESSAGES_OUTBOX_KAFKA:
                        Integer nbMessages = kafkaOutboxService.getNbOutboxElements();
                        msg = "L'Outbox Kafka contient " + nbMessages;
                        if (nbMessages > 1) {
                            msg += " messages.";
                        } else {
                            msg += " message.";
                        }
                        break;
                    case PURGE_BROUILLONS:
                        String res = purgeBrouillonsService.purgerBrouillons();
                        msg = "Purge des brouillons terminée.<br>" + res;
                        break;
                    default:
                        break;
                }
            } else {
                // Si ce n'est pas un job statique, on tente de le lancer via Quartz
                // Le GouvJobListener se chargera de mettre à jour le statut et les logs
                gouvSchedulerService.triggerJob(job.getJobName(), job.getId());
                // Ne pas appeler logSuccess ici : le listener mettra à jour le job une fois terminé
                return;
            }

            context.getBean(DemandeJobServiceImpl.class).logSuccess(job.getId(), msg);

            JobNamesEnum jobLogEnum = JobNamesEnum.getByName(job.getJobName());
            String libelle = jobLogEnum != null ? jobLogEnum.getLibelle() : job.getJobName();
            LOGGER.info("Fin du lancement du job {}", libelle);

        } catch (Exception e) {
            context.getBean(DemandeJobServiceImpl.class).logErrors(job.getId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void logExecutionStart(DemandeJobBO job, String jobName) {
        Date now = new Date();
        job.setDateCreation(now);
        job.setDateDernModif(now);
        job.setJobName(jobName);
        job.setMsg("Début de l'exécution du job");
        job.setStatut(JobStatutsEnum.RUNNING);
        demandeJobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logErrors(Integer jobId, Exception e) {

        StringBuilder errorMsg;
        if (e.getMessage() != null) {
            errorMsg = new StringBuilder(e.getMessage());
        } else {
            errorMsg = new StringBuilder("Une erreur inattendue est survenue");
        }
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            errorMsg.append("\n").append(e.getCause().getMessage());
        }

        Optional<DemandeJobBO> jobOpt = demandeJobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            DemandeJobBO job = jobOpt.get();
            JobNamesEnum jobEnum = JobNamesEnum.getByName(job.getJobName());
            String libelle = jobEnum != null ? jobEnum.getLibelle() : job.getJobName();
            LOGGER.error("Une erreur est survenue lors du lancement du job {} : {}", libelle,
                    errorMsg, e);
            job.setStatut(JobStatutsEnum.ERROR);
            job.setMsg(errorMsg.toString());
            job.setDateDernModif(new Date());
            demandeJobRepository.save(job);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(Integer jobId, String msg) {

        Optional<DemandeJobBO> jobOpt = demandeJobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            DemandeJobBO job = jobOpt.get();
            JobNamesEnum jobEnum = JobNamesEnum.getByName(job.getJobName());
            String libelle = jobEnum != null ? jobEnum.getLibelle() : job.getJobName();
            LOGGER.info("Le job {} a été exécuté avec succès", libelle);
            job.setStatut(JobStatutsEnum.SUCCEEDED);
            job.setMsg(msg);
            job.setDateDernModif(new Date());
            demandeJobRepository.save(job);
        }
    }

    @Override
    public Page<DemandeJobDTO> list(Pageable pageable) {
        Page<DemandeJobBO> jobs = demandeJobRepository.findAll(pageable);
        return DemandeJobTransformer.bo2Dto(jobs);
    }

}
