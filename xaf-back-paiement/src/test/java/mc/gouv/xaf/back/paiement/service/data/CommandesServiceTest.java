package mc.gouv.xaf.back.paiement.service.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

@Disabled
@ExtendWith(MockitoExtension.class)
class CommandesServiceTest {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private CommandesService commandesService;

    @BeforeEach
    void cleanData() {
        commandeRepository.deleteAll();
        demandesRepository.deleteAll();
    }

    @Test
    @Transactional
    void getCommandeOk() {
        ObjectMapper mapper = new ObjectMapper();
        DemandeBO demandeBO = new DemandeBO();
        try {
            demandeBO.setContenu(mapper.readTree("contenu"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
        // TODO moyenPaiementBO.setDateLimite(LocalDateTime.MIN);
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
        // TODO assertThat(commandeDTO.getMoyenPaiement().getPkMoyenPaiements()).isEqualTo("maRef");
    }

}
