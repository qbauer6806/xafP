package mc.gouv.xaf.back.paiement.service.scheduling;

import mc.gouv.xaf.back.paiement.service.purge.PurgePaiementDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.back.service.scheduling.PurgeDemandesSchedulingJob;
import mc.gouv.xaf.shared.annotations.TypeDePurge;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.COURRIER;
import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.GUICHET_PHYSIQUE;
import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.GUICHET_VIRTUEL;

/**
 * Job permettant d'appeler le service de purge des demandes.
 * <br>
 * Il est nécessaire de péciser la liste des statuts des demandes à purger.
 * <br>
 * Modification spécifique pour PERMC, on ajoute une purge des données de paiements avant la purge des demandes.
 *
 * @author mboutelier.ext
 */
@DisallowConcurrentExecution
@TypeDePurge("paiement")
@Component
public class PurgeDemandesPaiementsSchedulingJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDemandesPaiementsSchedulingJob.class);

    @Autowired
    private PurgeDemandesService purgeDemandesService;

    @Autowired
    private PurgePaiementDataService purgePaiementDataService;

    @Autowired
    private PropertiesService propertiesService;

    private static final String DELAI_PURGE_EN_JOURS = "DELAI_PURGE_EN_JOURS";
    private static final int DELAI_PAR_DEFAUT_PURGE = 1095; // 3 ans de purge par défaut
    private static final String ACTIVATION_PURGE = "ACTIVATION_PURGE";

    @Override
    @Transactional
    public void execute(JobExecutionContext jobExecutionContext) {
        PropertiesDTO activationPurge = propertiesService.getProperty(ACTIVATION_PURGE);
        if (activationPurge != null && Boolean.parseBoolean(activationPurge.getValue())) {
            try {
                List<String> statuts = getStatuts(jobExecutionContext);
                Integer delaiPurge = getDelaiPurge();
                LOGGER.info(
                        "PURGE: Suppression des commandes dont les demandes sont dans les états finaux à moins de {} jours",
                        delaiPurge);
                purgePaiementDataService.purgeData(statuts, delaiPurge);
                LOGGER.info("PURGE: Suppression des demandes dans les états finaux à moins de {} jours", delaiPurge);
                purgeDemandesService.purgerDemandesDansStatuts(statuts, delaiPurge);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la purge des demandes", e);
            }
        } else {
            LOGGER.info(
                    "PURGE: La fonctionnalité de la purge est désactivée, changez la propriété ACTIVATION_PURGE pour activer.");
        }
    }

    /**
     * @return La liste conteant les clés des statuts à purger
     */
    private List<String> getStatuts(JobExecutionContext jobExecutionContext) {
        List<String> statuts = new ArrayList<>();
        // Récupération de la liste des statuts à purger dans le contexte du job detail
        Object statutsJob = jobExecutionContext.getJobDetail().getJobDataMap().get("statuts");
        if (statutsJob instanceof List) {
            statuts = (List<String>) statutsJob;
        }
        return statuts;
    }

    private Integer getDelaiPurge() {
        PropertiesDTO delaiPurgeProperty = propertiesService.getProperty(DELAI_PURGE_EN_JOURS);
        return delaiPurgeProperty != null ? Integer.parseInt(delaiPurgeProperty.getValue()) : DELAI_PAR_DEFAUT_PURGE;
    }
}
