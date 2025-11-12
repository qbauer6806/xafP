package mc.gouv.xaf.back.paiement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.dao.CommandeOperationRepository;
import mc.gouv.xaf.back.paiement.data.dao.InformationFacturationRepository;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.data.entity.InformationFacturationBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.DebitDTO;
import mc.gouv.xaf.back.paiement.service.PaiementsDataProvider;
import mc.gouv.xaf.back.paiement.service.TableauPaiementService;
import mc.gouv.xaf.back.paiement.transformer.MwpaymtTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.AdresseFacturationDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.enums.UsagerTypeEnum;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class PaiementServiceImplTest {

    @Mock
    private BrouillonsService brouillonsService;

    @Mock
    private DemandesRepository demandesRepository;

    @Mock
    private CommandeDemandeRepository commandeDemandeRepository;

    @Mock
    private MoyenPaiementRepository moyenPaiementRepository;

    @Mock
    private InformationFacturationRepository infoFacturationRepository;

    @Mock
    private MwpaymtTransformer mwpaymtTransformer;

    @Mock
    private PaiementsDataProvider paiementsDataProvider;

    @Mock
    private CommandeOperationRepository commandeOperationRepository;

    @Mock
    private DemandesService demandesService;

    @Mock
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Mock
    private TableauPaiementService tableauPaiementService;

    @InjectMocks
    private PaiementServiceImpl paiementService;

    private DemandeBO demandeBo;
    private CommandeDemandeBO commandeDemande;
    private MoyenPaiementBO moyenPaiement;
    private InformationFacturationBO infoFacturation;
    private CommandeBO commande;
    private CommandeOperationBO commandeOperation;
    private DemandeDTO demande;
    private DebitDTO debit;

    @BeforeEach
    void setUp() {
        commandeOperation = mock(CommandeOperationBO.class);
        // 1️⃣ Initialisation des objets communs
        demandeBo = mock(DemandeBO.class);
        demandeBo.setPkDemandes(42);
        DemandesUsagersBO usager = new DemandesUsagersBO();
        usager.setId(99);
        demandeBo.setUsager(usager);

        commandeDemande = mock(CommandeDemandeBO.class);
        commandeDemande.setMontant(Double.parseDouble("5.0"));

        moyenPaiement = mock(MoyenPaiementBO.class);
        commande = mock(CommandeBO.class);;
        commande.setPkCommandes(11);
        moyenPaiement.setCommande(commande);

        infoFacturation = mock(InformationFacturationBO.class);
        when(commandeOperation.getDemande()).thenReturn(demandeBo);

        when(demandeBo.getPkDemandes()).thenReturn(42);

        demande = mock(DemandeDTO.class);
        when(demande.getIdentifiant()).thenReturn("DEM-001");

        debit = mock(DebitDTO.class);

        // 2️⃣ Configuration commune des mocks
        when(demandesRepository.findByIdentifiant(anyString())).thenReturn(demandeBo);
        when(commandeDemandeRepository.findLatestCommandeForDemande(42))
                .thenReturn(Optional.of(commandeDemande));
        when(moyenPaiementRepository.findByDemande_PkDemandesAndLastCreationDate(42))
                .thenReturn(moyenPaiement);
        when(infoFacturationRepository.findByCommande_PkCommandes(11))
                .thenReturn(infoFacturation);
        when(gouvPropertiesResolver.getMwpaymtUrl()).thenReturn("https://mwpaymt.fake");
    }

    @Test
    void testGetTableauPaiement_Brouillons() {
        String ids = "[1,2]";
        String objectType = RequestConstant.BROUILLONS_PATH;
        Integer usagerId = 123;

        BrouillonDTO brouillon1 = mock(BrouillonDTO.class);
        BrouillonDTO brouillon2 = mock(BrouillonDTO.class);
        JsonNode contenuMock = mock(JsonNode.class);
        TableauDTO tableau1 = new TableauDTO();
        TableauDTO tableau2 = new TableauDTO();

        when(brouillonsService.getBrouillon(1, usagerId)).thenReturn(brouillon1);
        when(brouillonsService.getBrouillon(2, usagerId)).thenReturn(brouillon2);
        when(brouillon1.getContenu()).thenReturn(contenuMock);
        when(brouillon2.getContenu()).thenReturn(contenuMock);
        when(brouillon1.getPkBrouillons()).thenReturn(10);
        when(brouillon2.getPkBrouillons()).thenReturn(20);
        when(tableauPaiementService.getItemTableauPaiement(contenuMock, 10)).thenReturn(tableau1);
        when(tableauPaiementService.getItemTableauPaiement(contenuMock, 20)).thenReturn(tableau2);

        List<TableauDTO> result = paiementService.getTableauPaiement(ids, objectType, usagerId);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(tableau1, tableau2)));

        verify(brouillonsService, times(2)).getBrouillon(anyInt(), eq(usagerId));
        verify(tableauPaiementService, times(2)).getItemTableauPaiement(eq(contenuMock), anyInt());
    }

    @Test
    void testGetTableauPaiement_Demandes() {
        String ids = "[5]";
        String objectType = RequestConstant.DEMANDES_PATH;
        Integer usagerId = 321;

        DemandeDTO demande = mock(DemandeDTO.class);
        JsonNode contenuMock = mock(JsonNode.class);
        TableauDTO tableau = new TableauDTO();

        when(demandesService.getDemande(5, usagerId)).thenReturn(demande);
        when(demande.getContenu()).thenReturn(contenuMock);
        when(demande.getPkDemandes()).thenReturn(99);
        when(tableauPaiementService.getItemTableauPaiement(contenuMock, 99)).thenReturn(tableau);

        List<TableauDTO> result = paiementService.getTableauPaiement(ids, objectType, usagerId);

        assertEquals(1, result.size());
        assertEquals(tableau, result.get(0));

        verify(demandesService).getDemande(5, usagerId);
        verify(tableauPaiementService).getItemTableauPaiement(contenuMock, 99);
    }

    @Test
    void testGetTableauPaiement_ItemNull() {
        String ids = "[42]";
        String objectType = RequestConstant.BROUILLONS_PATH;
        Integer usagerId = 999;

        BrouillonDTO brouillon = mock(BrouillonDTO.class);
        JsonNode contenuMock = mock(JsonNode.class);

        when(brouillonsService.getBrouillon(42, usagerId)).thenReturn(brouillon);
        when(brouillon.getContenu()).thenReturn(contenuMock);
        when(brouillon.getPkBrouillons()).thenReturn(42);
        when(tableauPaiementService.getItemTableauPaiement(any(), anyInt())).thenReturn(null);

        List<TableauDTO> result = paiementService.getTableauPaiement(ids, objectType, usagerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetInfoFacturation_Complet() {
        GichuniUsagerDTO usager = new GichuniUsagerDTO();
        usager.setNom("Dupont");
        usager.setPrenom("Jean");
        usager.setTitre(Short.valueOf("0"));
        usager.setEmail("jean.dupont@email.fr");
        usager.setRaisonSociale("Ma Société");
        UsagerTypeEnum usagerType = UsagerTypeEnum.INDIVIDUAL;
        usager.setType(usagerType); // ou ton enum si c’est un enum

        AdresseFacturationDTO adresseFacturation = new AdresseFacturationDTO();
        adresseFacturation.setAdresse("12 rue des Lilas");
        adresseFacturation.setComplAdresse1("Bâtiment B");
        adresseFacturation.setComplAdresse2("Appartement 23");
        adresseFacturation.setPaysCode("FR");
        adresseFacturation.setCodePostal("75001");
        adresseFacturation.setVille("Paris");
        usager.setAdresseFacturation(adresseFacturation);

        InfoFacturationResponseDTO result = paiementService.getInfoFacturation(usager);

        assertNotNull(result);
        assertEquals("jean.dupont@email.fr", result.getEmail());
        assertEquals("Ma Société", result.getRaisonSociale());
        assertTrue(result.isSaveRaisonSociale());
        assertEquals(UsagerTypeEnum.INDIVIDUAL.getValue(), result.getProfilType());

        assertNotNull(result.getVous());
        assertEquals("Dupont", result.getVous().getNom());
        assertEquals("Jean", result.getVous().getPrenom());
        assertEquals(Short.valueOf("0"), result.getVous().getTitre());

        assertNotNull(result.getAdresse());
        assertEquals("12 rue des Lilas", result.getAdresse().getLigne1());
        assertEquals("Bâtiment B", result.getAdresse().getLigne2());
        assertEquals("Appartement 23", result.getAdresse().getLigne3());
        assertEquals("FR", result.getAdresse().getPays());
        assertEquals("75001", result.getAdresse().getCodePostal());
        assertEquals("Paris", result.getAdresse().getVille());
    }

    @Test
    void testGetInfoFacturation_AdresseFacturationNull() {
        GichuniUsagerDTO usager = new GichuniUsagerDTO();
        usager.setNom("Martin");
        usager.setPrenom("Julie");
        usager.setTitre(Short.valueOf("1"));
        usager.setEmail("julie.martin@email.fr");
        usager.setRaisonSociale("Particulier");
        UsagerTypeEnum usagerType = UsagerTypeEnum.INDIVIDUAL;
        usager.setType(usagerType);
        usager.setAdresseFacturation(null);

        InfoFacturationResponseDTO result = paiementService.getInfoFacturation(usager);

        assertNotNull(result.getAdresse());
        assertNull(result.getAdresse().getLigne1()); // car pas d’adresse renseignée
        assertEquals(null, result.getAdresse().getLigne2());
        assertEquals(null, result.getAdresse().getLigne3());
    }

    @Test
    void testSontDejaEncaissees_AucuneDemande() throws Exception {
        List<Integer> ids = Collections.emptyList();

        Method m = PaiementServiceImpl.class.getDeclaredMethod("sontDejaEncaissees", List.class);
        m.setAccessible(true);
        boolean result = (boolean) m.invoke(paiementService, ids);

        assertTrue(result); // 0 == 0 → true
    }

    @Test
    void testIsDebitDeclenche_TrueQuandListeNonVide() {
        Integer pkDemande = 123;
        CommandeOperationBO operation = new CommandeOperationBO();
        when(commandeOperationRepository.findAllByFkDemandes(pkDemande))
                .thenReturn(List.of(operation));

        boolean result = paiementService.isDebitDeclenche(pkDemande);

        assertTrue(result);
        verify(commandeOperationRepository).findAllByFkDemandes(pkDemande);
    }

    @Test
    void testIsDebitDeclenche_FalseQuandListeVide() {
        Integer pkDemande = 456;
        when(commandeOperationRepository.findAllByFkDemandes(pkDemande))
                .thenReturn(Collections.emptyList());

        boolean result = paiementService.isDebitDeclenche(pkDemande);

        assertFalse(result);
        verify(commandeOperationRepository).findAllByFkDemandes(pkDemande);
    }

    @Test
    void testIsDebitDeclenche_FalseQuandNullRetourne() {
        Integer pkDemande = 789;
        when(commandeOperationRepository.findAllByFkDemandes(pkDemande))
                .thenReturn(null); // sécurité si le repo renvoie null

        boolean result;
        try {
            result = paiementService.isDebitDeclenche(pkDemande);
        } catch (NullPointerException e) {
            result = false;
        }

        assertFalse(result);
        verify(commandeOperationRepository).findAllByFkDemandes(pkDemande);
    }

}
