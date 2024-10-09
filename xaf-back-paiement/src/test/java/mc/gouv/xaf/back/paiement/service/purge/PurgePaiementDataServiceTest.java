package mc.gouv.xaf.back.paiement.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@ExtendWith(MockitoExtension.class)
class PurgePaiementDataServiceTest {

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Autowired
    private PurgePaiementDataServiceImpl purgeCommandesService;

    private DemandeBO createDemande(String indentifiant) {
        ObjectMapper mapper = new ObjectMapper();
        DemandeBO demandeBO = new DemandeBO();
        try {
            demandeBO.setContenu(mapper.readTree("contenu"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant(indentifiant);
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        return demandesRepository.save(demandeBO);
    }

    private CommandeBO createCommande(int montant, int capture, LocalDateTime date) {
        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontantInitial(montant);
        commandeBO.setMontantRestant(montant - capture);
        commandeBO.setMontantDejaCapture(capture);
        commandeBO.setDateCreation(date);
        return commandeRepository.save(commandeBO);
    }

    private DemandesStatutsBO createStatut(String libelle, DemandeBO demandeBO, Date date) {
        DemandesStatutsBO statutsBO = new DemandesStatutsBO();
        statutsBO.setLibelle(libelle);
        statutsBO.setFkDemandes(demandeBO);
        statutsBO.setDate(date);
        return demandesStatutsRepository.save(statutsBO);
    }

    @BeforeEach
    void cleanData() {
        commandeDemandeRepository.deleteAll();
        commandeRepository.deleteAll();
        demandesStatutsRepository.deleteAll();
        demandesRepository.deleteAll();
    }

    @Test
    @Transactional
    void purgeCommandeUneSeuleCommandeAvecDemandeTermineeTest() {
        DemandeBO demandeBO = createDemande("1");
        LocalDateTime ldt = LocalDateTime.now().minusDays(10);
        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        demandeBO.setDernierStatut(createStatut("ANNULEE", demandeBO, date));
        CommandeBO commandeBO = createCommande(110, 0, ldt);
        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setMontant(110);
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO);
        commandeBO.setCommandesDemandes(Collections.singletonList(commandeDemandeBO));
        commandeDemandeRepository.save(commandeDemandeBO);

        // Suppression des commandes, la table doit être vide
        List<String> statuts = new ArrayList<>();
        statuts.add("ANNULEE");
        statuts.add("CLOTUREE");
        statuts.add("REFUSEE");
        purgeCommandesService.purgeData(statuts, 1);

        List<CommandeDemandeBO> commandeDemandeBOS = commandeDemandeRepository.findAll();
        assertThat(commandeDemandeBOS).isEmpty();
        List<CommandeBO> commandeBOS = commandeRepository.findAll();
        assertThat(commandeBOS).isEmpty();
    }

    @Test
    @Transactional
    void purgeCommandeUneSeuleCommandeAvecDeuxDemandesDontUneTerminee() {
        LocalDateTime ldt = LocalDateTime.now().minusDays(5);
        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        DemandeBO demandeBO1 = createDemande("1");
        demandeBO1.setDernierStatut(createStatut("CLOTUREE", demandeBO1, date));
        DemandeBO demandeBO2 = createDemande("2");
        demandeBO2.setDernierStatut(createStatut("EN_COURS_TRAIT", demandeBO2, date));
        CommandeBO commandeBO = createCommande(220, 0, ldt);
        CommandeDemandeBO commandeDemandeBO1 = new CommandeDemandeBO();
        commandeDemandeBO1.setMontant(110);
        commandeDemandeBO1.setDemande(demandeBO1);
        commandeDemandeBO1.setCommande(commandeBO);
        List<CommandeDemandeBO> commandeDemandeBOS = new ArrayList<>();
        commandeDemandeBOS.add(commandeDemandeBO1);
        commandeDemandeBO1 = commandeDemandeRepository.save(commandeDemandeBO1);
        CommandeDemandeBO commandeDemandeBO2 = new CommandeDemandeBO();
        commandeDemandeBO2.setMontant(110);
        commandeDemandeBO2.setDemande(demandeBO2);
        commandeDemandeBO2.setCommande(commandeBO);
        commandeDemandeBOS.add(commandeDemandeBO2);
        commandeBO.setCommandesDemandes(commandeDemandeBOS);
        commandeDemandeBO2 = commandeDemandeRepository.save(commandeDemandeBO2);

        // Suppression des commandes, la table doit être vide
        List<String> statuts = new ArrayList<>();
        statuts.add("ANNULEE");
        statuts.add("CLOTUREE");
        statuts.add("REFUSEE");
        purgeCommandesService.purgeData(statuts, 1);

        commandeDemandeBOS = commandeDemandeRepository.findAll();
        assertThat(commandeDemandeBOS).hasSize(2);
        assertThat(commandeDemandeBOS.get(0).getPkCommandesDemandes()).isEqualTo(commandeDemandeBO1.getPkCommandesDemandes());
        assertThat(commandeDemandeBOS.get(0).getDemande()).isNull();
        assertThat(commandeDemandeBOS.get(1).getPkCommandesDemandes()).isEqualTo(commandeDemandeBO2.getPkCommandesDemandes());
        assertThat(commandeDemandeBOS.get(1).getDemande()).isNotNull();
        assertThat(commandeDemandeBOS.get(1).getDemande().getPkDemandes()).isEqualTo(demandeBO2.getPkDemandes());
        List<CommandeBO> commandeBOS = commandeRepository.findAll();
        assertThat(commandeBOS).hasSize(1);
        assertThat(commandeBOS.getFirst().getPkCommandes()).isEqualTo(commandeBO.getPkCommandes());
    }

    @Test
    @Transactional
    void purgeCommandeUneSeuleCommandeAvecDeuxDemandesDontUneDejaPurgee() {
        LocalDateTime ldt = LocalDateTime.now().minusDays(5);
        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        DemandeBO demandeBO = createDemande("2");
        demandeBO.setDernierStatut(createStatut("ANNULEE", demandeBO, date));
        CommandeBO commandeBO = createCommande(220, 0, ldt);
        CommandeDemandeBO commandeDemandeBO1 = new CommandeDemandeBO();
        commandeDemandeBO1.setMontant(110);
        commandeDemandeBO1.setDemande(null);
        commandeDemandeBO1.setCommande(commandeBO);
        List<CommandeDemandeBO> commandeDemandeBOS = new ArrayList<>();
        commandeDemandeBOS.add(commandeDemandeBO1);
        commandeDemandeRepository.save(commandeDemandeBO1);
        CommandeDemandeBO commandeDemandeBO2 = new CommandeDemandeBO();
        commandeDemandeBO2.setMontant(110);
        commandeDemandeBO2.setDemande(demandeBO);
        commandeDemandeBO2.setCommande(commandeBO);
        commandeDemandeBOS.add(commandeDemandeBO2);
        commandeBO.setCommandesDemandes(commandeDemandeBOS);
        commandeDemandeRepository.save(commandeDemandeBO2);

        // Suppression des commandes, la table doit être vide
        List<String> statuts = new ArrayList<>();
        statuts.add("ANNULEE");
        statuts.add("CLOTUREE");
        statuts.add("REFUSEE");
        purgeCommandesService.purgeData(statuts, 1);

        commandeDemandeBOS = commandeDemandeRepository.findAll();
        assertThat(commandeDemandeBOS).isEmpty();
        List<CommandeBO> commandeBOS = commandeRepository.findAll();
        assertThat(commandeBOS).isEmpty();
    }

    @Test
    @Transactional
    void purgeCommandeTest() {
        // 1: Demande annulée / Commande seule
        DemandeBO demandeBO1 = createDemande("1");
        LocalDateTime ldt = LocalDateTime.now().minusDays(10);
        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        demandeBO1.setDernierStatut(createStatut("ANNULEE", demandeBO1, date));
        CommandeBO commandeBO1 = createCommande(110, 0, ldt);
        CommandeDemandeBO commandeDemandeBO1 = new CommandeDemandeBO();
        commandeDemandeBO1.setMontant(110);
        commandeDemandeBO1.setDemande(demandeBO1);
        commandeDemandeBO1.setCommande(commandeBO1);
        commandeBO1.setCommandesDemandes(Collections.singletonList(commandeDemandeBO1));
        commandeDemandeRepository.save(commandeDemandeBO1);

        // 2: Demande terminée / 2 commandes, dont une avec la demande 3
        DemandeBO demandeBO2 = createDemande("2");
        demandeBO2.setDernierStatut(createStatut("CLOTUREE", demandeBO2, date));
        CommandeBO commandeBO2 = createCommande(220, 0, ldt);
        CommandeDemandeBO commandeDemandeBO2 = new CommandeDemandeBO();
        commandeDemandeBO2.setMontant(110);
        commandeDemandeBO2.setDemande(demandeBO2);
        commandeDemandeBO2.setCommande(commandeBO2);
        List<CommandeDemandeBO> commandeDemandeBOS = new ArrayList<>();
        commandeDemandeBOS.add(commandeDemandeBO2);
        commandeDemandeBO2 = commandeDemandeRepository.save(commandeDemandeBO2);
        ldt = LocalDateTime.now().minusDays(5);
        CommandeBO commandeBO3 = createCommande(110, 110, ldt);
        CommandeDemandeBO commandeDemandeBO3 = new CommandeDemandeBO();
        commandeDemandeBO3.setMontant(110);
        commandeDemandeBO3.setDemande(demandeBO2);
        commandeDemandeBO3.setCommande(commandeBO3);
        commandeBO3.setCommandesDemandes(Collections.singletonList(commandeDemandeBO3));
        commandeDemandeRepository.save(commandeDemandeBO3);

        // 3: Demande en cours / Même commande que la demande 2
        DemandeBO demandeBO3 = createDemande("3");
        date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        demandeBO3.setDernierStatut(createStatut("EN_COURS_TRAIT", demandeBO3, date));
        CommandeDemandeBO commandeDemandeBO4 = new CommandeDemandeBO();
        commandeDemandeBO4.setMontant(110);
        commandeDemandeBO4.setDemande(demandeBO3);
        commandeDemandeBO4.setCommande(commandeBO2);
        commandeDemandeBOS.add(commandeDemandeBO4);
        commandeBO2.setCommandesDemandes(commandeDemandeBOS);
        commandeDemandeBO4 = commandeDemandeRepository.save(commandeDemandeBO4);

        // 4: Demande rufusee mais pas à purger / Commande seule
        DemandeBO demandeBO4 = createDemande("4");
        ldt = LocalDateTime.now().plusDays(10);
        date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        demandeBO4.setDernierStatut(createStatut("REFUSEE", demandeBO3, date));
        CommandeBO commandeBO4 = createCommande(110, 0, ldt);
        CommandeDemandeBO commandeDemandeBO5 = new CommandeDemandeBO();
        commandeDemandeBO5.setMontant(110);
        commandeDemandeBO5.setDemande(demandeBO4);
        commandeDemandeBO5.setCommande(commandeBO4);
        commandeBO4.setCommandesDemandes(Collections.singletonList(commandeDemandeBO5));
        commandeDemandeBO5 = commandeDemandeRepository.save(commandeDemandeBO5);

        // Suppression des commandes, après on ne doit trouver plus que 2 commandes
        List<String> statuts = new ArrayList<>();
        statuts.add("ANNULEE");
        statuts.add("CLOTUREE");
        statuts.add("REFUSEE");
        purgeCommandesService.purgeData(statuts, 1);

        commandeDemandeBOS = commandeDemandeRepository.findAll();
        assertThat(commandeDemandeBOS).hasSize(3);
        assertThat(commandeDemandeBOS.get(0).getPkCommandesDemandes()).isEqualTo(commandeDemandeBO2.getPkCommandesDemandes());
        assertThat(commandeDemandeBOS.get(0).getDemande()).isNull();
        assertThat(commandeDemandeBOS.get(1).getPkCommandesDemandes()).isEqualTo(commandeDemandeBO4.getPkCommandesDemandes());
        assertThat(commandeDemandeBOS.get(1).getDemande()).isNotNull();
        assertThat(commandeDemandeBOS.get(2).getPkCommandesDemandes()).isEqualTo(commandeDemandeBO5.getPkCommandesDemandes());
        assertThat(commandeDemandeBOS.get(2).getDemande()).isNotNull();
        List<CommandeBO> commandeBOS = commandeRepository.findAll();
        assertThat(commandeBOS).hasSize(2);
        assertThat(commandeBOS.get(0).getPkCommandes()).isEqualTo(commandeBO2.getPkCommandes());
        assertThat(commandeBOS.get(1).getPkCommandes()).isEqualTo(commandeBO4.getPkCommandes());
    }
}
