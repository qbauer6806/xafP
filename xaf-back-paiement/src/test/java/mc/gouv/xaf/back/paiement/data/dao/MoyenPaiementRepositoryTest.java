package mc.gouv.xaf.back.paiement.data.dao;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class MoyenPaiementRepositoryTest {

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeRepository commandeRepository;

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
