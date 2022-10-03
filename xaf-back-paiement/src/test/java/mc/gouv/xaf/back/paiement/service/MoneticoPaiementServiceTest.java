package mc.gouv.xaf.back.paiement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesDataRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.*;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.dto.*;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.mock.DemandeStatutEnum;
import mc.gouv.xaf.back.paiement.service.itg.MoneticoPaiementService;
import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MoneticoPaiementServiceTest {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private MoneticoPaiementService moneticoPaiementService;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private DemandesDataRepository demandesDataRepository;

    @Autowired
    private PaiementSecurityService paiementSecurityService;

    @Before
    public void cleanData() {
        paiementHistoriqueRepository.deleteAll();
        commandeOperationRepository.deleteAll();
        moyenPaiementRepository.deleteAll();
        commandeDemandeRepository.deleteAll();
        commandeRepository.deleteAll();
        demandesRepository.deleteAll();
        demandesDataRepository.deleteAll();
    }

    @Test
    public void createOk() {
        DemandeBO demandeBO = new DemandeBO();
        Set<DemandesDataBO> demandesDataBOS = new HashSet<>();
        DemandesDataBO demandesDataBO = new DemandesDataBO();
        demandesDataBO.setKey(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name());
        demandesDataBO.setValue("123456");
        demandesDataBOS.add(demandesDataBO);
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setData(demandesDataBOS);
        demandeBO = demandesRepository.save(demandeBO);

        DemandeBO demandeBO2 = new DemandeBO();
        Set<DemandesDataBO> demandesDataBOS2 = new HashSet<>();
        DemandesDataBO demandesDataBO2 = new DemandesDataBO();
        demandesDataBO2.setKey(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name());
        demandesDataBO2.setValue("123456");
        demandesDataBOS2.add(demandesDataBO2);
        demandeBO2.setContenu("contenu");
        demandeBO2.setCanal("canal");
        demandeBO2.setIdentifiant("monIdentifiant");
        demandeBO2.setDateCreation(new Date());
        demandeBO2.setDateDerModif(new Date());
        demandeBO2.setData(demandesDataBOS2);
        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = "" + demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
        PaiementDTO paiementDTO = moneticoPaiementService.create(demandesId, langue, 1, true);

        assertThat(paiementDTO.getReference()).hasSize(12);
        assertThat(paiementDTO.getMontant()).isEqualTo("160.0EUR");

        Optional<MoyenPaiementBO> moyenPaiementOptional = moyenPaiementRepository.findById(paiementDTO.getReference());
        if (moyenPaiementOptional.isPresent()) {
            MoyenPaiementBO moyenPaiementBO = moyenPaiementOptional.get();
            assertThat(moyenPaiementBO.getLangue()).isEqualTo("FR");
        } else {
            fail("Le moyen de paiement avec la référence " + paiementDTO.getReference() + " n'a pas été généré !");
        }

    }

    @Test
    public void updateOk() throws InterruptedException {

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
        PaiementDTO paiementDTO = moneticoPaiementService.create(demandesId, langue, 1, true);

        demandesRepository.findAll().stream()
                .map(DemandeBO::getDernierStatut)
                .map(DemandesStatutsBO::getLibelle)
                .collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name()));

        String status = "paiement";
        MoneticoResponseDTO moneticoResponseDTO = new MoneticoResponseDTO();
        moneticoResponseDTO.setReference(paiementDTO.getReference());
        moneticoResponseDTO.setCodeRetour(status);
        moneticoResponseDTO.setVld("1223");
        moneticoResponseDTO.setMac(paiementSecurityService.getHmacStringInterfaceRetour(moneticoResponseDTO));
        String result = moneticoPaiementService.updateStatus(moneticoResponseDTO);
        assertThat(result).isEqualTo("0");

        TimeUnit.SECONDS.sleep(1);

        Optional<MoyenPaiementBO> optionalMoyenPaiementBO = moyenPaiementRepository.findById(paiementDTO.getReference());
        assertThat(optionalMoyenPaiementBO).isPresent();
        MoyenPaiementBO moyenPaiementBO = optionalMoyenPaiementBO.get();
        assertThat(moyenPaiementBO.getMoyenPaiementStatut()).isEqualTo(MoyenPaiementStatutEnum.VALIDE);

        demandesRepository.findAll().stream().map(DemandeBO::getDernierStatut).map(DemandesStatutsBO::getLibelle).collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
        paiementHistoriqueRepository.findAll().forEach(histo -> {
            assertThat(histo.getContenu()).isEqualTo("Usager Jon Doe : Effectue une empreinte bancaire");
            assertThat(histo.getStatut()).isEqualTo(PaiementStatutEnum.EMPREINTE_VALIDE.name());
        });
    }

    @Test
    public void updateKoRefarenceTest() {
        String status = "paiement";
        MoneticoResponseDTO moneticoResponseDTO = new MoneticoResponseDTO();
        moneticoResponseDTO.setReference("AZERTYUIOPQS");
        moneticoResponseDTO.setCodeRetour(status);
        moneticoResponseDTO.setVld("1223");
        moneticoResponseDTO.setMac(paiementSecurityService.getHmacStringInterfaceRetour(moneticoResponseDTO));
        try {
            moneticoPaiementService.updateStatus(moneticoResponseDTO);
            fail("updateStatus doit renvoyer une exception");
        } catch (DemarchesServiceException e) {
            assertThat(e.getMessage()).isEqualTo("Aucun paiement portant la référence AZERTYUIOPQS n'a été trouvé.");
            assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @Ignore
    public void updateKoMACTest() {
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
        PaiementDTO paiementDTO = moneticoPaiementService.create(demandesId, langue, 1, true);

        demandesRepository.findAll().stream()
                .map(DemandeBO::getDernierStatut)
                .map(DemandesStatutsBO::getLibelle)
                .collect(Collectors.toList()).forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name()));

        String status = "paiement";
        MoneticoResponseDTO moneticoResponseDTO = new MoneticoResponseDTO();
        moneticoResponseDTO.setReference(paiementDTO.getReference());
        moneticoResponseDTO.setCodeRetour(status);
        moneticoResponseDTO.setVld("1223");
        String mac = paiementSecurityService.getHmacStringInterfaceRetour(moneticoResponseDTO);
        moneticoResponseDTO.setMac("mauvais mac");
        String result = moneticoPaiementService.updateStatus(moneticoResponseDTO);
        assertThat(result).isEqualTo("1\n" + mac);
    }

}
