package mc.gouv.xaf.back.paiement.service.purge;

import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    @Override
    @Transactional
    public void purgeData(List<String> statuts, int jours) {
        // On récupère les commandeDemandes dont les demandes vont être purgées
        LOGGER.info("Récupération des commandes...");
        LocalDateTime ldt = LocalDateTime.now().minusDays(jours);
        List<CommandeDemandeBO> commandeDemandeBOS = commandeDemandeRepository.findAllByDemande_DernierStatut_LibelleInAndDemande_DernierStatut_DateLessThan(statuts, Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));

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
    }
}
