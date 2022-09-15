package mc.gouv.xaf.back.paiement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;
import mc.gouv.xaf.back.paiement.data.transformer.MoyenPaiementTransformer;
import mc.gouv.xaf.back.paiement.dto.*;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaptureServiceTest {

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
    public void captureOk() throws Exception {
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

        DemandeDTO demandeDTO = new DemandeDTO();
        ContenuTestDTO contenuTestDTO = new ContenuTestDTO();
        Paiement paiement = new Paiement();
        paiement.setTableau(new Tableau[]{new Tableau("objet", "80")});
        contenuTestDTO.setPaiement(paiement);
        contenuTestDTO.setTitre(new Titre("123456"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.valueToTree(contenuTestDTO);
        demandeDTO.setContenu(contenu);

        MoyenPaiementDTO moyenPaiementDTO = MoyenPaiementTransformer.bo2Dto(moyenPaiementBO);

        OperationDTO resutat = captureService.capture(moyenPaiementDTO, demandeDTO);
        assertThat(resutat.getMontant()).isEqualTo(80.0);
        assertThat(resutat.getOperationType()).isEqualTo(OperationTypeEnum.DEBIT);
        assertThat(resutat.getOperationStatut()).isEqualTo(OperationStatutEnum.ACCEPTEE);
        assertThat(resutat.getNumeroFacture()).isEqualTo("facture001");
    }

}
