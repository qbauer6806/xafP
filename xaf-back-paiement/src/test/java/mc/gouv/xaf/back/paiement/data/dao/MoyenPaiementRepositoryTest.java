package mc.gouv.xaf.back.paiement.data.dao;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;

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
        commandeBO.setDateCreation(LocalDateTime.now());
        commandeBO =  commandeRepository.save(commandeBO);
        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setCommande(commandeBO);
        moyenPaiementBO.setPkMoyensPaiements("maRef1");
        moyenPaiementRepository.save(moyenPaiementBO);

        MoyenPaiementBO moyenPaiement = moyenPaiementRepository.findByCommande_PkCommandes(commandeBO.getPkCommandes());
        assertEquals(commandeBO, moyenPaiement.getCommande());
    }

}