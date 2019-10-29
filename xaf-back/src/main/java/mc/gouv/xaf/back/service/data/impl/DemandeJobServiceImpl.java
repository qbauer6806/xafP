package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import mc.gouv.xaf.back.service.data.DemandeJobService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
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
                msg = demCount + " demandes ont été reindéxées";
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

        StringBuilder errorMsg = new StringBuilder(e.getMessage());
        if (e.getCause() != null) {
            errorMsg.append("\n").append(e.getCause().getMessage());
        }

        Optional<DemandeJobBO> jobOpt = demandeJobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            DemandeJobBO job = jobOpt.get();
            LOGGER.error("Une erreur est survenue lors du lancement du job {} : {}", job.getJobName().getLibelle(),
                    errorMsg);
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
