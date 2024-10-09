package mc.gouv.xaf.back.paiement.data.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@ExtendWith(MockitoExtension.class)
@DataJpaTest
class CommandeDemandeRepositoryTest {

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

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

    @Test
    void findByDemande_PkDemandes_test() {
        DemandeBO demandeBO = createDemande("monIdentifiant");
        CommandeBO commandeBO = createCommande(100, 0, LocalDateTime.now());

        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setMontant(100);
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO);
        commandeDemandeRepository.save(commandeDemandeBO);

        List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByDemande_PkDemandes(demandeBO.getPkDemandes());
        assertThat(commandeDemandeBOList).hasSize(1);
    }

    @Test
    void findDerniereCommandeDemande() {
        DemandeBO demandeBO = createDemande("monIdentifiant");

        CommandeBO commandeBO1 = createCommande(110, 0, LocalDateTime.now().plusDays(10));
        CommandeBO commandeBO2 = createCommande(100, 0, LocalDateTime.now().minusDays(10L));
        CommandeBO commandeBO3 = createCommande(110, 0, LocalDateTime.now());

        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setMontant(110);
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO1);
        CommandeDemandeBO expected = commandeDemandeRepository.save(commandeDemandeBO);

        commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setMontant(100);
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO2);
        commandeDemandeRepository.save(commandeDemandeBO);

        commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setMontant(110);
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO3);
        commandeDemandeRepository.save(commandeDemandeBO);

        List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByDemande_PkDemandesOrderByCommande_DateCreationDesc(demandeBO.getPkDemandes());
        assertThat(commandeDemandeBOList).hasSize(3);
        assertEquals(expected.getPkCommandesDemandes(), commandeDemandeBOList.get(0).getPkCommandesDemandes());
    }

    @Test
    void findDemandeCommandeToDelete() {
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
        commandeDemandeRepository.save(commandeDemandeBO1);

        // 2: Demande terminée / 2 commandes, dont une avec la demande 3
        DemandeBO demandeBO2 = createDemande("2");
        demandeBO2.setDernierStatut(createStatut("CLOTUREE", demandeBO2, date));
        CommandeBO commandeBO2 = createCommande(220, 0, ldt);
        CommandeDemandeBO commandeDemandeBO2 = new CommandeDemandeBO();
        commandeDemandeBO2.setMontant(110);
        commandeDemandeBO2.setDemande(demandeBO2);
        commandeDemandeBO2.setCommande(commandeBO2);
        commandeDemandeRepository.save(commandeDemandeBO2);
        ldt = LocalDateTime.now().minusDays(5);
        CommandeBO commandeBO3 = createCommande(110, 110, ldt);
        CommandeDemandeBO commandeDemandeBO3 = new CommandeDemandeBO();
        commandeDemandeBO3.setMontant(110);
        commandeDemandeBO3.setDemande(demandeBO2);
        commandeDemandeBO3.setCommande(commandeBO3);
        commandeDemandeRepository.save(commandeDemandeBO3);

        // 3: Demande en cours / Même commande que la demande 2
        DemandeBO demandeBO3 = createDemande("3");
        date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        demandeBO3.setDernierStatut(createStatut("EN_COURS_TRAIT", demandeBO3, date));
        CommandeDemandeBO commandeDemandeBO4 = new CommandeDemandeBO();
        commandeDemandeBO4.setMontant(110);
        commandeDemandeBO4.setDemande(demandeBO3);
        commandeDemandeBO4.setCommande(commandeBO2);
        commandeDemandeRepository.save(commandeDemandeBO4);

        // 4: Demande rufusee mais pas à purger / Commande seule
        DemandeBO demandeBO4 = createDemande("4");
        ldt = LocalDateTime.now().plusDays(10);
        date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        demandeBO4.setDernierStatut(createStatut("REFUSEE", demandeBO4, date));
        CommandeBO commandeBO4 = createCommande(110, 0, ldt);
        CommandeDemandeBO commandeDemandeBO5 = new CommandeDemandeBO();
        commandeDemandeBO5.setMontant(110);
        commandeDemandeBO5.setDemande(demandeBO4);
        commandeDemandeBO5.setCommande(commandeBO4);
        commandeDemandeRepository.save(commandeDemandeBO5);

        // Suppression de la liaison avec les demandes
        List<String> statuts = new ArrayList<>();
        statuts.add("ANNULEE");
        statuts.add("CLOTUREE");
        statuts.add("REFUSEE");
        List<CommandeDemandeBO> elements = commandeDemandeRepository.findAllByDemande_DernierStatut_LibelleInAndDemande_DernierStatut_DateLessThan(statuts, new Date());
        assertThat(elements).hasSize(3);
    }

}
