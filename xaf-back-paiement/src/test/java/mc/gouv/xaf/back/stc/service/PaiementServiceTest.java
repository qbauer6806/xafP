package mc.gouv.xaf.back.stc.service;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.stc.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.stc.data.dao.CommandeRepository;
import mc.gouv.xaf.back.stc.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.stc.data.entity.CommandeBO;
import mc.gouv.xaf.back.stc.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaiementServiceTest {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    DemandesRepository demandesRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Test
    public void createOk() {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO = demandesRepository.save(demandeBO);

        DemandeBO demandeBO2 = new DemandeBO();
        demandeBO2.setContenu("contenu");
        demandeBO2.setCanal("canal");
        demandeBO2.setIdentifiant("monIdentifiant");
        demandeBO2.setDateCreation(new Date());
        demandeBO2.setDateDerModif(new Date());
        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = "" + demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
        PaiementDTO paiementDTO = paiementService.create(demandesId, langue, 1);

        commandeRepository.findAll();
        moyenPaiementRepository.findAll();
        commandeDemandeRepository.findAll();
    }

    @Test
    public void updateOk() {
        String reference = null;
        String status = null;
        paiementService.updateStatus(reference, status);
    }


    @Test
    public void getMoyenPaiementOk() {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO = demandesRepository.save(demandeBO);
        System.out.println(demandeBO.getPkDemandes());
        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontant(100);
        commandeBO.setDateCreation(LocalDateTime.now());
        commandeRepository.save(commandeBO);

        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO);
        commandeDemandeRepository.save(commandeDemandeBO);

        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setCommande(commandeBO);
        moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
        moyenPaiementBO.setPkMoyenPaiement("maRef");
        moyenPaiementRepository.save(moyenPaiementBO);
        MoyenPaiementBO moyenPaiementBO2 = new MoyenPaiementBO();
        moyenPaiementBO2.setCommande(commandeBO);
        moyenPaiementBO2.setPkMoyenPaiement("maRef2");
        moyenPaiementBO2.setDateLimite(LocalDateTime.MAX);
        moyenPaiementRepository.save(moyenPaiementBO2);


        Optional<MoyenPaiementBO> optionalMoyenPaiementBO = paiementService.getMoyenPaiement(demandeBO.getPkDemandes());
        optionalMoyenPaiementBO.get();
    }

    @Test
    public void captureOk() throws IOException {
        MoyenPaiementBO moyenPaiementBO = null;
        String resutat = paiementService.capture(moyenPaiementBO, 1);
    }

}
