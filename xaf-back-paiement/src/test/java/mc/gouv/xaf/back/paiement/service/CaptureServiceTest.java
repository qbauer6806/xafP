package mc.gouv.xaf.back.paiement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.data.dao.*;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementTypeEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeTransformer;
import mc.gouv.xaf.back.paiement.dto.*;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaptureServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureServiceTest.class);

    @Autowired
    private CaptureService captureService;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private CommandeDemandeArticleRepository commandeDemandeArticleRepository;

    @Autowired
    private CommandeOperationRepository commandeOperationRepository;

    @Before
    public void cleanData() {
        commandeOperationRepository.deleteAll();
        moyenPaiementRepository.deleteAll();
        commandeDemandeArticleRepository.deleteAll();
        commandeDemandeRepository.deleteAll();
        commandeRepository.deleteAll();
        demandesRepository.deleteAll();
    }


    @Transactional
    @Test
    public void captureOk() {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("{\"donnee\":{\"demandeur\":{\"titre\":\"0\",\"nom\":\"Test\",\"prenom\":\"Test\",\"email\":\"test.ext@gouv.mc\"}},\"contact\":{\"telephone\":{\"indicatif\":\"t377\",\"numero\":\"98981234\"}},\"titulaire\":{\"adresse\":{\"ligne1\":\"1\",\"ligne2\":\"\",\"ligne3\":\"\",\"codePostal\":\"98000\",\"ville\":\"Monaco\",\"pays\":\"MC\"},\"cartemonegasque\":{\"expiration\":\"2022-09-22T00:00:00+02:00\",\"numero\":\"12345\"},\"cartesejour\":{\"numero\":null,\"categorie\":null,\"delivrance\":null,\"expiration\":null},\"pioupasseport\":\"PI\",\"datenaissance\":\"2022-09-22T00:00:00+02:00\",\"declarantouinon\":\"NO\",\"titre\":\"0\",\"prenom\":\"Test\",\"nom\":\"Test\",\"monegasque\":\"MC\",\"mandatairerlsociete\":null,\"representantlegal\":null,\"nomusage\":null,\"passeportnumero\":null,\"dateexpiration\":null},\"titre\":{\"categorie\":{\"b\":true},\"validitepermis\":\"2022-09-22T00:00:00+02:00\",\"numeropermis\":\"12345\",\"paysdelivrance\":\"FR\",\"permisinternational\":\"OUI\",\"langue\":null},\"paiement\":{\"tableau\":[{\"objet\":\"PERMIS\",\"montant\":80.0},{\"objet\":\"PERMIS_INTERNATIONAL\",\"montant\":30.0}],\"total\":\"110,00 €\"},\"declaration3\":\"DECLARATION_3\",\"declarations2\":\"DECLARATION2\",\"declarations1\":null,\"titreautrecateg\":null}");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO = demandesRepository.save(demandeBO);
        LOGGER.info("Created [ demandeBO {}] ", demandeBO);

        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setPkMoyensPaiements("maRef");

        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontantInitial(100);
        commandeBO.setMontantRestant(100);
        commandeBO.setMontantDejaCapture(0);
        commandeBO.setDateCreation(LocalDateTime.now());
        commandeBO.setMoyenPaiement(moyenPaiementBO);
        commandeBO.setCommandesDemandes(new ArrayList<>());
        commandeBO = commandeRepository.save(commandeBO);
        LOGGER.info("Created [ commandeBO {}] ", commandeBO);

        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setDemande(demandeBO);
        commandeDemandeBO.setCommande(commandeBO);
        commandeDemandeBO.setMontant(80.0);
        commandeDemandeBO.setCommandesDemandesArticles(new ArrayList<>());
        commandeDemandeBO = commandeDemandeRepository.save(commandeDemandeBO);
        LOGGER.info("Created [ commandeDemandeBO {}] ", commandeDemandeBO);

        List<CommandeDemandeArticleBO> articles = new ArrayList<>();
        CommandeDemandeArticleBO articleBO = new CommandeDemandeArticleBO();
        articleBO.setCodeTarif("P1");
        articleBO.setMontant(80.0);
        articleBO.setCommandeDemande(commandeDemandeBO);
        articleBO = commandeDemandeArticleRepository.save(articleBO);
        LOGGER.info("Created [ articleBO {}] ", articleBO);
        articles.add(articleBO);
        commandeDemandeBO.setCommandesDemandesArticles(articles);
        commandeDemandeBO = commandeDemandeRepository.save(commandeDemandeBO);
        LOGGER.info("Updated [ commandeDemandeBO {}] ", commandeDemandeBO);

        commandeBO.getCommandesDemandes().add(commandeDemandeBO);
        commandeBO.setOperations(new ArrayList<>());
        commandeBO = commandeRepository.save(commandeBO);
        LOGGER.info("Updated [ commandeBO {}] ", commandeBO);

        moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
        moyenPaiementBO.setMoyenPaiementType(MoyenPaiementTypeEnum.DIFFERE);
        moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.VALIDE);
        moyenPaiementBO.setCommande(commandeBO);
        moyenPaiementRepository.save(moyenPaiementBO);
        LOGGER.info("Created [ moyenPaiementBO {}] ", moyenPaiementBO);

        DemandeDTO demandeDTO = new DemandeDTO();
        demandeDTO.setPkDemandes(demandeBO.getPkDemandes());
        ContenuTestDTO contenuTestDTO = new ContenuTestDTO();
        Paiement paiement = new Paiement();
        paiement.setTableau(new Tableau[]{new Tableau("objet", "80")});
        contenuTestDTO.setPaiement(paiement);
        contenuTestDTO.setTitre(new Titre("123456"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.valueToTree(contenuTestDTO);
        demandeDTO.setContenu(contenu);

        CommandeDTO commandeDTO = CommandeTransformer.bo2Dto(commandeBO);

        CommandeOperationDTO resultat = captureService.capture(commandeDTO, demandeDTO);
        assertThat(resultat.getMontant()).isEqualTo(80.0);
        assertThat(resultat.getOperationType()).isEqualTo(OperationTypeEnum.DEBIT.name());
        assertThat(resultat.getOperationStatut()).isEqualTo(OperationStatutEnum.ACCEPTEE.name());
        assertThat(resultat.getNumeroFacture()).isEqualTo("facture001");
    }

}
