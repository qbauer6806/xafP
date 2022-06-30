package mc.gouv.xaf.back.stc.data.dao;


import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.stc.data.entity.CommandeBO;
import mc.gouv.xaf.back.stc.data.entity.CommandeDemandeBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@DataJpaTest
public class CommandeDemandeRepositoryTest {

    @Autowired
    CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    CommandeRepository commandeRepository;

    @Autowired
    DemandesRepository demandesRepository;

    @Test
    public void findByDemande_PkDemandes_test() {
        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setMontant(100);
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandesRepository.save(demandeBO);
        commandeDemandeBO.setDemande(demandeBO);
        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontant(100);
        commandeBO.setDateCreation(LocalDateTime.now());
        commandeDemandeBO.setCommande(commandeBO);
        commandeRepository.save(commandeBO);
        commandeDemandeRepository.save(commandeDemandeBO);

        List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByDemande_PkDemandes(1);
        assertThat(commandeDemandeBOList).hasSize(1);

    }


}