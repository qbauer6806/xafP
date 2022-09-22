package mc.gouv.xaf.back.paiement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
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
    private CommandeOperationRepository commandeOperationRepository;

    @Before
    public void cleanData() {
        commandeOperationRepository.deleteAll();
        moyenPaiementRepository.deleteAll();
        commandeDemandeRepository.deleteAll();
        commandeRepository.deleteAll();
        demandesRepository.deleteAll();
    }


    @Transactional
    @Test
    public void captureOk() throws Exception {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        demandeBO = demandesRepository.save(demandeBO);

        CommandeDemandeBO commandeDemandeBO = new CommandeDemandeBO();
        commandeDemandeBO.setDemande(demandeBO);

        MoyenPaiementBO moyenPaiementBO = new MoyenPaiementBO();
        moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
        moyenPaiementBO.setPkMoyensPaiements("maRef");
        moyenPaiementBO.setMoyenPaiementType(MoyenPaiementTypeEnum.DIFFERE);
        moyenPaiementBO.setMoyenPaiementStatut(MoyenPaiementStatutEnum.VALIDE);
        moyenPaiementBO = moyenPaiementRepository.save(moyenPaiementBO);

        CommandeBO commandeBO = new CommandeBO();
        commandeBO.setMontantInitial(100);
        commandeBO.setMontantRestant(100);
        commandeBO.setMontantDejaCapture(0);
        commandeBO.setDateCreation(LocalDateTime.now());
        commandeBO.setMoyenPaiement(moyenPaiementBO);
        List<CommandeDemandeBO> commandeDemandeBOList = new ArrayList<>();
        commandeDemandeBOList.add(commandeDemandeBO);
        commandeBO.setCommandesDemandes(commandeDemandeBOList);
        commandeBO.setOperations(new ArrayList<>());
        commandeRepository.save(commandeBO);

        DemandeDTO demandeDTO = new DemandeDTO();
        ContenuTestDTO contenuTestDTO = new ContenuTestDTO();
        Paiement paiement = new Paiement();
        paiement.setTableau(new Tableau[]{new Tableau("objet", "80")});
        contenuTestDTO.setPaiement(paiement);
        contenuTestDTO.setTitre(new Titre("123456"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.valueToTree(contenuTestDTO);
        demandeDTO.setContenu(contenu);

        CommandeDTO commandeDTO = CommandeTransformer.bo2Dto(commandeBO);

        CommandeOperationDTO resutat = captureService.capture(commandeDTO, demandeDTO);
        assertThat(resutat.getMontant()).isEqualTo(80.0);
        assertThat(resutat.getOperationType()).isEqualTo(OperationTypeEnum.DEBIT.name());
        assertThat(resutat.getOperationStatut()).isEqualTo(OperationStatutEnum.ACCEPTEE.name());
        assertThat(resutat.getNumeroFacture()).isEqualTo("facture001");
    }

}
