package mc.gouv.xaf.back.paiement.service.purge;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.InformationFacturationRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.service.kafka.GUKafkaPaiementProducer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
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

@Service
@EnableScheduling
public class PurgePaiementDataServiceImpl implements PurgePaiementDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgePaiementDataServiceImpl.class);

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private InformationFacturationRepository informationFacturationRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private GUKafkaPaiementProducer guKafkaPaiementProducer;

    @Override
    @Transactional
    public void purgeData(List<String> statuts, int jours) {
        // On récupère les commandeDemandes dont les demandes vont être purgées
        LOGGER.info("Récupération des commandes...");

        LocalDate dateLocaleDebutPurge = LocalDate.now().minusDays(jours - 1L);
        Date dateDebutPurge = Date.from(dateLocaleDebutPurge.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<CommandeDemandeBO> commandeDemandeBOS = commandeDemandeRepository.findCommandesByDernierStatutBeforeDate(statuts, dateDebutPurge);

        // On vire la liaison avec les demandes, et on récupère les ids des commandes et des demandes associées
        Set<Integer> pkCommmandes = new HashSet<>();
        Set<Integer> pkDemandes = new HashSet<>();
        commandeDemandeBOS.forEach(c -> {
            pkCommmandes.add(c.getCommande().getPkCommandes());
            pkDemandes.add(c.getDemande().getPkDemandes());
            c.setDemande(null);
        });

        LOGGER.info("Suppression de la liaison Commande Demande...");
        commandeDemandeRepository.saveAll(commandeDemandeBOS);

        // On supprime les historiques de paiements liés aux demandes
        LOGGER.info("Suppression de l'historique de paiement...");
        paiementHistoriqueRepository.deleteByFkDemandes_PkDemandesIn(pkDemandes);

        // Suppression informations de facturations
        LOGGER.info("Suppression des informations de facturation...");
        informationFacturationRepository.deleteByCommande_PkCommandesIn(pkCommmandes);

        // Suppressions moyen de paiement
        LOGGER.info("Suppression du moyen de paiement...");
        moyenPaiementRepository.deleteByCommande_PkCommandesIn(pkCommmandes);

        // On supprime les commandes dont il n'existe plus de liaisons avec des demandes
        LOGGER.info("Suppression des commandes...");
        List<CommandeBO> commandeBOS = commandeRepository.findAllById(pkCommmandes);
        Set<CommandeBO> commmandesToDelete = new HashSet<>();
        for (CommandeBO commandeBO : commandeBOS) {
            Hibernate.initialize(commandeBO.getCommandesDemandes());
            int i = 0;
            for (; i < commandeBO.getCommandesDemandes().size(); i++) {
                if (commandeBO.getCommandesDemandes().get(i).getDemande() != null) {
                    break;
                }
            }
            if (i == commandeBO.getCommandesDemandes().size()) {
                commmandesToDelete.add(commandeBO);
            }
        }
        commandeRepository.deleteAll(commmandesToDelete);
        // Suppression de l'historique de paiement dans Kafka et donc mon guichet
        for (Integer pkDemande : pkDemandes) {
            demandesRepository.findById(pkDemande).ifPresent(demande -> {
                guKafkaPaiementProducer.sendSuppressionPaiementMessage(demande.getIdentifiant(),
                        String.valueOf(demande.getFkAccess().getUsagerId()));
            });
        }
    }
}
