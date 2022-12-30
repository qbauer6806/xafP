package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.DemandeJobRepository;
import mc.gouv.xaf.back.data.entity.DemandeJobBO;
import mc.gouv.xaf.back.data.transformer.DemandeJobTransformer;
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
import mc.gouv.xaf.shared.dto.JobNamesEnum;
import mc.gouv.xaf.shared.dto.JobStatutsEnum;

@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class DemandeJobServiceImpl implements DemandeJobService {

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
    public void launch(JobNamesEnum jobName) {

        DemandeJobBO job = new DemandeJobBO();

        try {

            if (jobName == null) {
                throw new IllegalArgumentException("Aucun job à lancer");
            }

            LOGGER.info("Début du lancement du job {} : {}", jobName.getLibelle());

            logExecutionStart(job, jobName);

            context.getBean(DemandeJobServiceImpl.class).launch(job);

        } catch (IllegalArgumentException e) {

            LOGGER.error("Une erreur est survenue lors du lancement du job : {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());

        } catch (Exception e) {

            context.getBean(DemandeJobServiceImpl.class).logErrors(job.getId(), e);

        }

    }

    @Async
    @Transactional(propagation = Propagation.REQUIRED)
    public void launch(DemandeJobBO job) {
        try {
            String msg = "";
            if (job.getJobName().equals(JobNamesEnum.REINDEXATION)) {
                Long demCount = indexedDemandeService.reindex();
                msg = "Tous les fichiers et contenus des " + demCount + " demandes ont été reindéxés";
            }
            if (job.getJobName().equals(JobNamesEnum.REINDEXATION_DEMANDES)) {
                Long demCount = indexedDemandeService.reindexDemandes();
                msg = demCount + " demandes ont été reindéxées";
            }
            if (job.getJobName().equals(JobNamesEnum.REINDEXATION_DEMANDES_DESYNCHRO)) {
                List<String> demandes = indexedDemandeService.reindexDemandesDesynchro();
                if (demandes.size() > 0) {
                    msg = "Les demandes " + demandes + " ont été synchronisées (supprimées d'ES et/ou reindéxées)";
                } else {
                    msg = "Aucune demande n'a été synchronisée";
                }
            }
            if (job.getJobName().equals(JobNamesEnum.GET_DEMANDES_DESYNCHRONISEES)) {
                // [0] Demandes présentes dans ES mais pas en BDD
                // [1] Demandes présentes en BDD mais pas dans ES
                List<List<String>> ret = indexedDemandeService.getDemandesDesynchro();
                if (ret != null && ret.get(0).size() > 0) {
                    msg = "Les demandes " + ret.get(0) + " sont présentes dans ES mais pas en BDD<br/>";
                }
                if (ret != null && ret.get(1).size() > 0) {
                    msg += "Les demandes " + ret.get(1) + " sont présentes en BDD mais pas dans ES";
                }
                if (ret == null || ret.get(0).isEmpty() && ret.get(1).isEmpty()) {
                    msg = "Aucune demande désynchronisée";
                }
            }
            if (job.getJobName().equals(JobNamesEnum.RAFRAICHISSEMENT_STATUS)) {
                msg = demandesStatutsRefreshService.refreshStatuts();
            }
            if (job.getJobName().equals(JobNamesEnum.TRAITEMENT_DEAD_LETTER_TOPIC_GU_KAFKA)) {
            	if (gouvPropertiesResolver.isBackserver()) {
            		// Pas d'@Inject ni d'@Autowired car l'API doit pouvoir démarrer sans ça
            		msg = context.getBean(GUKafkaDLTConsumer.class).traiterDLT();
            	}
            	else {
            		throw new Exception("Ce job doit être lancé par le backserver");
            	}
            }
            if (job.getJobName().equals(JobNamesEnum.TRAITEMENT_OUTBOX_KAFKA)) {
            	msg = kafkaOutboxTraitementJob.execute();
            }
            if (job.getJobName().equals(JobNamesEnum.SYNCHRONISATION_GLOBALE_GU)) {
        	    List<UsagerDemandesRecapDTO> usagerDemandesRecaps = guKafkaUtils.getUsagerDemandesRecapList();
        	    guKafkaProducer.sendSynchronisationDemandesMessage(usagerDemandesRecaps);
        	    msg = "Message placé dans l'Outbox Kafka pour envoi";
            }
            if (job.getJobName().equals(JobNamesEnum.RECUPERATION_NOMBRE_MESSAGES_OUTBOX_KAFKA)) {
            	Integer nbMessages = kafkaOutboxService.getNbOutboxElements();
            	msg = "L'Outbox Kafka contient " + nbMessages;
            	if (nbMessages > 1) {
            		msg += " messages.";
            	}
            	else {
            		 msg += " message.";
            	}
            }

            context.getBean(DemandeJobServiceImpl.class).logSuccess(job.getId(), msg);

            LOGGER.info("Fin du lancement du job {}", job.getJobName().getLibelle());

        } catch (Exception e) {
            context.getBean(DemandeJobServiceImpl.class).logErrors(job.getId(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
