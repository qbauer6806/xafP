package mc.gouv.xaf.back.paiement.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Assert;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesConfigRepository;
import mc.gouv.xaf.back.data.dao.DemandesDataRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeArticleRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.dto.ContenuTestDTO;
import mc.gouv.xaf.back.paiement.dto.Paiement;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.dto.Tableau;
import mc.gouv.xaf.back.paiement.dto.Titre;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.mock.DemandeStatutEnum;
import mc.gouv.xaf.back.paiement.service.itg.MoneticoPaiementService;
import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Disabled
@ExtendWith(SpringExtension.class)
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
    private CommandeDemandeArticleRepository commandeDemandeArticleRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;

    @Autowired
    private AccessRepository accessRepository;

  @Autowired
  private DemandesConfigRepository demandesConfigRepository;

  @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private DemandesDataRepository demandesDataRepository;

    @Autowired
    private PaiementSecurityService paiementSecurityService;

    @BeforeEach
    public void cleanData() {
        paiementHistoriqueRepository.deleteAll();
        commandeOperationRepository.deleteAll();
        moyenPaiementRepository.deleteAll();
        commandeDemandeArticleRepository.deleteAll();
        commandeDemandeRepository.deleteAll();
        commandeRepository.deleteAll();
        demandesDataRepository.deleteAll();
        demandesRepository.deleteAll();
        accessRepository.deleteAll();
    }

    @Test
    @Transactional
    public void createOk() {
        ObjectMapper mapper = new ObjectMapper();
        AccessBO accessBO = new AccessBO();
        accessBO.setActive(true);
        accessBO.setContenu("{\"CGU\":true}");
        accessBO.setDateCreation(new Date());
        accessBO.setDateDerModif(new Date());
        accessBO.setUsagerId(1);
        accessRepository.save(accessBO);

        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setFkAccess(accessBO);
        Set<DemandesDataBO> demandesDataBOS = new HashSet<>();
        DemandesDataBO demandesDataBO = new DemandesDataBO();
        demandesDataBO.setKey(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name());
        demandesDataBO.setValue("123456");
        demandesDataBOS.add(demandesDataBO);
        try {
            demandeBO.setContenu(mapper.readTree("{\"donnee\":{\"demandeur\":{\"titre\":\"0\",\"nom\":\"Test\",\"prenom\":\"Test\",\"email\":\"test.ext@gouv.mc\"}},\"contact\":{\"telephone\":{\"indicatif\":\"t377\",\"numero\":\"98981234\"}},\"titulaire\":{\"adresse\":{\"ligne1\":\"1\",\"ligne2\":\"\",\"ligne3\":\"\",\"codePostal\":\"98000\",\"ville\":\"Monaco\",\"pays\":\"MC\"},\"cartemonegasque\":{\"expiration\":\"2022-09-22T00:00:00+02:00\",\"numero\":\"12345\"},\"cartesejour\":{\"numero\":null,\"categorie\":null,\"delivrance\":null,\"expiration\":null},\"pioupasseport\":\"PI\",\"datenaissance\":\"2022-09-22T00:00:00+02:00\",\"declarantouinon\":\"NO\",\"titre\":\"0\",\"prenom\":\"Test\",\"nom\":\"Test\",\"monegasque\":\"MC\",\"mandatairerlsociete\":null,\"representantlegal\":null,\"nomusage\":null,\"passeportnumero\":null,\"dateexpiration\":null},\"titre\":{\"categorie\":{\"b\":true},\"validitepermis\":\"2022-09-22T00:00:00+02:00\",\"numeropermis\":\"12345\",\"paysdelivrance\":\"FR\",\"permisinternational\":\"OUI\",\"langue\":null},\"declaration3\":\"DECLARATION_3\",\"declarations2\":\"DECLARATION2\",\"declarations1\":null,\"titreautrecateg\":null}"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setData(demandesDataBOS);
      DemandeConfigBO configBO = new DemandeConfigBO();
      configBO.setBuildId("1695305061010");
      try {
        configBO.setContenu(mapper.readTree("{}"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      demandesConfigRepository.save(configBO);
      demandeBO.setConfig(configBO);

      demandeBO = demandesRepository.save(demandeBO);

        DemandeBO demandeBO2 = new DemandeBO();
        demandeBO2.setFkAccess(accessBO);
        Set<DemandesDataBO> demandesDataBOS2 = new HashSet<>();
        DemandesDataBO demandesDataBO2 = new DemandesDataBO();
        demandesDataBO2.setKey(PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT_REFERENCE.name());
        demandesDataBO2.setValue("123456");
        demandesDataBOS2.add(demandesDataBO2);
        try {
            demandeBO2.setContenu(mapper.readTree("{\"donnee\":{\"demandeur\":{\"titre\":\"0\",\"nom\":\"Test\",\"prenom\":\"Test\",\"email\":\"test.ext@gouv.mc\"}},\"contact\":{\"telephone\":{\"indicatif\":\"t377\",\"numero\":\"98981234\"}},\"titulaire\":{\"adresse\":{\"ligne1\":\"1\",\"ligne2\":\"\",\"ligne3\":\"\",\"codePostal\":\"98000\",\"ville\":\"Monaco\",\"pays\":\"MC\"},\"cartemonegasque\":{\"expiration\":\"2022-09-22T00:00:00+02:00\",\"numero\":\"12345\"},\"cartesejour\":{\"numero\":null,\"categorie\":null,\"delivrance\":null,\"expiration\":null},\"pioupasseport\":\"PI\",\"datenaissance\":\"2022-09-22T00:00:00+02:00\",\"declarantouinon\":\"NO\",\"titre\":\"0\",\"prenom\":\"Test\",\"nom\":\"Test\",\"monegasque\":\"MC\",\"mandatairerlsociete\":null,\"representantlegal\":null,\"nomusage\":null,\"passeportnumero\":null,\"dateexpiration\":null},\"titre\":{\"categorie\":{\"b\":true},\"validitepermis\":\"2022-09-22T00:00:00+02:00\",\"numeropermis\":\"12345\",\"paysdelivrance\":\"FR\",\"permisinternational\":\"OUI\",\"langue\":null},\"declaration3\":\"DECLARATION_3\",\"declarations2\":\"DECLARATION2\",\"declarations1\":null,\"titreautrecateg\":null}"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO2.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO2.setIdentifiant("monIdentifiant");
        demandeBO2.setDateCreation(new Date());
        demandeBO2.setDateDerModif(new Date());
        demandeBO2.setData(demandesDataBOS2);
      DemandeConfigBO configBO2 = new DemandeConfigBO();
      configBO2.setBuildId("1695305061010");
      try {
        configBO2.setContenu(mapper.readTree("{}"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      demandesConfigRepository.save(configBO2);
      demandeBO2.setConfig(configBO2);
        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
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

        List<CommandeBO> commandeBOS = commandeRepository.findAll();
        Assert.notEmpty(commandeBOS);
        CommandeBO commandeBO = commandeBOS.get(0);
        Hibernate.initialize(commandeBO.getCommandesDemandes());
        List<CommandeDemandeBO> commandeDemandeBOS = commandeBO.getCommandesDemandes();
        Assert.notEmpty(commandeDemandeBOS);
        for (CommandeDemandeBO cdBO : commandeDemandeBOS) {
            Hibernate.initialize(cdBO.getCommandesDemandesArticles());
            List<CommandeDemandeArticleBO> articleBOS = cdBO.getCommandesDemandesArticles();
            Assert.notEmpty(articleBOS);
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
        access.setContenu("{\"CGU\":true}");
        access.setUsagerId(1);
        access.setDateCreation(new Date());
        access.setDateDerModif(new Date());
        access.setActive(true);
        accessRepository.save(access);

        demandeBO.setContenu(contenu);
        demandeBO.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setFkAccess(access);
        DemandesUsagersBO usager = new DemandesUsagersBO();
        usager.setPrenom("Jon");
        usager.setNom("Doe");
        demandeBO.setUsager(usager);

        DemandesStatutsBO dernierStatut = new DemandesStatutsBO();
        dernierStatut.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut);
        demandeBO.setDernierStatut(dernierStatut);
      DemandeConfigBO configBO = new DemandeConfigBO();
      configBO.setBuildId("1695305061010");
      try {
        configBO.setContenu(mapper.readTree("{}"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      demandesConfigRepository.save(configBO);
      demandeBO.setConfig(configBO);
        demandeBO = demandesRepository.save(demandeBO);

        DemandeBO demandeBO2 = new DemandeBO();
        try {
            demandeBO2.setContenu(mapper.readTree("{\"donnee\":{\"demandeur\":{\"titre\":\"0\",\"nom\":\"Test\",\"prenom\":\"Test\",\"email\":\"test.ext@gouv.mc\"}},\"contact\":{\"telephone\":{\"indicatif\":\"t377\",\"numero\":\"98981234\"}},\"titulaire\":{\"adresse\":{\"ligne1\":\"1\",\"ligne2\":\"\",\"ligne3\":\"\",\"codePostal\":\"98000\",\"ville\":\"Monaco\",\"pays\":\"MC\"},\"cartemonegasque\":{\"expiration\":\"2022-09-22T00:00:00+02:00\",\"numero\":\"12345\"},\"cartesejour\":{\"numero\":null,\"categorie\":null,\"delivrance\":null,\"expiration\":null},\"pioupasseport\":\"PI\",\"datenaissance\":\"2022-09-22T00:00:00+02:00\",\"declarantouinon\":\"NO\",\"titre\":\"0\",\"prenom\":\"Test\",\"nom\":\"Test\",\"monegasque\":\"MC\",\"mandatairerlsociete\":null,\"representantlegal\":null,\"nomusage\":null,\"passeportnumero\":null,\"dateexpiration\":null},\"titre\":{\"categorie\":{\"b\":true},\"validitepermis\":\"2022-09-22T00:00:00+02:00\",\"numeropermis\":\"12345\",\"paysdelivrance\":\"FR\",\"permisinternational\":\"OUI\",\"langue\":null},\"paiement\":{\"tableau\":[{\"objet\":\"PERMIS\",\"montant\":80.0},{\"objet\":\"PERMIS_INTERNATIONAL\",\"montant\":30.0}],\"total\":\"110,00 €\"},\"declaration3\":\"DECLARATION_3\",\"declarations2\":\"DECLARATION2\",\"declarations1\":null,\"titreautrecateg\":null}"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO2.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO2.setIdentifiant("monIdentifiant");
        demandeBO2.setDateCreation(new Date());
        demandeBO2.setDateDerModif(new Date());
        demandeBO2.setFkAccess(access);
        DemandesUsagersBO usager2 = new DemandesUsagersBO();
        usager2.setPrenom("Jon");
        usager2.setNom("Doe");
        demandeBO2.setUsager(usager2);

        DemandesStatutsBO dernierStatut2 = new DemandesStatutsBO();
        dernierStatut2.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut2.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut2);
        demandeBO2.setDernierStatut(dernierStatut2);
      DemandeConfigBO configBO2 = new DemandeConfigBO();
      configBO2.setBuildId("1695305061010");
      try {
        configBO2.setContenu(mapper.readTree("{}"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      demandesConfigRepository.save(configBO2);
      demandeBO2.setConfig(configBO2);

        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
        PaiementDTO paiementDTO = moneticoPaiementService.create(demandesId, langue, 1, true);

        demandesRepository.findAll().stream()
                .map(DemandeBO::getDernierStatut)
                .map(DemandesStatutsBO::getLibelle)
                .toList().forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name()));

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

        demandesRepository.findAll().stream().map(DemandeBO::getDernierStatut).map(DemandesStatutsBO::getLibelle).toList()
                .forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
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
        access.setContenu("{\"CGU\":true}");
        access.setUsagerId(1);
        access.setDateCreation(new Date());
        access.setDateDerModif(new Date());
        access.setActive(true);
        accessRepository.save(access);

        demandeBO.setContenu(contenu);
        demandeBO.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setFkAccess(access);
        DemandesUsagersBO usager = new DemandesUsagersBO();
        usager.setPrenom("Jon");
        usager.setNom("Doe");
        demandeBO.setUsager(usager);

        DemandesStatutsBO dernierStatut = new DemandesStatutsBO();
        dernierStatut.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut);
        demandeBO.setDernierStatut(dernierStatut);
      DemandeConfigBO configBO = new DemandeConfigBO();
      configBO.setBuildId("1695305061010");
      try {
        configBO.setContenu(mapper.readTree("{}"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      demandesConfigRepository.save(configBO);
      demandeBO.setConfig(configBO);
        demandeBO = demandesRepository.save(demandeBO);

        DemandeBO demandeBO2 = new DemandeBO();
        try {
            demandeBO2.setContenu(mapper.readTree("{\"donnee\":{\"demandeur\":{\"titre\":\"0\",\"nom\":\"Test\",\"prenom\":\"Test\",\"email\":\"test.ext@gouv.mc\"}},\"contact\":{\"telephone\":{\"indicatif\":\"t377\",\"numero\":\"98981234\"}},\"titulaire\":{\"adresse\":{\"ligne1\":\"1\",\"ligne2\":\"\",\"ligne3\":\"\",\"codePostal\":\"98000\",\"ville\":\"Monaco\",\"pays\":\"MC\"},\"cartemonegasque\":{\"expiration\":\"2022-09-22T00:00:00+02:00\",\"numero\":\"12345\"},\"cartesejour\":{\"numero\":null,\"categorie\":null,\"delivrance\":null,\"expiration\":null},\"pioupasseport\":\"PI\",\"datenaissance\":\"2022-09-22T00:00:00+02:00\",\"declarantouinon\":\"NO\",\"titre\":\"0\",\"prenom\":\"Test\",\"nom\":\"Test\",\"monegasque\":\"MC\",\"mandatairerlsociete\":null,\"representantlegal\":null,\"nomusage\":null,\"passeportnumero\":null,\"dateexpiration\":null},\"titre\":{\"categorie\":{\"b\":true},\"validitepermis\":\"2022-09-22T00:00:00+02:00\",\"numeropermis\":\"12345\",\"paysdelivrance\":\"FR\",\"permisinternational\":\"OUI\",\"langue\":null},\"paiement\":{\"tableau\":[{\"objet\":\"PERMIS\",\"montant\":80.0},{\"objet\":\"PERMIS_INTERNATIONAL\",\"montant\":30.0}],\"total\":\"110,00 €\"},\"declaration3\":\"DECLARATION_3\",\"declarations2\":\"DECLARATION2\",\"declarations1\":null,\"titreautrecateg\":null}"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO2.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO2.setIdentifiant("monIdentifiant");
        demandeBO2.setDateCreation(new Date());
        demandeBO2.setDateDerModif(new Date());
        demandeBO2.setFkAccess(access);
        DemandesUsagersBO usager2 = new DemandesUsagersBO();
        usager2.setPrenom("Jon");
        usager2.setNom("Doe");
        demandeBO2.setUsager(usager2);

        DemandesStatutsBO dernierStatut2 = new DemandesStatutsBO();
        dernierStatut2.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut2.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut2);
      DemandeConfigBO configBO2 = new DemandeConfigBO();
      configBO2.setBuildId("1695305061010");
      try {
        configBO2.setContenu(mapper.readTree("{}"));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      demandesConfigRepository.save(configBO2);
      demandeBO2.setConfig(configBO2);
        demandeBO2.setDernierStatut(dernierStatut2);

        demandeBO2 = demandesRepository.save(demandeBO2);
        String langue = "FR";
        String demandesId = demandeBO.getPkDemandes() + "," + demandeBO2.getPkDemandes();
        PaiementDTO paiementDTO = moneticoPaiementService.create(demandesId, langue, 1, true);

        demandesRepository.findAll().stream()
                .map(DemandeBO::getDernierStatut)
                .map(DemandesStatutsBO::getLibelle)
                .toList().forEach(libelle -> assertThat(libelle).isEqualTo(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name()));

        String status = "paiement";
        MoneticoResponseDTO moneticoResponseDTO = new MoneticoResponseDTO();
        moneticoResponseDTO.setReference(paiementDTO.getReference());
        moneticoResponseDTO.setCodeRetour(status);
        moneticoResponseDTO.setVld("1223");
        moneticoResponseDTO.setMac("mauvais mac");
        String result = moneticoPaiementService.updateStatus(moneticoResponseDTO);
        assertThat(result).isEqualTo("1");
    }

    @Test
    public void testWhenDemandeNotAssignedUsagerThenThrowException() {
        DemandeBO demandeBO = new DemandeBO();

        AccessBO access = new AccessBO();
        access.setContenu("{\"CGU\":true}");
        access.setUsagerId(1);
        access.setDateCreation(new Date());
        access.setDateDerModif(new Date());
        access.setActive(true);
        accessRepository.save(access);

        ObjectMapper mapper = new ObjectMapper();
        try {
            demandeBO.setContenu(mapper.readTree("VIDE"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setFkAccess(access);
        DemandesUsagersBO usager = new DemandesUsagersBO();
        usager.setPrenom("Jon");
        usager.setNom("Doe");
        demandeBO.setUsager(usager);

        DemandesStatutsBO dernierStatut = new DemandesStatutsBO();
        dernierStatut.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut);
        demandeBO.setDernierStatut(dernierStatut);
        demandeBO = demandesRepository.save(demandeBO);

        String langue = "FR";
        String demandesId = demandeBO.getPkDemandes().toString();

        assertThrows(
                DemarchesServiceException.class,
                () -> {
                    // When
                    moneticoPaiementService.create(demandesId, langue, 2, true);
                });
    }
    @Test
    public void
    testWhenUsagerDemandeNotActiveThenThrowException() {
        DemandeBO demandeBO = new DemandeBO();

        AccessBO access = new AccessBO();
        access.setContenu("{\"CGU\":true}");
        access.setUsagerId(1);
        access.setDateCreation(new Date());
        access.setDateDerModif(new Date());
        access.setActive(false);
        accessRepository.save(access);

        ObjectMapper mapper = new ObjectMapper();
        try {
            demandeBO.setContenu(mapper.readTree("VIDE"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        demandeBO.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO.setFkAccess(access);
        DemandesUsagersBO usager = new DemandesUsagersBO();
        usager.setPrenom("Jon");
        usager.setNom("Doe");
        demandeBO.setUsager(usager);

        DemandesStatutsBO dernierStatut = new DemandesStatutsBO();
        dernierStatut.setLibelle(DemandeStatutEnum.EN_ATTENTE_DE_PAIEMENT.name());
        dernierStatut.setDate(new Date());
        demandesStatutsRepository.save(dernierStatut);
        demandeBO.setDernierStatut(dernierStatut);
        demandeBO = demandesRepository.save(demandeBO);

        String langue = "FR";
        String demandesId = demandeBO.getPkDemandes().toString();

        assertThrows(
                DemarchesServiceException.class,
                () -> {
                    // When
                    moneticoPaiementService.create(demandesId, langue, 1, true);
                });
    }
}
