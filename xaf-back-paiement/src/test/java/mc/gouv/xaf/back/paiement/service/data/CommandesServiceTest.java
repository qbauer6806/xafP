package mc.gouv.xaf.back.paiement.service.data;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommandesServiceTest {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private CommandesService commandesService;

    @Before
    public void cleanData() {
        commandeRepository.deleteAll();
        demandesRepository.deleteAll();
    }

    @Test
    @Transactional
    public void getCommandeOk() {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        Set<DemandesDataBO> demandesDataBOS = new HashSet<>();
        DemandesDataBO demandesDataBO = new DemandesDataBO();
        demandesDataBO.setKey(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name());
        demandesDataBO.setValue("maRef");
        demandesDataBOS.add(demandesDataBO);
        demandeBO.setData(demandesDataBOS);
        demandeBO = demandesRepository.save(demandeBO);

        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.VALIDE);
        moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
        moyenPaiementBO.setPkMoyensPaiements("maRef");

        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setDemande(demandeBO);

        CommandeDemandeArticleBO articleBO = new CommandeDemandeArticleBO();
        articleBO.setCommandeDemande(commandeDemandeBO);
        commandeDemandeBO.setCommandesDemandesArticles(Collections.singletonList(articleBO));

        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontantInitial(122);
        commandeBO.setDateCreation(LocalDateTime.now());
        commandeBO.setMoyenPaiement(moyenPaiementBO);
        commandeBO.setCommandesDemandes(Collections.singletonList(commandeDemandeBO));
        commandeDemandeBO.setCommande(commandeBO);
        moyenPaiementBO.setCommande(commandeBO);
        commandeRepository.save(commandeBO);

        CommandeDTO commandeDTO = commandesService.getDerniereCommande(demandeBO.getPkDemandes());
        assertThat(commandeDTO.getMontantInitial()).isEqualTo(122);
        assertThat(commandeDTO.getMoyenPaiement().getPkMoyenPaiements()).isEqualTo("maRef");
    }

}
