package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.DemandeJobRepository;
import mc.gouv.xaf.back.data.entity.DemandeJobBO;
import mc.gouv.xaf.back.data.transformer.DemandeJobTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.KafkaOutboxTraitementJob;
import mc.gouv.xaf.back.service.data.DemandeJobService;
import mc.gouv.xaf.back.service.data.DemandesStatutsRefreshService;
import mc.gouv.xaf.back.service.data.KafkaOutboxService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaDLTConsumer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.shared.dto.DemandeJobDTO;
import mc.gouv.xaf.shared.enums.JobNamesEnum;
import mc.gouv.xaf.shared.enums.JobStatutsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class DemandeJobServiceImpl implements DemandeJobService {

    private static final String LES_DEMANDES = "Les demandes ";

    @Inject
    IndexedDemandeService indexedDemandeService;

    @Inject
    DemandeJobRepository demandeJobRepository;

    @Inject
    private ApplicationContext context;

    @Inject
    private DemandesStatutsRefreshService demandesStatutsRefreshService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private KafkaOutboxTraitementJob kafkaOutboxTraitementJob;
    
    @Autowired
    private GUKafkaUtils guKafkaUtils;
    
    @Autowired
    private GUKafkaProducer guKafkaProducer;
    
    @Autowired
    private KafkaOutboxService kafkaOutboxService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeJobServiceImpl.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void launch(JobNamesEnum jobName) {

        DemandeJobBO job = new DemandeJobBO();

        try {

            if (jobName == null) {
                throw new IllegalArgumentException("Aucun job à lancer");
            }

            LOGGER.info("Début du lancement du job {}", jobName.getLibelle());

            logExecutionStart(job, jobName);

            context.getBean(DemandeJobServiceImpl.class).launch(job);

        } catch (IllegalArgumentException e) {
            LOGGER.error("Une erreur est survenue lors du lancement du job.");
            throw e;
        } catch (Exception e) {
            context.getBean(DemandeJobServiceImpl.class).logErrors(job.getId(), e);
        }
    }

    /**
     * <p>Job de recherche des demandes désynchronisées entre ES et la BDD.</p>
     * <p>[0] Demandes présentes dans ES mais pas en BDD</p>
     * <p>[1] Demandes présentes en BDD mais pas dans ES</p>
     * @return Un message contenant le résultat de la recherche.
     */
    private String getDemandesDesynchroJob() {
        List<List<String>> ret = indexedDemandeService.getDemandesDesynchro();
        String msg = "";
        if (ret != null && !ret.get(0).isEmpty()) {
            msg = LES_DEMANDES + ret.get(0) + " sont présentes dans ES mais pas en BDD<br/>";
        }
        if (ret != null && !ret.get(1).isEmpty()) {
            msg += LES_DEMANDES + ret.get(1) + " sont présentes en BDD mais pas dans ES";
        }
        if (ret == null || ret.get(0).isEmpty() && ret.get(1).isEmpty()) {
            msg = "Aucune demande désynchronisée";
        }
        return msg;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void launch(DemandeJobBO job) {
        try {
            String msg = "";
            Long demCount;

            switch (job.getJobName()) {
                case GET_DEMANDES_DESYNCHRONISEES:
                    msg = getDemandesDesynchroJob();
                    break;
                case REINDEXATION_DEMANDES_DESYNCHRO:
                    List<String> demandes = indexedDemandeService.reindexDemandesDesynchro();
                    if (!demandes.isEmpty()) {
                        msg = LES_DEMANDES + demandes + " ont été synchronisées (supprimées d'ES et/ou reindéxées)";
                    } else {
                        msg = "Aucune demande n'a été synchronisée";
                    }
                    break;
                case REINDEXATION:
                    demCount = indexedDemandeService.reindex();
                    msg = "Tous les fichiers et contenus des " + demCount + " demandes ont été reindéxés";
                    break;
                case REINDEXATION_DEMANDES:
                    demCount = indexedDemandeService.reindexDemandes();
                    msg = demCount + " demandes ont été reindéxées";
                    break;
                case REINDEXATION_COURRIER:
                    demCount = indexedDemandeService.reindexDemandesCourrier();
                    msg = demCount + " demandes courrier ont été reindéxées";
                    break;
                case RAFRAICHISSEMENT_STATUS:
                    msg = demandesStatutsRefreshService.refreshStatuts();
                    break;
                case TRAITEMENT_DEAD_LETTER_TOPIC_GU_KAFKA:
                    if (gouvPropertiesResolver.isBackserver()) {
                        // Pas d'@Inject ni d'@Autowired car l'API doit pouvoir démarrer sans ça
                        msg = context.getBean(GUKafkaDLTConsumer.class).traiterDLT();
                    } else {
                        throw new DemarchesServiceException("Ce job doit être lancé par le backserver", HttpStatus.UNAUTHORIZED);
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
                    }
                    else {
                        msg += " message.";
                    }
                    break;
                default:
                    break;
            }

            context.getBean(DemandeJobServiceImpl.class).logSuccess(job.getId(), msg);

            LOGGER.info("Fin du lancement du job {}", job.getJobName().getLibelle());

        } catch (Exception e) {
            context.getBean(DemandeJobServiceImpl.class).logErrors(job.getId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void logExecutionStart(DemandeJobBO job, JobNamesEnum jobName) {
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
			LOGGER.error("Une erreur est survenue lors du lancement du job {} : {}", job.getJobName().getLibelle(),
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
            LOGGER.info("Le job {} a été exécuté avec succès", job.getJobName().getLibelle());
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
