package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class MoyenPaiementRepositoryTest {

    @Autowired
    MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    CommandeRepository commandeRepository;


    @Test
    public void findByCommande_Id_test() {

        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontant(100);
        commandeBO.setDateCreation(LocalDateTime.now());
         commandeBO =  commandeRepository.save(commandeBO);
        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setCommande(commandeBO);
        moyenPaiementBO.setPkMoyenPaiement("maRef");
        moyenPaiementRepository.save(moyenPaiementBO);
        MoyenPaiementBO moyenPaiementBO2 = new MoyenPaiementBO();
        moyenPaiementBO2.setCommande(commandeBO);
        moyenPaiementBO2.setPkMoyenPaiement("maRef2");
        moyenPaiementRepository.save(moyenPaiementBO2);

        List<MoyenPaiementBO> moyenPaiements = moyenPaiementRepository.findByCommande_PkCommande(commandeBO.getPkCommande());
        assertThat(moyenPaiements).hasSize(2);

    }

}