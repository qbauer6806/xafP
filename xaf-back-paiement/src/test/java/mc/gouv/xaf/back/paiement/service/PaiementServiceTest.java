package mc.gouv.xaf.back.paiement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.paiement.data.dao.*;
import mc.gouv.xaf.back.paiement.data.entity.*;
import mc.gouv.xaf.back.paiement.dto.*;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.mock.DemandeStatutEnum;
import mc.gouv.xaf.shared.stc.MoyenPaiementDTO;
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
    private DemandesRepository demandesRepository;

    @Autowired
    private PaiementService paiementService;

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

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Before
    public void cleanData() {
        paiementHistoriqueRepository.deleteAll();
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
        PaiementDTO paiementDTO = paiementService.create(demandesId, langue, 1, true);

        assertThat(paiementDTO.getReference()).hasSize(12);
        assertThat(paiementDTO.getMontant()).isEqualTo("160.0EUR");
    }

    @Test
    public void updateOk() throws IOException, SAXException {

        DemandeBO demandeBO = new DemandeBO();

        ContenuTestDTO contenuTestDTO = new ContenuTestDTO();
        Paiement paiement = new Paiement();
        paiement.setTableau(new Tableau[]{new Tableau("objet", "80")});
        contenuTestDTO.setPaiement(paiement);
        contenuTestDTO.setTitre(new Titre("123456"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.valueToTree(contenuTestDTO);

        AccessBO access = new AccessBO();
        access.setDemarcheId("PERMC");
        access.setContenu("{\"CGU\":true}");
        access.setUsagerId(1);
        access.setDateCreation(new Date());
        access.setDateDerModif(new Date());
        accessRepository.save(access);

        demandeBO.setContenu(contenu.toString());
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setFkAccess(access);
        demandeBO.setUsagerPrenom("Jon");
        demandeBO.setUsagerNom("Doe");

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
        demandeBO2.setFkAccess(access);
        demandeBO2.setUsagerPrenom("Jon");
        demandeBO2.setUsagerNom("Doe");

        DemandesStatutsBO dernierStatut2 = new DemandesStatutsBO();
        dernierStatut2.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut2.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut2);
        demandeBO2.setDernierStatut(dernierStatut2);

        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = "" + demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
        PaiementDTO paiementDTO = paiementService.create(demandesId, langue, 1, true);

        demandesRepository.findAll().stream()
                .map(DemandeBO::getDernierStatut)
                .map(DemandesStatutsBO::getLibelle)
                .collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name()));

        String status = "paiement";
        MoyenPaiementDTO moyenPaiementDTO = new MoyenPaiementDTO();
        moyenPaiementDTO.setReference(paiementDTO.getReference());
        moyenPaiementDTO.setCodeRetour(status);
        moyenPaiementDTO.setVld("1223");
        paiementService.updateStatus(moyenPaiementDTO);

        Optional<MoyenPaiementBO> optionalMoyenPaiementBO = moyenPaiementRepository.findById(paiementDTO.getReference());
        assertThat(optionalMoyenPaiementBO).isPresent();
        MoyenPaiementBO moyenPaiementBO = optionalMoyenPaiementBO.get();
        assertThat(moyenPaiementBO.getMoyenPaiementStatut()).isEqualTo(MoyenPaiementStatutBO.VALIDE);

        demandesRepository.findAll().stream().map(DemandeBO::getDernierStatut).map(DemandesStatutsBO::getLibelle).collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
        paiementHistoriqueRepository.findAll().forEach(histo -> {
            assertThat(histo.getContenu()).isEqualTo("Usager Jon Doe : Effectue une empreinte bancaire");
            assertThat(histo.getStatut()).isEqualTo(PaiementStatutEnum.EMPREINTE_VALIDE.name());
        });
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
        moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutBO.VALIDE);
        moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
        moyenPaiementBO.setPkMoyenPaiement("maRef");
        moyenPaiementRepository.save(moyenPaiementBO);
        MoyenPaiementBO moyenPaiementBO2 = new MoyenPaiementBO();
        moyenPaiementBO2.setCommande(commandeBO);
        moyenPaiementBO2.setMontantInitial(155);
        moyenPaiementBO2.setMoyenPaiementStatut(MoyenPaiementStatutBO.VALIDE);
        moyenPaiementBO2.setPkMoyenPaiement("maRef2");
        moyenPaiementBO2.setDateLimite(LocalDateTime.MAX);
        moyenPaiementRepository.save(moyenPaiementBO2);


        Optional<MoyenPaiementBO> optionalMoyenPaiementBO = paiementService.getMoyenPaiement(demandeBO.getPkDemandes());
        MoyenPaiementBO moyenPaiement = optionalMoyenPaiementBO.get();
        assertThat(moyenPaiement.getMontantInitial()).isEqualTo(155);
        assertThat(moyenPaiement.getPkMoyenPaiement()).isEqualTo("maRef2");
    }

}
