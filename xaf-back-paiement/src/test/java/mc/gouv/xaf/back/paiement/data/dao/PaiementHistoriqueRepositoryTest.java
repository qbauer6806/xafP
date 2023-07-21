package mc.gouv.xaf.back.paiement.data.dao;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PaiementHistoriqueRepositoryTest {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    private DemandeBO createDemande() {
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setContenu("contenu");
        demandeBO.setCanal("canal");
        demandeBO.setIdentifiant("monIdentifiant");
        demandeBO.setDateCreation(new Date());
        demandeBO.setDateDerModif(new Date());
        return demandesRepository.save(demandeBO);
    }

    private PaiementHistoriqueBO createHistorique(DemandeBO demandeBO) {
        PaiementHistoriqueBO histo = new PaiementHistoriqueBO();
        histo.setFkDemandes(demandeBO);
        histo.setDate(Timestamp.valueOf(LocalDateTime.now()));
        histo.setStatut("STATUT");
        histo.setContenu("contenu");
        return paiementHistoriqueRepository.save(histo);
    }

    @Test
    @Transactional
    public void findHistoriqueByFkDemandesTest() {
        DemandeBO demandeBO = createDemande();
        PaiementHistoriqueBO paiementHistoriqueBO = createHistorique(demandeBO);
        Set<Integer> pkDemandes = new HashSet<>();
        pkDemandes.add(demandeBO.getPkDemandes());
        List<PaiementHistoriqueBO> result = paiementHistoriqueRepository.findByFkDemandes_PkDemandesIn(pkDemandes);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPkHistorique()).isEqualTo(paiementHistoriqueBO.getPkHistorique());
    }

    @Test
    @Transactional
    public void findHistoriqueByFkDemandesLesHistoPlusBonTest() {
        DemandeBO demandeBO1 = createDemande();
        PaiementHistoriqueBO paiementHistoriqueBO1 = createHistorique(demandeBO1);
        Set<Integer> pkDemandes = new HashSet<>();
        pkDemandes.add(demandeBO1.getPkDemandes());
        DemandeBO demandeBO2 = createDemande();
        createHistorique(demandeBO2);
        List<PaiementHistoriqueBO> result = paiementHistoriqueRepository.findByFkDemandes_PkDemandesIn(pkDemandes);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPkHistorique()).isEqualTo(paiementHistoriqueBO1.getPkHistorique());
    }

    @Test
    @Transactional
    public void deleteHistoriqueTest() {
        DemandeBO demandeBO = createDemande();
        createHistorique(demandeBO);
        Set<Integer> pkDemandes = new HashSet<>();
        pkDemandes.add(demandeBO.getPkDemandes());
        paiementHistoriqueRepository.deleteByFkDemandes_PkDemandesIn(pkDemandes);
        List<PaiementHistoriqueBO> result = paiementHistoriqueRepository.findAll();
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    public void deleteHistoriquePlusBonTest() {
        DemandeBO demandeBO1 = createDemande();
        createHistorique(demandeBO1);
        Set<Integer> pkDemandes = new HashSet<>();
        pkDemandes.add(demandeBO1.getPkDemandes());
        DemandeBO demandeBO2 = createDemande();
        PaiementHistoriqueBO paiementHistoriqueBO = createHistorique(demandeBO2);
        paiementHistoriqueRepository.deleteByFkDemandes_PkDemandesIn(pkDemandes);
        List<PaiementHistoriqueBO> result = paiementHistoriqueRepository.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPkHistorique()).isEqualTo(paiementHistoriqueBO.getPkHistorique());
    }

}
