package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementStatutBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationStatutBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationTypeBO;
import mc.gouv.xaf.back.paiement.mock.DemandeStatutEnum;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Before
    public void cleanData() {
        operationRepository.deleteAll();
        moyenPaiementRepository.deleteAll();
        commandeDemandeRepository.deleteAll();
        commandeRepository.deleteAll();
        demandesRepository.deleteAll();
    }

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

        assertThat(paiementDTO.getReference()).hasSize(12);
        assertThat(paiementDTO.getMontant()).isEqualTo("160.0EUR");
    }

    @Test
    public void updateOk() throws IOException, SAXException {

        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());


        DemandesStatutsBO dernierStatut = new DemandesStatutsBO();
        dernierStatut.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut);
        demandeBO.setDernierStatut(dernierStatut);
        demandeBO = demandesRepository.save(demandeBO);

        DemandeBO demandeBO2 = new DemandeBO();
        demandeBO2.setContenu("contenu");
        demandeBO2.setCanal("canal");
        demandeBO2.setIdentifiant("monIdentifiant");
        demandeBO2.setDateCreation(new Date());
        demandeBO2.setDateDerModif(new Date());

        DemandesStatutsBO dernierStatut2 = new DemandesStatutsBO();
        dernierStatut2.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut2.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut2);
        demandeBO2.setDernierStatut(dernierStatut2);

        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = "" + demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
        PaiementDTO paiementDTO = paiementService.create(demandesId, langue, 1);

        demandesRepository.findAll().stream()
                .map(DemandeBO::getDernierStatut)
                .map(DemandesStatutsBO::getLibelle)
                .collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name()));

        String status = "paiement";
        paiementService.updateStatus(paiementDTO.getReference(), status);

        Optional<MoyenPaiementBO> optionalMoyenPaiementBO = moyenPaiementRepository.findById(paiementDTO.getReference());
        assertThat(optionalMoyenPaiementBO).isPresent();
        MoyenPaiementBO moyenPaiementBO = optionalMoyenPaiementBO.get();
        assertThat(moyenPaiementBO.getMoyenPaiementStatut()).isEqualTo(MoyenPaiementStatutBO.VALIDE);

        demandesRepository.findAll().stream().map(DemandeBO::getDernierStatut).map(DemandesStatutsBO::getLibelle).collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
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
        moyenPaiementBO.setMontantInitial(122);
        moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
        moyenPaiementBO.setPkMoyenPaiement("maRef");
        moyenPaiementRepository.save(moyenPaiementBO);
        MoyenPaiementBO moyenPaiementBO2 = new MoyenPaiementBO();
        moyenPaiementBO2.setCommande(commandeBO);
        moyenPaiementBO2.setMontantInitial(155);
        moyenPaiementBO2.setPkMoyenPaiement("maRef2");
        moyenPaiementBO2.setDateLimite(LocalDateTime.MAX);
        moyenPaiementRepository.save(moyenPaiementBO2);


        Optional<MoyenPaiementBO> optionalMoyenPaiementBO = paiementService.getMoyenPaiement(demandeBO.getPkDemandes());
        MoyenPaiementBO moyenPaiement = optionalMoyenPaiementBO.get();
        assertThat(moyenPaiement.getMontantInitial()).isEqualTo(155);
        assertThat(moyenPaiement.getPkMoyenPaiement()).isEqualTo("maRef2");
    }

    @Test
    public void captureOk() throws IOException {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO = demandesRepository.save(demandeBO);

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
        String resutat = paiementService.capture(moyenPaiementBO, 1);
        OperationBO operationBo = operationRepository.findAll().iterator().next();
        assertThat(operationBo.getMontant()).isEqualTo(-80.0);
        assertThat(operationBo.getOperationType()).isEqualTo(OperationTypeBO.DEBIT);
        assertThat(operationBo.getOperationStatut()).isEqualTo(OperationStatutBO.REFUSEE);
        assertThat(resutat).isEqualTo(operationBo.getPkOperation());
    }

}
