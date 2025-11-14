package mc.gouv.xaf.back.paiement.service.purge;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeArticleRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.InformationFacturationRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.service.kafka.GUKafkaPaiementProducer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class PurgePaiementDataServiceImpl implements PurgePaiementDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgePaiementDataServiceImpl.class);

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;

    @Autowired
    private CommandeDemandeArticleRepository commandeDemandeArticleRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private InformationFacturationRepository informationFacturationRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private GUKafkaProducer guKafkaProducer;

    @Override
    @Transactional
    public void purgeData(List<String> statuts, int jours) {
        LOGGER.info("Récupération des commandes et demandes à purger...");

        LocalDate dateLocaleDebutPurge = LocalDate.now().minusDays(jours - 1L);
        Date dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Récupérer les CommandeDemande à purger
        List<CommandeDemandeBO> commandeDemandeBOS =
                commandeDemandeRepository.findCommandesByDernierStatutBeforeDate(statuts, dateDebutPurge);

        Set<Integer> pkCommandes = commandeDemandeBOS.stream()
                .map(cd -> cd.getCommande().getPkCommandes())
                .collect(Collectors.toSet());

        Set<Integer> idsArticles = commandeDemandeBOS.stream()
                .flatMap(commandeDemande -> commandeDemande.getCommandesDemandesArticles().stream())
                .map(CommandeDemandeArticleBO::getPkCommandesDemandesArticles)
                .collect(Collectors.toSet());

        Set<Integer> pkDemandesPurge = commandeDemandeBOS.stream()
                .map(cd -> cd.getDemande().getPkDemandes())
                .collect(Collectors.toSet());

        // Supprimer commande demande articles
        LOGGER.info("Suppression des liaisons Commande Demande Articles...");
        commandeDemandeArticleRepository.deleteByPkCommandesDemandesArticlesIn(idsArticles);

        // Supprimer les liaisons Commande <--> Demande des demandes purgées
        LOGGER.info("Suppression des liaisons Commande Demande...");
        commandeDemandeRepository.deleteByDemande_PkDemandesIn(pkDemandesPurge);

        // Supprimer les entités liées aux demandes purgées
        LOGGER.info("Suppression de l'historique de paiement...");
        paiementHistoriqueRepository.deleteByFkDemandes_PkDemandesIn(pkDemandesPurge);

        // Supprimer les commandes opérations
        LOGGER.info("Suppression des commandes opérations...");
        commandeOperationRepository.deleteByDemande_PkDemandesIn(pkDemandesPurge);

        // Identifier les commandes encore utilisées
        Set<Integer> commandesStillReferenced =
                commandeRepository.findCommandesStillReferenced(pkCommandes, pkDemandesPurge);

        // Supprimer uniquement les commandes totalement orphelines
        Set<Integer> commandesToDelete = new HashSet<>(pkCommandes);
        commandesToDelete.removeAll(commandesStillReferenced);

        if (!commandesToDelete.isEmpty()) {
            LOGGER.info("Suppression des informations de facturation...");
            informationFacturationRepository.deleteByCommande_PkCommandesIn(commandesToDelete);

            LOGGER.info("Suppression des moyens de paiement...");
            moyenPaiementRepository.deleteByCommande_PkCommandesIn(commandesToDelete);

            LOGGER.info("Suppression des commandes orphelines...");
            List<CommandeBO> commandes = commandeRepository.findAllById(commandesToDelete);
            commandeRepository.deleteAll(commandes);
        }

        for (Integer pkDemande : pkDemandesPurge) {
            demandesRepository.findById(pkDemande).ifPresent(demande -> {
                guKafkaProducer.sendSuppressionPaiementMessage(
                        demande.getIdentifiant(),
                        String.valueOf(demande.getFkAccess().getUsagerId())
                );
            });
        }
    }
}
