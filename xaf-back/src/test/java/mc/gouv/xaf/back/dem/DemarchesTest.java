package mc.gouv.xaf.back.dem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mc.gouv.xaf.back.AfBackServiceTestConfiguration;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import mc.gouv.xaf.shared.dto.DemarcheInputDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.StatutInputDTO;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.DemandeComplementsStatutEnum;
import mc.gouv.xboot.test.jwt.JwtUtils;

/**
 * @author qdeme Exemple voir :https://spring.io/blog/2014/05/23/preview-spring-security-test-web-security
 *         http://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#test-mockmvc-setup
 */
@Ignore // TODO Remplacer les appels mock par des appels directs aux services...
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = DemarchesApiApplication.class)
@SpringBootTest(classes = AfBackServiceTestConfiguration.class)
@WebAppConfiguration // Nécessaire car @EnableWebMvc utilisé
public class DemarchesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemarchesTest.class);

    private static boolean initialized = false;

    private static MockMvc mockMvc;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final String demarcheId = "HAB";

    private final String demarcheId2 = "HAB2";

    private final String emailService = "hab@gouv.mc";

    private final String emailServiceNom = "Direction de l''Habitat";

    private final String emailReplyto = "habreplyto@gouv.mc";

    private final String emailReplytoNom = "Reply To";

    private final String emailFrom = "from@gouv.mc";

    private final String emailFromNom = "From";

    private final String emailService2 = "hab887@gouv.mc";

    private final String nomDemarche = "Déclarer un changement de situation pour un logement sous Loi 887";

    private final Integer usagerId = 2;

    private final Integer usagerId2 = 3;

    //private final Integer usagerId3 = 4;

    private final Integer usagerId4 = 5;

    private final Integer usagerId5 = 6;

    private final Integer usagerId6 = 7;

    private final Integer usagerId8 = 9;

    private final Integer usagerId9 = 10;

    private final Integer usagerId10 = 11;

    private final Integer usagerId11 = 12;

    private final Integer usagerId12 = 13;

    private final Integer usagerId13 = 14;

    private final Integer usagerId14 = 15;

    private final Integer usagerId15 = 16;

    private final Integer usagerId16 = 17;

    private final Integer usagerId17 = 18;

    private final Integer usagerId18 = 19;

    private final Integer usagerId19 = 20;

    public static boolean fileServiceImplTestTriggered = false;

    public static String fileServiceImplTestFileName;

    public static String fileServiceImplTestFileUrl;

    public static String fileServiceImplTestFileMeta;

    private final String linkedFileName = "console.log";

    private final String linkedFileUrl = "/ece7a75e-e5f9-11e5-a7a3-08002700c066/console.log";

    private final String linkedFileMeta = "Meta1";

    private final String linkedFileName2 = "name2";

    private final String linkedFileUrl2 = "url2";

    private final String linkedFileMeta2 = "meta2";

    private final String questionFausseDateStr = "19-02-1988";

    private final String reponseFausseDateStr = "08-10-1986";

    private final String codeMotif1 = "AUTRE_PERSONNE_CHOISIE";

    private final String questionMotif1 = "Extrait d'acte de naissance manquant";

    private final String codeMotif2 = "AUTRE_EXEMPLE_CODE";

    private final String questionMotif2 = "Autre exemple de motif...";

    private final String codeMotif3 = "AUTRE_CODE3";

    private final String questionMotif3 = "ABCD";

    private final String codeMotifRefus = "PAS_MONEGASQUE";

    private final String commentaireRefus = "Vous devez etre de nationalite monegasque pour effectuer cette demande";

    private final String questionAgentId = "agent007";

    private final Integer reponseUsagerId = 1;

    private final String reponseTexte = "Voici l'extrait demande";

    private final String agentId1 = "agentId1";

    private final String langueFR = "fr";

    private final String langueIT = "it";

    private final String observations = "Dossier un peu limite";

    private final String motifLibelle1 = "Une autre personne a ete choisie";

    private final String motifLibelle2 = "Une autre personne a ete choisie par le service";

    private final String usagerCourrierAdresse1 = "26 rue des palmiers";

    private final String usagerCourrierAdresse2 = "Bat A";

    private final String usagerCourrierAdresseComplement = "Etage 2";

    private final String usagerCourrierCodePostal = "06190";

    private final String usagerCourrierEmail = "test@gouv.mc";

    private final String usagerCourrierPrenom = "Lo";

    private final String usagerCourrierNom = "Gin";

    private final String usagerCourrierPays = "SY"; // Syrie

    private final String usagerCourrierPays2 = "MC"; // Monaco

    private final String usagerCourrierRaisonSociale = "Gouvernement de Monaco";

    private final String usagerCourrierTelephone = "+377645897823";

    private final Integer usagerCourrierTitre = 1;

    private final String usagerCourrierVille = "Monaco";

    private final String usagerCourrier2Prenom = "Lo2";

    private final String usagerCourrier2Nom = "Gin2";

    private final String usagerCourrier2Login = "testlogin2";

    private final String template1Code = "MAIL_CREATION";

    private final String template2Code = "MAIL_REPONSE_IC";

    private final String template1Contenu = "Bonjour Mr ${userName}, veuillez trouver votre attestation <a href=\"${url}\">ici</a>";

    private final String template2Contenu = "Bonjour Mr ${userName}, voici votre mot de passe : ${userPwd}";

    private final String key1 = "key1";

    private final String value1 = "value1";

    private final String key2 = "key2";

    private final String value2 = "value2";

    private final DemandeCanalEnum canal = DemandeCanalEnum.GUICHET_VIRTUEL;

    private final String adresseWS = "/api/v1";

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private String histoAgentId = "90012";

    private Integer histoUsagerId = 3071;

    private String courrierDateReception = "14/12/2016";

    private String courrierRefInterne = "maRefInterne";

    private String courrier1Name = "CourrierRefus004";

    private String courrier1Url = "/url/dans/file";

    private String courrier1Meta = "Une meta";

    private String courrier1Identifiant = "CourrierID05";

    private String courrier2Name = "CourrierAcceptation006";

    private String courrier2Url = "/autre/url/dans/file";

    private String courrier2Meta = "Une deuxieme meta";

    @Before
    public void setup() {

        // Booléen nécessaire pour ne faire l'initialisation qu'une seule fois, car @Before s'exécute avant chaque
        // méthode @Test
        // Certes on aurait pu utiliser @BeforeClass qui ne s'exécute qu'une seule fois avant tous les tests, mais cela
        // impose
        // le "static", et le setup utilise des champs @Autowired qui ne peuvent pas être "static"...
        // Quant à l'AbstractTestExecutionListener de Spring utilisé avec @TestExecutionListeners, ça s'exécute avant
        // l'autowiring des
        // champs, donc on ne peut pas faire le setup non plus...
        if (!initialized) {
            LOGGER.info("MockMvc setup...");

            // webAppContextSetup au lieu de standaloneSetup, pour prendre en charge
            // le WebMvcConfig de l'application
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

            LOGGER.info("Définition de la valeur initiale de la clé primaire de la table USAGERS_COURRIER...");
            
            jdbcTemplate.execute(
                    "ALTER TABLE HAB.DEM_USAGERS_COURRIER ALTER COLUMN PK_USAGERSCOURRIER RESTART WITH 1000000001");
			jdbcTemplate.execute(
					"INSERT INTO HAB.DEM_DEMARCHES (PK_DEMARCHEID,NOM,EMAIL_SERVICE,EMAIL_SERVICE_NOM,EMAIL_REPLYTO,EMAIL_REPLYTO_NOM,EMAIL_FROM,EMAIL_FROM_NOM,IDENTIFIANT_PREFIXE) VALUES ('"
							+ demarcheId + "','" + nomDemarche + "','" + emailService + "','" + emailServiceNom + "','"
							+ emailReplyto + "','" + emailReplytoNom + "','" + emailFrom + "','" + emailFromNom + "','"
							+ demarcheId + "')");
			jdbcTemplate.execute(
					"INSERT INTO HAB.DEM_DEMARCHES (PK_DEMARCHEID,NOM,EMAIL_SERVICE,EMAIL_SERVICE_NOM,EMAIL_REPLYTO,EMAIL_REPLYTO_NOM,EMAIL_FROM,EMAIL_FROM_NOM,IDENTIFIANT_PREFIXE) VALUES ('"
							+ demarcheId2 + "','" + nomDemarche + "','" + emailService + "','" + emailServiceNom + "','"
							+ emailReplyto + "','" + emailReplytoNom + "','" + emailFrom + "','" + emailFromNom + "','"
							+ demarcheId2 + "')");

            initialized = true;
        }
    }

    private static class HttpJwtRequestPostProcessor implements RequestPostProcessor {

        private String headerValue;

        private HttpJwtRequestPostProcessor(String secret, String sub, String role, String aud) {
            this.headerValue = JwtUtils.createJwtHeaderValue(secret, sub, role, aud);
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.addHeader("Authorization", this.headerValue);
            return request;
        }
    }

    public static RequestPostProcessor httpJwt(String secret, String sub, String role, String aud) {
        return new HttpJwtRequestPostProcessor(secret, sub, role, aud);
    }

    public static RequestPostProcessor httpJwtFrontUser() {
        return new HttpJwtRequestPostProcessor(TestUtils.JWT_SECRET, "TEST", TestUtils.FRONT_USER_ROLE, "DEM");
    }

    public static RequestPostProcessor httpJwtBackUser() {
        return new HttpJwtRequestPostProcessor(TestUtils.JWT_SECRET, "TEST", TestUtils.BACK_USER_ROLE, "DEM");
    }

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services /accesses et /demandes.
     * 
     * @throws Exception
     */
    @Test
    public void testAccessesDemandes() throws Exception {

        LOGGER.info("Access object creation...");

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId);

        LOGGER.info("REST call: modification de l'accès");

        contenu = mapper.readTree("{ \"d3\":\"d4\" }");
        accessInput = new AccessInputDTO();
        accessInput.setContenu(contenu);
        result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isOk()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId);

        LOGGER.info("REST call: récupération de l'accès...");

        result = mockMvc.perform(get(adresseWS + "/accesses/" + demarcheId + "/" + usagerId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId);

        LOGGER.info("REST call: création d'une demande relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);
        // Champs qui normalement ne devraient pas être pris en compte par le service à la création d'une demande :
        demandeInput.setObservations(observations);
        // Champs courrier
        demandeInput.setCourrierDateReception(new SimpleDateFormat("dd/MM/yyyy").parse(courrierDateReception));
        demandeInput.setCourrierRefInterne(courrierRefInterne);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getLangue().equals(langueFR));
        assertTrue(demande.getObservations() == null);
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId);
        assertTrue(demande.getStatuts().length == 1);
        assertTrue(demande.getStatuts()[0].getLibelle().equals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
        assertTrue(demande.getStatuts()[0].getUsagerId() == usagerId);
        assertTrue(demande.getCanal().equals(canal));
        assertTrue(demande.getCourrierDateReception()
                .equals(new SimpleDateFormat("dd/MM/yyyy").parse(courrierDateReception)));
        assertTrue(demande.getCourrierRefInterne().equals(courrierRefInterne));
        // Vérification également du champ "dernierStatut" de la demande
        assertTrue(demande.getDernierStatut().getLibelle().equals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
        // Vérification de l'ID public généré
        String identifiant = demande.getIdentifiant();
        assertTrue(identifiant != null);
        assertTrue(identifiant.split("-")[0].equals(demarcheId));
        assertTrue(identifiant.split("-")[1].equals(dateFormat.format(new Date())));
        assertTrue(identifiant.split("-")[2].length() == 4);
        Integer demandeId1 = demande.getPkDemandes();

        LOGGER.info("REST call: création d'une deuxième demande relative à cet accès");

        contenu = mapper.readTree("{ \"d7\":\"d8\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId);
        Integer demandeId2 = demande.getPkDemandes();

        LOGGER.info("REST call: récupération des deux demandes (toutes les demandes de l'usager)");

        result = mockMvc
                .perform(get(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeDTO[] demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);
        boolean d1found, d2found;
        d1found = d2found = false;
        for (DemandeDTO d : demandes) {
            if (d.getPkDemandes() == demandeId1) {
                d1found = true;
            } else if (d.getPkDemandes() == demandeId2) {
                d2found = true;
            }
        }
        assertTrue(d1found && d2found);

        LOGGER.info("REST call: modification de la demande 1");

        contenu = mapper.readTree("{ \"d7\":\"d9\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        // + mise à jour des champs suivants :
        demandeInput.setObservations(observations);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId1).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId);
        assertTrue(demande.getPkDemandes() == demandeId1);
        assertTrue(demande.getObservations().equals(observations));

        LOGGER.info("REST call: suppression de la demande 2...");

        mockMvc.perform(delete(adresseWS + "/demandes/" + demarcheId + "/" + demandeId2).with(httpJwtBackUser()))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la demande 2 : erreur attendue");

        mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId2).with(httpJwtFrontUser()))
                .andExpect(status().isNotFound());

        LOGGER.info("REST call: récupération de la demande 1");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId1).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId);
        assertTrue(demande.getPkDemandes() == demandeId1);
        assertTrue(demande.getStatuts().length == 1);
        assertTrue(demande.getStatuts()[0].getLibelle().equals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
        assertTrue(demande.getStatuts()[0].getUsagerId() == usagerId);

        // Supprimer l'accès et constater que la demande a aussi été supprimée (en apparence car flag active = false)

        LOGGER.info("REST call: suppression de l'accès");

        mockMvc.perform(
                delete(adresseWS + "/accesses/" + demarcheId + "/" + usagerId)
                        .with(httpJwtFrontUser()))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la demande 1 : erreur attendue");

        mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId1).with(httpJwtFrontUser()))
                .andExpect(status().isNotFound());

        LOGGER.info("Vérification en base que l'accès et la demande existent encore");

        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId1);

        assertTrue(demandeBoOp.isPresent());
        assertTrue(demandeBoOp.get().getPkDemandes() == demandeId1);
        assertTrue(demandeBoOp.get().getFkAccess() != null);
        assertFalse(demandeBoOp.get().getFkAccess().isActive());
    }

    /**
     * Tente de créer une demande sur un accès inexstant et vérifie que le la bonne erreur est retournée
     * 
     * @throws Exception
     */
    @Test
    public void testCreerDemandeSurAccesInexistant() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");

        LOGGER.info("REST call: création d'une demande relative à un accès inexistant : erreur attendue");

        contenu = mapper.readTree("{ \"d7\":\"d8\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        mockMvc.perform(post(adresseWS + "/demandes/999?usagerId=" + usagerId + "&premierStatut="
                + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isNotFound());

    }

    /**
     * Tente de mettre à jour une demande sur un accès qui a été désactivé, doit retourner accès inexistant
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateDemandeSurAccesDesactive() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId10).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId10);

        LOGGER.info("REST call: création d'une demande relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId10 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId10);
        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: suppression de l'accès");

        mockMvc.perform(
                delete(adresseWS + "/accesses/" + demarcheId + "/" + usagerId10)
                        .with(httpJwtFrontUser()))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la demande : erreur attendue");

        mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser())
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

    }

    /**
     * Teste l'appel au WS FILE quand un fichier lié a été indiqué dans le contenu de la demande
     * 
     * @throws Exception
     */
    @Test
    @Transactional
    public void testCreerDemandeAvecFichiersLies() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId2).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId2);

        LOGGER.info("REST call: création d'une demande relative à cet accès avec un fichier lié");

        contenu = mapper.readTree("{ \"d3\":\"d4\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setCreeParAgentId("19723");
        demandeInput.setContenu(contenu);
        DemandeFileDTO file = new DemandeFileDTO();
        file.setName(linkedFileName);
        file.setUrl(linkedFileUrl);
        file.setMeta(linkedFileMeta);
        demandeInput.setFichiers(new DemandeFileDTO[] { file });
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId2 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertEquals(demande.getCreeParAgentId(), "19723");
        assertTrue(demande.getUsagerId() == usagerId2);
        // Vérifier que les informations liées au fichier ont bien été retournées dans la réponse
        assertTrue(demande.getFichiers()[0].getName().equals(linkedFileName));
        assertTrue(demande.getFichiers()[0].getUrl().equals(linkedFileUrl));
        assertTrue(demande.getFichiers()[0].getMeta().equals(linkedFileMeta));

        // Vérifier que le service a été appelé
        assertTrue(fileServiceImplTestTriggered);
        assertTrue(fileServiceImplTestFileName.equals(linkedFileName));
        assertTrue(fileServiceImplTestFileUrl.equals(linkedFileUrl));
        assertTrue(fileServiceImplTestFileMeta.equals(linkedFileMeta));

        // Vérifier en base que tout y est
        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demande.getPkDemandes());
        assertTrue(demandeBoOp.get().getFiles() != null);
        DemandesFilesBO fileBo = demandeBoOp.get().getFiles().iterator().next();
        assertTrue(fileBo.getName().equals(linkedFileName));
        assertTrue(fileBo.getUrl().equals(linkedFileUrl));
        assertTrue(fileBo.getMeta().equals(linkedFileMeta));

    }

    /**
     * Vérifie le bon fonctionnement de la modification d'une demande afin de supprimer ses fichiers liés
     * 
     * @throws Exception
     */
    @Test
    public void testSupprimerFichiersLiesAUneDemande() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId5).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId5);

        LOGGER.info("REST call: création d'une demande relative à cet accès avec un fichier lié");

        contenu = mapper.readTree("{ \"d3\":\"d4\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        DemandeFileDTO file = new DemandeFileDTO();
        file.setName(linkedFileName);
        file.setUrl(linkedFileUrl);
        file.setMeta(linkedFileMeta);
        demandeInput.setFichiers(new DemandeFileDTO[] { file });
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId5 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);
        Integer demandeId = demande.getPkDemandes();

        assertTrue(demande.getFichiers().length == 1);

        LOGGER.info("REST call: modification de cette demande pour supprimer les fichiers liés");

        contenu = mapper.readTree("{ \"d7\":\"d9\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        // Aucun fichier défini

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isOk()).andReturn();

        LOGGER.info("REST call: récupération de la demande");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getFichiers() == null || demande.getFichiers().length == 0);

    }

    /**
     * Teste le WS de récupération d'accès par clé primaire
     * 
     * @throws Exception
     */
    @Test
    public void testGetAccessFromId() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AccessDTO access = new AccessDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        access.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId4).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(access)))
                .andExpect(status().isCreated()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId4);
        Integer accessId = access.getPkAccess();

        LOGGER.info("REST call: récupération de l'accès...");

        result = mockMvc.perform(get(adresseWS + "/accesses/" + accessId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId4);
    }

    /**
     * Crée une demande d'informations complémentaire par rapport à une demande
     * 
     * @throws Exception
     */
    @Test
    public void testCreerDemandeComplements() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");

        LOGGER.info("Access object creation...");

        AccessInputDTO accessInput = new AccessInputDTO();
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId6).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        LOGGER.info("REST call: création d'une demande relative à l'accès");

        contenu = mapper.readTree("{ \"d7\":\"d8\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);
        DemandeFileDTO file0 = new DemandeFileDTO();
        file0.setName(linkedFileName);
        file0.setUrl(linkedFileUrl);
        file0.setMeta(linkedFileMeta);
        demandeInput.setFichiers(new DemandeFileDTO[] { file0 });

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId6 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // La demande doit être dans le statut "En attente"
        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle()
                .equals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()));
        // Vérification également du champ "agentAffecte" de la demande
        assertTrue(demande.getAgentAffecteId() == null);

        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: passage de la demande au statut EN_COURS_TRAIT");

        StatutInputDTO statutInput = new StatutInputDTO();
        statutInput.setStatut(TestDemandeStatutEnum.EN_COURS_TRAIT.name());
        statutInput.setAgentId(agentId1);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/statuts").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(statutInput)))
                .andExpect(status().isCreated());

        LOGGER.info("REST call: affectation de la demande à un agent de l'état");

        demandeInput = new DemandeInputDTO();
        demandeInput.setAgentAffecteId(agentId1);

        mockMvc.perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtBackUser())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // La demande doit être dans le statut "En cours de traitement"
        DemandeStatutDTO statut = DemarchesUtils.getLatestStatus(demande);
        assertTrue(statut.getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));
        assertTrue(statut.getUsagerId() == null);
        assertTrue(statut.getAgentId().equals(agentId1));
        // Vérification également du champ "dernierStatut" de la demande
        assertTrue(demande.getDernierStatut().getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));
        // Vérification également du champ "agentAffecte" de la demande
        assertTrue(demande.getAgentAffecteId().equals(agentId1));

        LOGGER.info("REST call: création d'une demande d'informations complémentaires relative à la demande");

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date questionFausseDate = formatter.parse(questionFausseDateStr);
        DemandeComplementsQuestionDTO question = new DemandeComplementsQuestionDTO();
        question.setTexte(questionMotif1);
        question.setCodeMotif(codeMotif1);
        question.setAgentId(questionAgentId);
        question.setDate(questionFausseDate);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements")
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(question)))
                .andExpect(status().isCreated()).andReturn();

        DemandeComplementsDTO demandeCompl = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getDemandeId() == demandeId);
        assertTrue(demandeCompl.getQuestion() != null);
        assertTrue(demandeCompl.getReponse() == null);
        assertTrue(demandeCompl.getQuestion().getAgentId().equals(questionAgentId));
        assertTrue(demandeCompl.getQuestion().getTexte().equals(questionMotif1));
        assertTrue(demandeCompl.getQuestion().getCodeMotif().equals(codeMotif1));
        assertTrue(!demandeCompl.getQuestion().getDate().equals(questionFausseDate));
        Date questionDate = demandeCompl.getQuestion().getDate();

        Integer demandeComplId = demandeCompl.getPkDemandeComplements();

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // La demande doit être dans le statut "En attente"
        statut = DemarchesUtils.getLatestStatus(demande);
        // assertTrue(statut.getLibelle().equals(DemandeStatutEnum.EN_ATTENTE_COMPL));
        // Suite aux modifs qui font que le BPM a plus de contrôle : l'état doit rester le même
        assertTrue(statut.getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));
        assertTrue(statut.getUsagerId() == null);
        // Suite aux modifs qui font que le BPM a plus de contrôle : le statut n'est pas mis à jour par DEM, donc
        // l'agent du statut ne change pas
        // assertTrue(statut.getAgentId().equals(questionAgentId));
        // Vérification également du champ "dernierStatut" de la demande
        // assertTrue(demande.getDernierStatut().getLibelle().equals(DemandeStatutEnum.EN_ATTENTE_COMPL));
        assertTrue(demande.getDernierStatut().getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));

        LOGGER.info("REST call: modification de la demande d'informations complémentaires");

        question.setTexte(questionMotif2);
        question.setCodeMotif(codeMotif2);

        result = mockMvc
                .perform(
                        post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                                .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(question)))
                .andExpect(status().isCreated()).andReturn();

        demandeCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getQuestion().getTexte().equals(questionMotif2));
        assertTrue(demandeCompl.getQuestion().getCodeMotif().equals(codeMotif2));

        LOGGER.info("REST call: répondre à la demande d'informations complémentaires");

        DemandeComplementsReponseDTO reponse = new DemandeComplementsReponseDTO();
        reponse.setUsagerId(reponseUsagerId);
        reponse.setTexte(reponseTexte);
        DemandeComplementsFileDTO file1 = new DemandeComplementsFileDTO();
        file1.setName(linkedFileName);
        file1.setUrl(linkedFileUrl);
        file1.setMeta(linkedFileMeta);
        DemandeComplementsFileDTO file2 = new DemandeComplementsFileDTO();
        file2.setName(linkedFileName2);
        file2.setUrl(linkedFileUrl2);
        file2.setMeta(linkedFileMeta2);
        reponse.setFichiers(new DemandeComplementsFileDTO[] { file1, file2 });
        Date reponseFausseDate = formatter.parse(reponseFausseDateStr);
        reponse.setDate(reponseFausseDate);

        result = mockMvc
                .perform(put(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                        .with(httpJwtFrontUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reponse)))
                .andExpect(status().isOk()).andReturn();

        demandeCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getPkDemandeComplements() == demandeComplId);
        assertTrue(demandeCompl.getReponse().getTexte().equals(reponseTexte));
        assertTrue(!demandeCompl.getReponse().getDate().equals(reponseFausseDate));

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // La demande doit être dans le statut "En attente"
        statut = DemarchesUtils.getLatestStatus(demande);
        assertTrue(statut.getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));
        // Suite aux modifs qui sont que le BPM a plus de contrôle, le statut n'est plus mis par DEM, et donc
        // on ne change pas l'usager/agent du statut
        // assertTrue(statut.getUsagerId() == reponseUsagerId);
        // assertTrue(statut.getAgentId() == null);

        LOGGER.info(
                "REST call: s'assurer qu'on ne peut pas répondre deux fois à une demande d'informations complémentaires...");

        mockMvc.perform(put(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                .with(httpJwtFrontUser()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reponse))).andExpect(status().isBadRequest());

        LOGGER.info(
                "REST call: s'assurer qu'on ne peut pas modifier une demande d'informations complémentaires qui a déjà fait l'objet d'une réponse...");

        question.setTexte(questionMotif3);
        question.setCodeMotif(codeMotif3);

        mockMvc.perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(question))).andExpect(status().isBadRequest());

        LOGGER.info("REST call: récupérer la demande d'informations complémentaires et vérifier les données");

        result = mockMvc
                .perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                        .with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        demandeCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getPkDemandeComplements() == demandeComplId);
        assertTrue(demandeCompl.getQuestion() != null);
        assertTrue(demandeCompl.getQuestion().getAgentId().equals(questionAgentId));
        assertTrue(demandeCompl.getQuestion().getTexte().equals(questionMotif2));
        assertTrue(demandeCompl.getQuestion().getCodeMotif().equals(codeMotif2));
        assertTrue(demandeCompl.getQuestion().getDate().equals(questionDate));
        assertTrue(demandeCompl.getReponse().getTexte().equals(reponseTexte));
        assertTrue(demandeCompl.getReponse().getAgentId() == null);
        assertTrue(demandeCompl.getReponse().getUsagerId() == reponseUsagerId);
        assertTrue(!demandeCompl.getReponse().getDate().equals(reponseFausseDate));
        DemandeComplementsFileDTO[] files = demandeCompl.getReponse().getFichiers();

        boolean file1Found, file2Found;
        file1Found = file2Found = false;
        for (DemandeComplementsFileDTO file : files) {
            if (file.getName().equals(linkedFileName)) {
                file1Found = true;
                assertTrue(file.getUrl().equals(linkedFileUrl));
                assertTrue(file.getMeta().equals(linkedFileMeta));
            } else if (file.getName().equals(linkedFileName2)) {
                file2Found = true;
                assertTrue(file.getUrl().equals(linkedFileUrl2));
                assertTrue(file.getMeta().equals(linkedFileMeta2));
            }
        }
        assertTrue(file1Found && file2Found);

        LOGGER.info("REST call: supprimer la réponse de la demande d'informations complémentaires");

        result = mockMvc.perform(delete(
                adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId + "/reponse")
                        .with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        LOGGER.info("REST call: vérifier que la réponse n'y est plus");

        result = mockMvc
                .perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                        .with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demandeCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getQuestion() != null);
        assertTrue(demandeCompl.getReponse() == null);

        // Tester le WS de récupération des statuts
        // Il doit y avoir EN_ATTENTE_TRAIT, EN_COURS_TRAIT, EN_ATTENTE_COMPL

        LOGGER.info("REST call: récupération de statuts de la demande puis vérification");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/statuts").with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeStatutDTO[] statuts = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeStatutDTO[].class);

        int nbEnAttente = 0, nbAffectee = 0, nbEnAttenteCompl = 0, autres = 0;
        for (DemandeStatutDTO sta : statuts) {
            if (sta.getLibelle().equals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name())) {
                nbEnAttente++;
            } else if (sta.getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name())) {
                nbAffectee++;
            } else if (sta.getLibelle().equals(TestDemandeStatutEnum.EN_ATTENTE_COMPL.name())) {
                nbEnAttenteCompl++;
            } else {
                autres++;
            }
        }
        // Suite aux modifs qui font que le BPM a plus de contrôle, le statut EN_ATTENTE_COMPL n'est plus mis par DEM
        assertTrue(nbEnAttenteCompl == 0);
        assertTrue(nbAffectee == 1);
        assertTrue(nbEnAttente == 1);
        assertTrue(autres == 0);

        LOGGER.info("REST call: créer une seconde demande d'informations complémentaires");

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements")
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(question)))
                .andExpect(status().isCreated()).andReturn();

        demandeCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO.class);

        Integer demandeComplId2 = demandeCompl.getPkDemandeComplements();

        LOGGER.info(
                "REST call: tester le WS de récupération de demandes d'informations complémentaires par rapport à une demande et vérifier qu'on récupère bien les deux");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements").with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeComplementsDTO[] demandesCompl = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeComplementsDTO[].class);
        boolean d1found, d2found;
        d1found = d2found = false;
        for (DemandeComplementsDTO d : demandesCompl) {
            if (d.getPkDemandeComplements() == demandeComplId) {
                d1found = true;
            } else if (d.getPkDemandeComplements() == demandeComplId2) {
                d2found = true;
            }
        }
        assertTrue(d1found && d2found);

        int numberOfDemandesCompl = demandesCompl.length;

        LOGGER.info("REST call: création d'une ligne d'historique pour cette demande");

        DemandeHistoriqueDTO historique = new DemandeHistoriqueDTO();
        historique.setAgentId(histoAgentId);
        JsonNode histoContenu = mapper.readTree("{ \"d10\":\"d11\" }");
        historique.setContenu(histoContenu);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/historique").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(historique)))
                .andExpect(status().isCreated());

        LOGGER.info("REST call: création d'une 2ème ligne d'historique pour cette demande");

        historique = new DemandeHistoriqueDTO();
        historique.setAgentId(histoAgentId);
        histoContenu = mapper.readTree("{ \"d11\":\"d12\" }");
        historique.setContenu(histoContenu);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/historique").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(historique)))
                .andExpect(status().isCreated());

        LOGGER.info("REST call: création d'une donnée de demande");

        DemandeDataDTO demandeDataDto = new DemandeDataDTO();
        demandeDataDto.setValue(value1);

        mockMvc.perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params/" + key1)
                .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(demandeDataDto))).andExpect(status().isCreated());

        LOGGER.info("REST call: création d'un courrier");

        DemandeCourrierDTO demandeCourrierDto = new DemandeCourrierDTO();
        demandeCourrierDto.setName(courrier1Name);
        demandeCourrierDto.setUrl(courrier1Url);
        demandeCourrierDto.setMeta(courrier1Meta);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeCourrierDto)))
                .andExpect(status().isCreated());

        /*
         * TEST DE LA FONCTIONNALITE DE DUPLICATION Inclus dans ce test car c'est celui qui remplit le plus de champs
         * dans une demande et qu'il faut tester (statuts, fichiers, demandes d'infos complémentaires, fichiers des
         * demandes d'infos compl., données de demande, etc.)
         */

        LOGGER.info("TEST DE LA FONCTIONNALITE DE DUPLICATION");

        LOGGER.info("REST call: récupération de la demande pour la comparer plus tard à la demande clonée");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeDTO demande1 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);
        // Conversion en JSON PrettyPrint
        String demande1Json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(demande1);
        int demande1JsonLines = demande1Json.split("[\n|\r]").length;

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/clone")
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande2 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);
        // Conversion en JSON PrettyPrint
        String demande2Json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(demande2);
        int demande2JsonLines = demande2Json.split("[\n|\r]").length;

        // On veut que les JSON aient exactement le même nombre de lignes
        assertTrue(demande1JsonLines == demande2JsonLines);

        LOGGER.info("FIN TEST DE LA FONCTIONNALITE DE DUPLICATION");

        // Finalement ne plus dupliquer l'historique de la demande (#4679)
        // LOGGER.info("TEST DE LA DUPLICATION DE L'HISTORIQUE DE LA DEMANDE");
        //
        // result = mockMvc
        // .perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demande1.getPkDemandes() + "/historique")
        // .principal(principalBack).session(sessionBack))
        // .andExpect(status().isOk()).andReturn();
        //
        // DemandeHistoriqueDTO[] histos1 = mapper.readValue(result.getResponse().getContentAsString(),
        // DemandeHistoriqueDTO[].class);
        //
        // result = mockMvc
        // .perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demande2.getPkDemandes() + "/historique")
        // .principal(principalBack).session(sessionBack))
        // .andExpect(status().isOk()).andReturn();
        //
        // DemandeHistoriqueDTO[] histos2 = mapper.readValue(result.getResponse().getContentAsString(),
        // DemandeHistoriqueDTO[].class);
        //
        // assertTrue(histos1.length == histos2.length);
        //
        // LOGGER.info("FIN TEST DE LA DUPLICATION DE L'HISTORIQUE DE LA DEMANDE");

        /*
         * FIN TEST DE LA FONCTIONNALITE DE DUPLICATION
         */

        LOGGER.info("REST call: suppression de cette deuxième demande d'informations complémentaires");

        mockMvc.perform(
                delete(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId2)
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isOk());

        LOGGER.info(
                "REST call: récupération des demandes d'informations complémentaires et constat qu'il y en a une de moins");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements").with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demandesCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO[].class);

        assertTrue(demandesCompl.length == numberOfDemandesCompl - 1);

        LOGGER.info("REST call: suppression de la demande...");

        mockMvc.perform(delete(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtBackUser())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isOk());

        LOGGER.info("REST call: et constat que toutes les demandes d'informations complémentaires ont disparu");

        mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements").with(httpJwtFrontUser()))
                .andExpect(status().isNotFound());

        LOGGER.info("REST call: même quand on en demande une en particulier");

        mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/" + demandeComplId)
                .with(httpJwtFrontUser())).andExpect(status().isNotFound());

        LOGGER.info("REST call: suppression de la demande dupliquée...");

        mockMvc.perform(
                delete(adresseWS + "/demandes/" + demarcheId + "/" + demande2.getPkDemandes()).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la demande dupliquée (404 attendu)");

        mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isNotFound());
    }

    /**
     * Teste les transitions entre les différents états d'une demande
     * 
     * @throws Exception
     */
    @Test
    public void testGrapheEtats() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");

        // Définition de l'authentification FRONT

        LOGGER.info("Access object creation...");

        AccessInputDTO accessInput = new AccessInputDTO();
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId8).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        LOGGER.info("REST call: création d'une demande relative à l'accès");

        contenu = mapper.readTree("{ \"d7\":\"d8\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId8 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle()
                .equals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()));

        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: affectation de la demande à un agent de l'état");

        StatutInputDTO statutInput = new StatutInputDTO();
        statutInput.setStatut(TestDemandeStatutEnum.EN_COURS_TRAIT.name());
        statutInput.setAgentId(agentId1);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/statuts").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(statutInput)))
                .andExpect(status().isCreated());

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut0");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle()
                .equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));

        LOGGER.info("REST call: création d'une demande d'informations complémentaires relative à la demande");

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date questionFausseDate = formatter.parse(questionFausseDateStr);
        DemandeComplementsQuestionDTO question = new DemandeComplementsQuestionDTO();
        question.setTexte(questionMotif1);
        question.setCodeMotif(codeMotif1);
        question.setAgentId(questionAgentId);
        question.setDate(questionFausseDate);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements")
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(question)))
                .andExpect(status().isCreated()).andReturn();

        DemandeComplementsDTO demandeCompl = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getStatut().equals(DemandeComplementsStatutEnum.EN_ATTENTE));

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle().equals(DemandeStatutEnum.EN_ATTENTE_COMPL));
        // Maintenant, suite à l'augmentation du pilotage par le BPM : la demande doit rester dans l'état courant
        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle()
                .equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));

        LOGGER.info("REST call: répondre à la demande d'informations complémentaires");

        DemandeComplementsReponseDTO reponse = new DemandeComplementsReponseDTO();
        reponse.setUsagerId(reponseUsagerId);
        reponse.setTexte(reponseTexte);
        DemandeComplementsFileDTO file1 = new DemandeComplementsFileDTO();
        file1.setName(linkedFileName);
        file1.setUrl(linkedFileUrl);
        file1.setMeta(linkedFileMeta);
        DemandeComplementsFileDTO file2 = new DemandeComplementsFileDTO();
        file2.setName(linkedFileName2);
        file2.setUrl(linkedFileUrl2);
        file2.setMeta(linkedFileMeta2);
        reponse.setFichiers(new DemandeComplementsFileDTO[] { file1, file2 });
        Date reponseFausseDate = formatter.parse(reponseFausseDateStr);
        reponse.setDate(reponseFausseDate);

        result = mockMvc
                .perform(put(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/complements/"
                        + demandeCompl.getPkDemandeComplements()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(reponse)))
                .andExpect(status().isOk()).andReturn();

        demandeCompl = mapper.readValue(result.getResponse().getContentAsString(), DemandeComplementsDTO.class);

        assertTrue(demandeCompl.getStatut().equals(DemandeComplementsStatutEnum.REPONDUE));

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle()
                .equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut (ne doit pas avoir changé)");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle()
                .equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));

        LOGGER.info("REST call: rejeter la demande avec l'user BACK : doit fonctionner");

        statutInput.setStatut(TestDemandeStatutEnum.REFUSEE.name());
        statutInput.setAgentId(agentId1);
        statutInput.setCodeMotif(codeMotifRefus);
        statutInput.setCommentaire(commentaireRefus);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/statuts").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(statutInput)))
                .andExpect(status().isCreated());

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(DemarchesUtils.getLatestStatus(demande).getLibelle().equals(TestDemandeStatutEnum.REFUSEE.name()));
        assertTrue(demande.getDernierStatut().getLibelle().equals(TestDemandeStatutEnum.REFUSEE.name()));
        assertTrue(demande.getDernierStatut().getCodeMotif().equals(codeMotifRefus));
        assertTrue(demande.getDernierStatut().getCommentaire().equals(commentaireRefus));

    }

    /**
     * Teste la fonctionnalité PATCH des WS des demandes (modification partielle)
     * 
     * @throws Exception
     */
    @Test
    public void testPatchDemande() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId9).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId9);

        LOGGER.info("REST call: création d'une demande relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId9 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId9);
        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: modification partielle de la demande");

        contenu = mapper.readTree("{ \"d7\":\"d9\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setObservations(observations);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getObservations().equals(observations));

    }

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services /motifs.
     * 
     * @throws Exception
     */
    @Test
    public void testMotifs() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("REST call: création d'un motif");

        MotifDTO motif = new MotifDTO();
        motif.setLangue(langueFR);
        motif.setLibelle(motifLibelle1);
        motif.setStatut(TestDemandeStatutEnum.REFUSEE.name());
        motif.setCode(codeMotif1);

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/motifs/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(motif)))
                .andExpect(status().isCreated()).andReturn();

        motif = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO.class);

        Integer motifId = motif.getPkMotifs();

        assertTrue(motif.getDemarcheId().equals(demarcheId));
        assertTrue(motif.getLangue().equals(langueFR));
        assertTrue(motif.getLibelle().equals(motifLibelle1));
        assertTrue(motif.getStatut().equals(TestDemandeStatutEnum.REFUSEE.name()));
        assertTrue(motif.getCode().equals(codeMotif1));
        assertTrue(motif.getDateArchive() == null);

        LOGGER.info("REST call: récupération du motif");

        result = mockMvc.perform(get(adresseWS + "/motifs/" + demarcheId + "/" + motifId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        motif = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO.class);

        assertTrue(motif.getDemarcheId().equals(demarcheId));
        assertTrue(motif.getLangue().equals(langueFR));
        assertTrue(motif.getLibelle().equals(motifLibelle1));
        assertTrue(motif.getStatut().equals(TestDemandeStatutEnum.REFUSEE.name()));

        LOGGER.info("REST call: modification du motif");

        motif.setLibelle(motifLibelle2);
        motif.setCode(codeMotif2);

        result = mockMvc
                .perform(post(adresseWS + "/motifs/" + demarcheId + "/" + motifId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(motif)))
                .andExpect(status().isOk()).andReturn();

        motif = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO.class);

        assertTrue(motif.getLibelle().equals(motifLibelle2));
        assertTrue(motif.getCode().equals(codeMotif2));

        LOGGER.info("REST call: création d'un deuxième motif");

        MotifDTO motif2 = new MotifDTO();
        motif2.setLangue(langueIT);
        motif2.setLibelle(motifLibelle2);
        motif2.setStatut(TestDemandeStatutEnum.REFUSEE.name());
        motif2.setCode(codeMotif2);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/motifs/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(motif2)))
                .andExpect(status().isCreated()).andReturn();

        motif2 = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO.class);

        Integer motifId2 = motif2.getPkMotifs();

        assertTrue(motif2.getDemarcheId().equals(demarcheId));
        assertTrue(motif2.getLangue().equals(langueIT));
        assertTrue(motif2.getLibelle().equals(motifLibelle2));
        assertTrue(motif2.getStatut().equals(TestDemandeStatutEnum.REFUSEE.name()));
        assertTrue(motif2.getCode().equals(codeMotif2));

        LOGGER.info("REST call: récupération des 2 motifs");

        result = mockMvc.perform(get(adresseWS + "/motifs/" + demarcheId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        MotifDTO[] motifs = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO[].class);

        boolean motif1Found, motif2Found;
        motif1Found = motif2Found = false;

        for (MotifDTO motifDto : motifs) {
            if (motifDto.getPkMotifs() == motifId) {
                motif1Found = true;
            } else if (motifDto.getPkMotifs() == motifId2) {
                motif2Found = true;
            }
        }

        assertTrue(motif1Found && motif2Found);

        LOGGER.info("REST call: suppression du premier motif");

        mockMvc.perform(delete(adresseWS + "/motifs/" + demarcheId + "/" + motifId).with(httpJwtBackUser()))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération du premier motif (la date achivage doit être renseignée)");

        result = mockMvc.perform(get(adresseWS + "/motifs/" + demarcheId + "/" + motifId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        motif = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO.class);

        assertTrue(motif.getDateArchive() != null);

        LOGGER.info("REST call: réactivation du motif");

        motif.setDateArchive(null);

        result = mockMvc
                .perform(post(adresseWS + "/motifs/" + demarcheId + "/" + motifId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(motif)))
                .andExpect(status().isOk()).andReturn();

        motif = mapper.readValue(result.getResponse().getContentAsString(), MotifDTO.class);

        assertTrue(motif.getDateArchive() == null);

    }

    /**
     * Teste la création d'un accès (automatique) pour un usager courrier valide
     * 
     * @throws Exception
     */
    @Test
    public void testCreationAccesUsagerCourrierValide() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jNodeCGU = mapper.createObjectNode();
        ObjectNode jNodeValue = mapper.createObjectNode();
        jNodeCGU.set("CGU", jNodeValue.booleanNode(true));

        LOGGER.info("REST call: création d'un usager courrier");

        UsagerCourrierDTO usagerCourrier = new UsagerCourrierDTO();
        usagerCourrier.setAdresse1(usagerCourrierAdresse1);
        usagerCourrier.setAdresse2(usagerCourrierAdresse2);
        usagerCourrier.setAdresseComplement(usagerCourrierAdresseComplement);
        usagerCourrier.setCodePostal(usagerCourrierCodePostal);
        usagerCourrier.setDemarcheId(demarcheId);
        usagerCourrier.setEmail(usagerCourrierEmail);
        usagerCourrier.setPrenom(usagerCourrierPrenom);
        usagerCourrier.setNom(usagerCourrierNom);
        usagerCourrier.setPays(usagerCourrierPays);
        usagerCourrier.setRaisonSociale(usagerCourrierRaisonSociale);
        usagerCourrier.setTelephone(usagerCourrierTelephone);
        usagerCourrier.setTitre(usagerCourrierTitre);
        usagerCourrier.setVille(usagerCourrierVille);
        usagerCourrier.setAccessContenu(jNodeCGU);

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(usagerCourrier)))
                .andExpect(status().isCreated()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        Integer usagerCourrierId = usagerCourrier.getPkUsagersCourrier();

        LOGGER.info("REST call: récupération de l'accès afin de vérifier qu'il a bien été créé avec le bon contenu...");

        result = mockMvc
                .perform(get(adresseWS + "/accesses/" + demarcheId + "/" + usagerCourrierId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(jNodeCGU));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId().equals(usagerCourrierId));

    }

    /**
     * Teste la création d'un usager courrier sans accessContenu : doit retourner une erreur
     * 
     * @throws Exception
     */
    @Test
    public void testCreationAccesUsagerCourrierInvalide() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("REST call: création d'un usager courrier");

        UsagerCourrierDTO usagerCourrier = new UsagerCourrierDTO();
        usagerCourrier.setAdresse1(usagerCourrierAdresse1);
        usagerCourrier.setAdresse2(usagerCourrierAdresse2);
        usagerCourrier.setAdresseComplement(usagerCourrierAdresseComplement);
        usagerCourrier.setCodePostal(usagerCourrierCodePostal);
        usagerCourrier.setDemarcheId(demarcheId);
        usagerCourrier.setEmail(usagerCourrierEmail);
        usagerCourrier.setPrenom(usagerCourrierPrenom);
        usagerCourrier.setNom(usagerCourrierNom);
        usagerCourrier.setPays(usagerCourrierPays);
        usagerCourrier.setRaisonSociale(usagerCourrierRaisonSociale);
        usagerCourrier.setTelephone(usagerCourrierTelephone);
        usagerCourrier.setTitre(usagerCourrierTitre);
        usagerCourrier.setVille(usagerCourrierVille);

        // Appel mocké au WS Rest
        mockMvc.perform(post(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(usagerCourrier)))
                .andExpect(status().isBadRequest());

    }

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services /usagerscourrier.
     * 
     * @throws Exception
     */
    @Test
    public void testUsagersCourrier() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jNodeCGU = mapper.createObjectNode();
        ObjectNode jNodeValue = mapper.createObjectNode();
        jNodeCGU.set("CGU", jNodeValue.booleanNode(true));

        LOGGER.info("REST call: création d'un usager courrier");

        UsagerCourrierDTO usagerCourrier = new UsagerCourrierDTO();
        usagerCourrier.setAdresse1(usagerCourrierAdresse1);
        usagerCourrier.setAdresse2(usagerCourrierAdresse2);
        usagerCourrier.setAdresseComplement(usagerCourrierAdresseComplement);
        usagerCourrier.setCodePostal(usagerCourrierCodePostal);
        usagerCourrier.setDemarcheId(demarcheId);
        usagerCourrier.setEmail(usagerCourrierEmail);
        usagerCourrier.setPrenom(usagerCourrierPrenom);
        usagerCourrier.setNom(usagerCourrierNom);
        usagerCourrier.setPays(usagerCourrierPays);
        usagerCourrier.setRaisonSociale(usagerCourrierRaisonSociale);
        usagerCourrier.setTelephone(usagerCourrierTelephone);
        usagerCourrier.setTitre(usagerCourrierTitre);
        usagerCourrier.setVille(usagerCourrierVille);
        usagerCourrier.setAccessContenu(jNodeCGU);

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(usagerCourrier)))
                .andExpect(status().isCreated()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        Integer usagerCourrierId = usagerCourrier.getPkUsagersCourrier();

        assertTrue(usagerCourrier.getAdresse1().equals(usagerCourrierAdresse1));
        assertTrue(usagerCourrier.getAdresse2().equals(usagerCourrierAdresse2));
        assertTrue(usagerCourrier.getAdresseComplement().equals(usagerCourrierAdresseComplement));
        assertTrue(usagerCourrier.getCodePostal().equals(usagerCourrierCodePostal));
        assertTrue(usagerCourrier.getDateCreation() != null);
        assertTrue(usagerCourrier.getDateDerModif() != null);
        assertTrue(usagerCourrier.getDateDerModif().equals(usagerCourrier.getDateCreation()));
        assertTrue(usagerCourrier.getDemarcheId().equals(demarcheId));
        assertTrue(usagerCourrier.getEmail().equals(usagerCourrierEmail));
        assertTrue(usagerCourrier.getLogin().equals(usagerCourrier.getPkUsagersCourrier().toString()));
        assertTrue(usagerCourrier.getNom().equals(usagerCourrierNom));
        assertTrue(usagerCourrier.getPays().equals(usagerCourrierPays));
        assertTrue(usagerCourrier.getPrenom().equals(usagerCourrierPrenom));
        assertTrue(usagerCourrier.getRaisonSociale().equals(usagerCourrierRaisonSociale));
        assertTrue(usagerCourrier.getTelephone().equals(usagerCourrierTelephone));
        assertTrue(usagerCourrier.getTitre().equals(usagerCourrierTitre));
        assertTrue(usagerCourrier.getVille().equals(usagerCourrierVille));
        assertTrue(usagerCourrierId != null);
        assertTrue(usagerCourrierId > DemarchesUtils.USAGERID_OFFSET);

        LOGGER.info("REST call: récupération de l'usager courrier");

        result = mockMvc.perform(
                get(adresseWS + "/usagerscourrier/" + demarcheId + "/" + usagerCourrierId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        assertTrue(usagerCourrier.getAdresse1().equals(usagerCourrierAdresse1));
        assertTrue(usagerCourrier.getAdresse2().equals(usagerCourrierAdresse2));
        assertTrue(usagerCourrier.getAdresseComplement().equals(usagerCourrierAdresseComplement));
        assertTrue(usagerCourrier.getCodePostal().equals(usagerCourrierCodePostal));
        assertTrue(usagerCourrier.getDateCreation() != null);
        assertTrue(usagerCourrier.getDateDerModif() != null);
        assertTrue(usagerCourrier.getDateDerModif().equals(usagerCourrier.getDateCreation()));
        assertTrue(usagerCourrier.getDemarcheId().equals(demarcheId));
        assertTrue(usagerCourrier.getEmail().equals(usagerCourrierEmail));
        assertTrue(usagerCourrier.getLogin().equals(usagerCourrier.getPkUsagersCourrier().toString()));
        assertTrue(usagerCourrier.getNom().equals(usagerCourrierNom));
        assertTrue(usagerCourrier.getPays().equals(usagerCourrierPays));
        assertTrue(usagerCourrier.getPrenom().equals(usagerCourrierPrenom));
        assertTrue(usagerCourrier.getRaisonSociale().equals(usagerCourrierRaisonSociale));
        assertTrue(usagerCourrier.getTelephone().equals(usagerCourrierTelephone));
        assertTrue(usagerCourrier.getTitre().equals(usagerCourrierTitre));
        assertTrue(usagerCourrier.getVille().equals(usagerCourrierVille));
        assertTrue(usagerCourrier.getPkUsagersCourrier() != null);
        assertTrue(usagerCourrier.getPkUsagersCourrier() > DemarchesUtils.USAGERID_OFFSET);
        assertEquals(0, usagerCourrier.getNbDemandes());

        LOGGER.info("REST call: modification de l'usager courrier");

        usagerCourrier.setAdresse1(usagerCourrierAdresse1 + "a");
        usagerCourrier.setAdresse2(usagerCourrierAdresse2 + "a");
        usagerCourrier.setAdresseComplement(usagerCourrierAdresseComplement + "a");
        usagerCourrier.setCodePostal(usagerCourrierCodePostal + "a");
        usagerCourrier.setDemarcheId(demarcheId + "a");
        usagerCourrier.setEmail(usagerCourrierEmail + "a");
        usagerCourrier.setPrenom(usagerCourrierPrenom + "a");
        usagerCourrier.setNom(usagerCourrierNom + "a");
        usagerCourrier.setPays(usagerCourrierPays2);
        usagerCourrier.setRaisonSociale(usagerCourrierRaisonSociale + "a");
        usagerCourrier.setTelephone(usagerCourrierTelephone + "a");
        usagerCourrier.setTitre(usagerCourrierTitre + 1);
        usagerCourrier.setVille(usagerCourrierVille + "a");

        result = mockMvc
                .perform(post(adresseWS + "/usagerscourrier/" + demarcheId + "/" + usagerCourrierId)
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(usagerCourrier)))
                .andExpect(status().isOk()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        assertTrue(usagerCourrier.getAdresse1().equals(usagerCourrierAdresse1 + "a"));
        assertTrue(usagerCourrier.getAdresse2().equals(usagerCourrierAdresse2 + "a"));
        assertTrue(usagerCourrier.getAdresseComplement().equals(usagerCourrierAdresseComplement + "a"));
        assertTrue(usagerCourrier.getCodePostal().equals(usagerCourrierCodePostal + "a"));
        assertTrue(usagerCourrier.getDateCreation() != null);
        assertTrue(usagerCourrier.getDateDerModif() != null);
        assertTrue(usagerCourrier.getDateDerModif().after(usagerCourrier.getDateCreation()));
        assertTrue(usagerCourrier.getDemarcheId().equals(demarcheId)); // Pas de changement
        assertTrue(usagerCourrier.getEmail().equals(usagerCourrierEmail + "a"));
        assertTrue(usagerCourrier.getLogin().equals(usagerCourrier.getPkUsagersCourrier().toString()));
        assertTrue(usagerCourrier.getNom().equals(usagerCourrierNom + "a"));
        assertTrue(usagerCourrier.getPays().equals(usagerCourrierPays2));
        assertTrue(usagerCourrier.getPrenom().equals(usagerCourrierPrenom + "a"));
        assertTrue(usagerCourrier.getRaisonSociale().equals(usagerCourrierRaisonSociale + "a"));
        assertTrue(usagerCourrier.getTelephone().equals(usagerCourrierTelephone + "a"));
        assertTrue(usagerCourrier.getTitre().equals(usagerCourrierTitre + 1));
        assertTrue(usagerCourrier.getVille().equals(usagerCourrierVille + "a"));
        assertTrue(usagerCourrier.getPkUsagersCourrier().equals(usagerCourrierId));

        LOGGER.info("REST call: création d'une demande relative à cet accès 1");

        JsonNode contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerCourrierId
                        + "&premierStatut=" + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        LOGGER.info(
                "Récupération de l'usager courrier afin de constater que le compteur de demandes a bien été incrémenté");

        result = mockMvc.perform(
                get(adresseWS + "/usagerscourrier/" + demarcheId + "/" + usagerCourrierId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        assertEquals(1, usagerCourrier.getNbDemandes());

        LOGGER.info("REST call: création d'un deuxième usager courrier");

        UsagerCourrierDTO usagerCourrier2 = new UsagerCourrierDTO();
        usagerCourrier2.setAdresse1(usagerCourrierAdresse1);
        usagerCourrier2.setAdresse2(usagerCourrierAdresse2);
        usagerCourrier2.setAdresseComplement(usagerCourrierAdresseComplement);
        usagerCourrier2.setCodePostal(usagerCourrierCodePostal);
        usagerCourrier2.setDemarcheId(demarcheId);
        usagerCourrier2.setEmail(usagerCourrierEmail);
        usagerCourrier2.setLogin(usagerCourrier2Login);
        usagerCourrier2.setPrenom(usagerCourrier2Prenom);
        usagerCourrier2.setNom(usagerCourrier2Nom);
        usagerCourrier2.setPays(usagerCourrierPays);
        usagerCourrier2.setRaisonSociale(usagerCourrierRaisonSociale);
        usagerCourrier2.setTelephone(usagerCourrierTelephone);
        usagerCourrier2.setTitre(usagerCourrierTitre);
        usagerCourrier2.setVille(usagerCourrierVille);
        usagerCourrier2.setAccessContenu(jNodeCGU);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(usagerCourrier2)))
                .andExpect(status().isCreated()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        Integer usagerCourrierId2 = usagerCourrier.getPkUsagersCourrier();

        LOGGER.info("REST call: récupération des 2 usagers courrier");

        result = mockMvc.perform(get(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        UsagerCourrierDTO[] usagersCourrier = mapper.readValue(result.getResponse().getContentAsString(),
                UsagerCourrierDTO[].class);

        boolean usagerCourrier1Found, usagerCourrier2Found;
        usagerCourrier1Found = usagerCourrier2Found = false;

        for (UsagerCourrierDTO usagerCourrierDto : usagersCourrier) {
            if (usagerCourrierDto.getPkUsagersCourrier().equals(usagerCourrierId)) {
                usagerCourrier1Found = true;
            } else if (usagerCourrierDto.getPkUsagersCourrier().equals(usagerCourrierId2)) {
                usagerCourrier2Found = true;
            }
        }

        assertTrue(usagerCourrier1Found && usagerCourrier2Found);

        LOGGER.info("REST call: suppression du premier usager courrier");

        mockMvc.perform(
                delete(adresseWS + "/usagerscourrier/" + demarcheId + "/" + usagerCourrierId).with(httpJwtBackUser()))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération du premier usager courrier (erreur attendue)");

        mockMvc.perform(
                get(adresseWS + "/usagerscourrier/" + demarcheId + "/" + usagerCourrierId).with(httpJwtBackUser()))
                .andExpect(status().isNotFound());

    }

    /**
     * 
     * Teste les WS pour la manipulation des démarches.
     * 
     * @throws Exception
     */
    @Test
    public void testDemarches() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("REST call: récupération d'une démarche");

        MvcResult result = mockMvc.perform(get(adresseWS + "/demarches/" + demarcheId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        DemarcheDTO demarche = mapper.readValue(result.getResponse().getContentAsString(), DemarcheDTO.class);

        assertTrue(demarche.getPkDemarches().equals(demarcheId));
        assertTrue(demarche.getEmailService().equals(emailService));

        LOGGER.info("REST call: modification d'une démarche");

        DemarcheInputDTO demarcheInput = new DemarcheInputDTO();
        demarcheInput.setEmailService(emailService2);

        result = mockMvc
                .perform(post(adresseWS + "/demarches/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demarcheInput)))
                .andExpect(status().isOk()).andReturn();

        demarche = mapper.readValue(result.getResponse().getContentAsString(), DemarcheDTO.class);

        assertTrue(demarche.getPkDemarches().equals(demarcheId));
        assertTrue(demarche.getEmailService().equals(emailService2));

    }

    /**
     * Teste la récupération des demandes uniquement par demarcheId, quelque soit donc l'accès
     * 
     * @throws Exception
     */
    @Test
    public void testGetDemandesParDemarcheId() throws Exception {

        LOGGER.info("Access object creation...");

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès 1");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId11).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId11);

        LOGGER.info("REST call: création d'une demande relative à cet accès 1");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId11 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId1 = demande.getPkDemandes();

        LOGGER.info("REST call: création d'un accès 2");

        accessInput.setContenu(contenu);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId12).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId12);

        LOGGER.info("REST call: création d'une demande relative à cet accès 2");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId12 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId2 = demande.getPkDemandes();

        LOGGER.info("REST call: récupération de toutes les démarches");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeDTO[] demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);
        boolean d1found, d2found;
        d1found = d2found = false;
        for (DemandeDTO d : demandes) {
            if (d.getPkDemandes() == demandeId1) {
                d1found = true;
            } else if (d.getPkDemandes() == demandeId2) {
                d2found = true;
            }
        }
        assertTrue(d1found && d2found);

    }

    /**
     * Teste la création de lignes d'historique et la récupération d'historique
     * 
     * @throws Exception
     */
    @Test
    public void testHistorique() throws Exception {

        LOGGER.info("Access object creation...");

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès 1");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId13).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        LOGGER.info("REST call: création d'une demande relative à cet accès 1");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId13 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtBackUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: création d'une ligne d'historique pour cette demande");

        DemandeHistoriqueDTO historique = new DemandeHistoriqueDTO();
        historique.setAgentId(histoAgentId);
        JsonNode histoContenu = mapper.readTree("{ \"d10\":\"d11\" }");
        historique.setContenu(histoContenu);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/historique")
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(historique)))
                .andExpect(status().isCreated()).andReturn();

        historique = mapper.readValue(result.getResponse().getContentAsString(), DemandeHistoriqueDTO.class);

        assertTrue(historique != null);
        assertTrue(historique.getAgentId().equals(histoAgentId));
        assertTrue(historique.getUsagerId() == null);
        assertTrue(historique.getDate() != null);
        assertTrue(historique.getFkDemandes() == demandeId);
        assertTrue(historique.getFkStatut().getPkStatut().equals(demande.getDernierStatut().getPkStatut()));
        assertTrue(historique.getContenu().equals(histoContenu));

        LOGGER.info("REST call: création d'une 2ème ligne d'historique pour cette demande");

        DemandeHistoriqueDTO historique2 = new DemandeHistoriqueDTO();
        historique2.setUsagerId(histoUsagerId);
        JsonNode histoContenu2 = mapper.readTree("{ \"d12\":\"d13\" }");
        historique2.setContenu(histoContenu2);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/historique")
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(historique2)))
                .andExpect(status().isCreated()).andReturn();

        historique2 = mapper.readValue(result.getResponse().getContentAsString(), DemandeHistoriqueDTO.class);

        assertTrue(historique2 != null);
        assertTrue(historique2.getAgentId() == null);
        assertTrue(historique2.getUsagerId().equals(histoUsagerId));
        assertTrue(historique2.getDate() != null);
        assertTrue(historique2.getFkDemandes() == demandeId);
        assertTrue(historique2.getFkStatut().getPkStatut().equals(demande.getDernierStatut().getPkStatut()));
        assertTrue(historique2.getContenu().equals(histoContenu2));

        LOGGER.info("REST call: récupération de tout l'historique de la demande");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/historique").with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeHistoriqueDTO[] histos = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeHistoriqueDTO[].class);

        boolean histo1Found, histo2Found;
        histo1Found = histo2Found = false;

        for (DemandeHistoriqueDTO histo : histos) {
            if (histo.getPkDemandeHistorique() == historique.getPkDemandeHistorique()) {
                histo1Found = true;
            }
            if (histo.getPkDemandeHistorique() == historique2.getPkDemandeHistorique()) {
                histo2Found = true;
            }
        }

        assertTrue(histo1Found && histo2Found);

    }

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services /templates.
     * 
     * @throws Exception
     */
    @Test
    public void testTemplates() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("REST call: création d'un template");

        TemplateDTO template = new TemplateDTO();
        template.setCode(template1Code);
        template.setContenu(template1Contenu);
        template.setLangue(langueFR);

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/templates/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(template)))
                .andExpect(status().isCreated()).andReturn();

        template = mapper.readValue(result.getResponse().getContentAsString(), TemplateDTO.class);

        Integer templateId = template.getPkTemplates();

        assertTrue(template.getDemarcheId().equals(demarcheId));
        assertTrue(template.getLangue().equals(langueFR));
        assertTrue(template.getCode().equals(template1Code));
        assertTrue(template.getContenu().equals(template1Contenu));

        LOGGER.info("REST call: récupération du template");

        result = mockMvc.perform(get(adresseWS + "/templates/" + demarcheId + "/" + templateId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        template = mapper.readValue(result.getResponse().getContentAsString(), TemplateDTO.class);

        assertTrue(template.getDemarcheId().equals(demarcheId));
        assertTrue(template.getLangue().equals(langueFR));
        assertTrue(template.getCode().equals(template1Code));
        assertTrue(template.getContenu().equals(template1Contenu));

        LOGGER.info("REST call: modification du template");

        template.setCode(template1Code + "aaa");
        template.setContenu(template1Contenu + "bbb");
        template.setLangue(langueFR);

        result = mockMvc
                .perform(post(adresseWS + "/templates/" + demarcheId + "/" + templateId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(template)))
                .andExpect(status().isOk()).andReturn();

        template = mapper.readValue(result.getResponse().getContentAsString(), TemplateDTO.class);

        assertTrue(template.getDemarcheId().equals(demarcheId));
        assertTrue(template.getLangue().equals(langueFR));
        assertTrue(template.getCode().equals(template1Code + "aaa"));
        assertTrue(template.getContenu().equals(template1Contenu + "bbb"));

        LOGGER.info("REST call: création d'un deuxième template");

        TemplateDTO template2 = new TemplateDTO();
        template2.setCode(template2Code);
        template2.setContenu(template2Contenu);
        template2.setLangue(langueFR);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/templates/" + demarcheId).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(template2)))
                .andExpect(status().isCreated()).andReturn();

        template2 = mapper.readValue(result.getResponse().getContentAsString(), TemplateDTO.class);

        Integer templateId2 = template2.getPkTemplates();

        assertTrue(template2.getDemarcheId().equals(demarcheId));
        assertTrue(template2.getLangue().equals(langueFR));
        assertTrue(template2.getCode().equals(template2Code));
        assertTrue(template2.getContenu().equals(template2Contenu));

        LOGGER.info("REST call: récupération du 2ème template par demarcheId, code et langue");

        result = mockMvc
                .perform(get(adresseWS + "/templates/" + demarcheId + "?code=" + template2Code + "&langue=" + langueFR)
                        .with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        TemplateDTO[] templates = mapper.readValue(result.getResponse().getContentAsString(), TemplateDTO[].class);

        assertTrue(templates.length == 1);
        assertTrue(templates[0].getDemarcheId().equals(demarcheId));
        assertTrue(templates[0].getLangue().equals(langueFR));
        assertTrue(templates[0].getCode().equals(template2Code));
        assertTrue(templates[0].getContenu().equals(template2Contenu));

        LOGGER.info("REST call: récupération des 2 templates");

        result = mockMvc.perform(get(adresseWS + "/templates/" + demarcheId).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        templates = mapper.readValue(result.getResponse().getContentAsString(), TemplateDTO[].class);

        boolean template1Found, template2Found;
        template1Found = template2Found = false;

        for (TemplateDTO templateDto : templates) {
            if (templateDto.getPkTemplates() == templateId) {
                template1Found = true;
            } else if (templateDto.getPkTemplates() == templateId2) {
                template2Found = true;
            }
        }

        assertTrue(template1Found && template2Found);

        LOGGER.info("REST call: suppression du premier template");

        mockMvc.perform(delete(adresseWS + "/templates/" + demarcheId + "/" + templateId).with(httpJwtBackUser()))
                .andExpect(status().isOk());

        LOGGER.info("REST call: récupération du premier template (erreur attendue)");

        mockMvc.perform(get(adresseWS + "/templates/" + demarcheId + "/" + templateId).with(httpJwtBackUser()))
                .andExpect(status().isNotFound());

    }

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services relatifs à la manipulation des données de
     * demandes.
     * 
     * @throws Exception
     */
    @Test
    public void testDemandesData() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest

        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId14).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId14);

        LOGGER.info("REST call: création d'une demande relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId14 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtBackUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId14);
        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: création d'une donnée de demande");

        DemandeDataDTO demandeDataDto = new DemandeDataDTO();
        demandeDataDto.setValue(value1);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params/" + key1)
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(demandeDataDto)))
                .andExpect(status().isCreated()).andReturn();

        demandeDataDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeDataDTO.class);

        assertTrue(demandeDataDto != null);
        assertTrue(demandeDataDto.getKey().equals(key1));
        assertTrue(demandeDataDto.getValue().equals(value1));
        assertTrue(demandeDataDto.getDemandeId().equals(demandeId));
        assertTrue(demandeDataDto.getPkDemandesData() != null);

        LOGGER.info("REST call: récupération de cette donnée de demande");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params/" + key1)
                .with(httpJwtBackUser())).andExpect(status().isOk()).andReturn();

        demandeDataDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeDataDTO.class);

        assertTrue(demandeDataDto != null);
        assertTrue(demandeDataDto.getKey().equals(key1));
        assertTrue(demandeDataDto.getValue().equals(value1));
        assertTrue(demandeDataDto.getDemandeId().equals(demandeId));
        assertTrue(demandeDataDto.getPkDemandesData() != null);

        LOGGER.info("REST call: création d'une 2ème donnée de demande");

        demandeDataDto = new DemandeDataDTO();
        demandeDataDto.setValue(value2);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params/" + key2)
                        .with(httpJwtBackUser()).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(demandeDataDto)))
                .andExpect(status().isCreated()).andReturn();

        demandeDataDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeDataDTO.class);

        assertTrue(demandeDataDto != null);

        LOGGER.info("REST call: récupération des deux données de demande");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params").with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeDataDTO[] demarcheParams = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeDataDTO[].class);

        boolean demarcheParam1Found, demarcheParam2Found;
        demarcheParam1Found = demarcheParam2Found = false;

        for (DemandeDataDTO dpDto : demarcheParams) {
            if (dpDto.getKey().equals(key1)) {
                demarcheParam1Found = true;
            } else if (dpDto.getKey().equals(key2)) {
                demarcheParam2Found = true;
            }
        }
        assertTrue(demarcheParam1Found && demarcheParam2Found);

        LOGGER.info("REST call: suppression de la 1ère donnée de demande");

        mockMvc.perform(delete(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params/" + key1)
                .with(httpJwtBackUser())).andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la 1ère donnée de demande (erreur attendue)");

        mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/params/" + key1)
                .with(httpJwtBackUser())).andExpect(status().isNotFound());
    }

    @Test
    public void testRechercheDemandes() throws JsonProcessingException, Exception {

        // Non utilisation du spring filter chain et donc du basic auth
        // On set à la main le principal
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Définition de l'authentification FRONT
        UsernamePasswordAuthenticationToken principalFront = (UsernamePasswordAuthenticationToken) MockSecurityContext
                .getPrincipal(TestUtils.FRONT_USER_NAME, TestUtils.FRONT_USER_PWD);

        MockHttpSession sessionFront = new MockHttpSession();
        sessionFront.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new MockSecurityContext(principalFront));

        // Définition de l'authentification BACK
        UsernamePasswordAuthenticationToken principalBack = (UsernamePasswordAuthenticationToken) MockSecurityContext
                .getPrincipal(TestUtils.BACK_USER_NAME, TestUtils.BACK_USER_PWD);

        MockHttpSession sessionBack = new MockHttpSession();
        sessionBack.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new MockSecurityContext(principalBack));

        SecurityContextHolder.getContext().setAuthentication(principalFront);

        LOGGER.info("REST call: création d'un accès");

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId2 + "/" + usagerId15).principal(principalFront)
                        .session(sessionFront).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId2));
        assertTrue(access.getUsagerId() == usagerId15);

        LOGGER.info("REST call: création d'une demande 1 relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId2 + "?usagerId=" + usagerId15 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).principal(principalFront).session(sessionFront)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId1 = demande.getPkDemandes();

        LOGGER.info("REST call: modification de la demande 1 pour y mettre des observations");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        // + mise à jour des champs suivants :
        demandeInput.setObservations(observations);

        SecurityContextHolder.getContext().setAuthentication(principalBack);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId2 + "/" + demandeId1).principal(principalBack)
                        .session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isOk()).andReturn();

        LOGGER.info("REST call: création d'une deuxième demande relative à cet accès");

        contenu = mapper.readTree("{ \"d7\":\"d8\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(DemandeCanalEnum.COURRIER);

        SecurityContextHolder.getContext().setAuthentication(principalFront);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId2 + "?usagerId=" + usagerId15 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).principal(principalFront).session(sessionFront)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId2 = demande.getPkDemandes();

        LOGGER.info("REST call: passage de la demande 2 à EN_COURS_TRAIT");

        SecurityContextHolder.getContext().setAuthentication(principalBack);

        StatutInputDTO statutInput = new StatutInputDTO();
        statutInput.setStatut(TestDemandeStatutEnum.EN_COURS_TRAIT.name());
        statutInput.setAgentId(agentId1);

        SecurityContextHolder.getContext().setAuthentication(principalBack);

        mockMvc.perform(post(adresseWS + "/demandes/" + demarcheId2 + "/" + demandeId2 + "/statuts")
                .principal(principalBack).session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(statutInput))).andExpect(status().isCreated());

        LOGGER.info("REST call: affectation de la demande 2 à un agent de l'état");

        demandeInput = new DemandeInputDTO();
        demandeInput.setAgentAffecteId(agentId1);

        mockMvc.perform(post(adresseWS + "/demandes/" + demarcheId2 + "/" + demandeId2).principal(principalBack)
                .session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(demandeInput))).andExpect(status().isOk());

        LOGGER.info("REST call: récupération de la demande pour vérification de son statut");

        result = mockMvc
                .perform(get(adresseWS + "/demandes/" + demarcheId2 + "/" + demandeId2).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // La demande doit être dans le statut "En cours de traitement"
        DemandeStatutDTO statut = DemarchesUtils.getLatestStatus(demande);
        assertTrue(statut.getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));
        assertTrue(statut.getUsagerId() == null);
        assertTrue(statut.getAgentId().equals(agentId1));
        // Vérification également du champ "dernierStatut" de la demande
        assertTrue(demande.getDernierStatut().getLibelle().equals(TestDemandeStatutEnum.EN_COURS_TRAIT.name()));
        // Vérification également du champ "agentAffecte" de la demande
        assertTrue(demande.getAgentAffecteId().equals(agentId1));
        assertTrue(demande.getCanal().equals(DemandeCanalEnum.COURRIER));

        LOGGER.info("REST call: récupération de la demande 2 par agent affecté");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId2 + "?agentId=" + agentId1)
                .principal(principalBack).session(sessionBack)).andExpect(status().isOk()).andReturn();

        DemandeDTO[] demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 1);
        assertTrue(demandes[0].getPkDemandes().equals(demandeId2));
        assertTrue(demandes[0].getAgentAffecteId().equals(agentId1));

        LOGGER.info("REST call: récupération de la demande 2 par dernier statut");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId2 + "?statut=" + TestDemandeStatutEnum.EN_COURS_TRAIT.name())
                        .principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 1);
        assertTrue(demandes[0].getPkDemandes().equals(demandeId2));

        LOGGER.info("REST call: récupération de la demande 2 par canal");

        result = mockMvc
                .perform(get(adresseWS + "/demandes/" + demarcheId2 + "?canal=" + DemandeCanalEnum.COURRIER.name())
                        .principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 1);
        assertTrue(demandes[0].getPkDemandes().equals(demandeId2));

        LOGGER.info("REST call: récupération des deux demandes par dernier statut");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId2 + "?statut=" + TestDemandeStatutEnum.EN_COURS_TRAIT.name()
                        + "&statut=" + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).principal(principalBack)
                                .session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 2);

        LOGGER.info("REST call: récupération de la demande 1 par recherche textuelle sur les observations");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId2 + "?texte=limite").principal(principalBack)
                .session(sessionBack)).andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 1);
        assertTrue(demandes[0].getPkDemandes().equals(demandeId1));

        LOGGER.info("REST call: récupération des deux demandes par date");

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -1);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 1);

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId2 + "?creationStartDate="
                + sdf.format(startDate.getTime()) + "&creationEndDate=" + sdf.format(endDate.getTime()))
                        .principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 2);

        LOGGER.info("REST call: récupération des deux demandes par dates inversées (ne doit rien retourner)");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId2 + "?creationStartDate="
                + sdf.format(endDate.getTime()) + "&creationEndDate=" + sdf.format(startDate.getTime()))
                        .principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 0);

        LOGGER.info("REST call: récupération de la 1ère demande par combinaison de critères");

        result = mockMvc
                .perform(get(
                        adresseWS + "/demandes/" + demarcheId2 + "?creationStartDate=" + sdf.format(startDate.getTime())
                                + "&creationEndDate=" + sdf.format(endDate.getTime()) + "&texte=limite&statut="
                                + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name() + "&usagerId=" + usagerId15)
                                        .principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 1);
        assertTrue(demandes[0].getPkDemandes().equals(demandeId1));

    }

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services relatifs à la manipulation des courriers.
     * 
     * @throws Exception
     */
    @Test
    public void testDemandesCourriers() throws Exception {

        // Non utilisation du spring filter chain et donc du basic auth
        // On set à la main le principal
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ObjectMapper mapper = new ObjectMapper();

        // Définition de l'authentification BACK
        UsernamePasswordAuthenticationToken principalBack = (UsernamePasswordAuthenticationToken) MockSecurityContext
                .getPrincipal(TestUtils.BACK_USER_NAME, TestUtils.BACK_USER_PWD);

        MockHttpSession sessionBack = new MockHttpSession();
        sessionBack.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new MockSecurityContext(principalBack));

        SecurityContextHolder.getContext().setAuthentication(principalBack);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest

        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId16).principal(principalBack)
                        .session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId16);

        LOGGER.info("REST call: création d'une demande relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId16 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).principal(principalBack).session(sessionBack)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId16);
        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: création d'un courrier");

        DemandeCourrierDTO demandeCourrierDto = new DemandeCourrierDTO();
        demandeCourrierDto.setName(courrier1Name);
        demandeCourrierDto.setUrl(courrier1Url);
        demandeCourrierDto.setMeta(courrier1Meta);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers")
                        .principal(principalBack).session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(demandeCourrierDto)))
                .andExpect(status().isCreated()).andReturn();

        demandeCourrierDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeCourrierDTO.class);

        assertTrue(demandeCourrierDto != null);
        assertTrue(demandeCourrierDto.getName().equals(courrier1Name));
        assertTrue(demandeCourrierDto.getUrl().equals(courrier1Url));
        assertTrue(demandeCourrierDto.getMeta().equals(courrier1Meta));
        assertTrue(demandeCourrierDto.getIdentifiant() == null);
        assertTrue(demandeCourrierDto.getDatePrinted() == null);
        assertTrue(demandeCourrierDto.getFkStatut().getLibelle().equals(demande.getDernierStatut().getLibelle()));
        assertTrue(demandeCourrierDto.getDemandeId().equals(demandeId));
        assertTrue(demandeCourrierDto.getPkCourrier() != null);
        assertTrue(demandeCourrierDto.getDateCreation() != null);

        Integer courrierId1 = demandeCourrierDto.getPkCourrier();

        LOGGER.info("REST call: récupération de ce courrier");

        result = mockMvc
                .perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers/" + courrierId1)
                        .principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        demandeCourrierDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeCourrierDTO.class);

        assertTrue(demandeCourrierDto != null);
        assertTrue(demandeCourrierDto.getPkCourrier().equals(courrierId1));

        LOGGER.info("REST call: mise à jour de l'identifiant de ce courrier");

        demandeCourrierDto.setIdentifiant(courrier1Identifiant);

        // Appel mocké au WS Rest
        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers/" + courrierId1)
                        .principal(principalBack).session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(demandeCourrierDto)))
                .andExpect(status().isCreated()).andReturn();

        demandeCourrierDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeCourrierDTO.class);

        assertTrue(demandeCourrierDto != null);
        assertTrue(demandeCourrierDto.getName().equals(courrier1Name));
        assertTrue(demandeCourrierDto.getUrl().equals(courrier1Url));
        assertTrue(demandeCourrierDto.getMeta().equals(courrier1Meta));
        assertTrue(demandeCourrierDto.getIdentifiant().equals(courrier1Identifiant));
        assertTrue(demandeCourrierDto.getDatePrinted() == null);
        assertTrue(demandeCourrierDto.getFkStatut().getLibelle().equals(demande.getDernierStatut().getLibelle()));
        assertTrue(demandeCourrierDto.getDemandeId().equals(demandeId));
        assertTrue(demandeCourrierDto.getPkCourrier().equals(courrierId1));

        LOGGER.info("REST call: création d'un 2ème courrier");

        demandeCourrierDto = new DemandeCourrierDTO();
        demandeCourrierDto.setName(courrier2Name);
        demandeCourrierDto.setUrl(courrier2Url);
        demandeCourrierDto.setMeta(courrier2Meta);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers")
                        .principal(principalBack).session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(demandeCourrierDto)))
                .andExpect(status().isCreated()).andReturn();

        demandeCourrierDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeCourrierDTO.class);

        assertTrue(demandeCourrierDto != null);

        Integer courrierId2 = demandeCourrierDto.getPkCourrier();

        LOGGER.info("REST call: récupération des deux courriers");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers")
                .principal(principalBack).session(sessionBack)).andExpect(status().isOk()).andReturn();

        DemandeCourrierDTO[] courriers = mapper.readValue(result.getResponse().getContentAsString(),
                DemandeCourrierDTO[].class);

        boolean courrier1Found, courrier2Found;
        courrier1Found = courrier2Found = false;

        for (DemandeCourrierDTO dcDto : courriers) {
            if (dcDto.getPkCourrier().equals(courrierId1)) {
                courrier1Found = true;
            } else if (dcDto.getPkCourrier().equals(courrierId2)) {
                courrier2Found = true;
            }
        }
        assertTrue(courrier1Found && courrier2Found);

        LOGGER.info("REST call: impression du premier courrier");

        result = mockMvc.perform(
                put(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/courriers/" + courrierId1 + "/print")
                        .principal(principalBack).session(sessionBack).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString("")))
                .andExpect(status().isOk()).andReturn();

        demandeCourrierDto = mapper.readValue(result.getResponse().getContentAsString(), DemandeCourrierDTO.class);

        assertTrue(demandeCourrierDto.getDatePrinted() != null);

        LOGGER.info("REST call: récupération des deux courriers par démarche");

        result = mockMvc
                .perform(get(adresseWS + "/courriers/" + demarcheId).principal(principalBack).session(sessionBack))
                .andExpect(status().isOk()).andReturn();

        courriers = mapper.readValue(result.getResponse().getContentAsString(), DemandeCourrierDTO[].class);

        courrier1Found = courrier2Found = false;

        for (DemandeCourrierDTO dcDto : courriers) {
            if (dcDto.getPkCourrier().equals(courrierId1)) {
                courrier1Found = true;
            } else if (dcDto.getPkCourrier().equals(courrierId2)) {
                courrier2Found = true;
            }
        }
        assertTrue(courrier1Found && courrier2Found);
    }

    /**
     * Teste la fonctionnalité d'association/rattachement de demandes courriers à un usager téléservice
     * 
     * @throws Exception
     */
    @Test
    public void testAssociationDemandeCourrier() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId17).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId17);

        LOGGER.info("REST call: création d'une demande relative à cet accès");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(DemandeCanalEnum.COURRIER);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId17 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande.getContenu().equals(contenu));
        assertTrue(demande.getDemarcheId().equals(demarcheId));
        assertTrue(demande.getUsagerId() == usagerId17);
        assertTrue(demande.getCanal().equals(DemandeCanalEnum.COURRIER));
        Integer demandeId = demande.getPkDemandes();

        LOGGER.info("REST call: création d'un deuxième accès");

        accessInput = new AccessInputDTO();
        contenu = mapper.readTree("{ \"d1\":\"d10\" }");
        accessInput.setContenu(contenu);

        result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId18).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getContenu().equals(contenu));
        assertTrue(access.getDemarcheId().equals(demarcheId));
        assertTrue(access.getUsagerId() == usagerId18);

        LOGGER.info(
                "REST call: association de la demande courrier à l'accès de l'usager téléservice qui vient d'être créé");

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId + "/associerDemandeCourrier/"
                        + access.getPkAccess()).with(httpJwtFrontUser()).contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isOk()).andReturn();

        demande = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        // Vérifier qu'il s'agit bien de la même demande
        assertTrue(demande.getPkDemandes().equals(demandeId));
        // Passage du canal de COURRIER à GUICHET_VIRTUEL
        assertTrue(demande.getCanal().equals(DemandeCanalEnum.GUICHET_VIRTUEL));
        // Passage de l'accès au nouvel accès
        assertTrue(demande.getFkAccess().equals(access.getPkAccess()));

    }

    /**
     * Teste la fonctionnalité de transfert de demandes entre usagers courrier
     * 
     * @throws Exception
     */
    @Test
    public void testTransfertDemandesUsagersCourrier() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");

        LOGGER.info("REST call: création d'un premier usager courrier");

        UsagerCourrierDTO usagerCourrierACreer = new UsagerCourrierDTO();
        usagerCourrierACreer.setAdresse1(usagerCourrierAdresse1);
        usagerCourrierACreer.setAdresse2(usagerCourrierAdresse2);
        usagerCourrierACreer.setAdresseComplement(usagerCourrierAdresseComplement);
        usagerCourrierACreer.setCodePostal(usagerCourrierCodePostal);
        usagerCourrierACreer.setDemarcheId(demarcheId);
        usagerCourrierACreer.setEmail(usagerCourrierEmail);
        usagerCourrierACreer.setPrenom(usagerCourrierPrenom);
        usagerCourrierACreer.setNom(usagerCourrierNom);
        usagerCourrierACreer.setPays(usagerCourrierPays);
        usagerCourrierACreer.setRaisonSociale(usagerCourrierRaisonSociale);
        usagerCourrierACreer.setTelephone(usagerCourrierTelephone);
        usagerCourrierACreer.setTitre(usagerCourrierTitre);
        usagerCourrierACreer.setVille(usagerCourrierVille);
        usagerCourrierACreer.setAccessContenu(contenu);

        // Appel mocké au WS Rest
        MvcResult result = mockMvc.perform(post(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(usagerCourrierACreer)))
                .andExpect(status().isCreated()).andReturn();

        UsagerCourrierDTO usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(),
                UsagerCourrierDTO.class);

        Integer usagerCourrierId1 = usagerCourrier.getPkUsagersCourrier();

        LOGGER.info("REST call: création d'un deuxième usager courrier");

        // Appel mocké au WS Rest
        result = mockMvc.perform(post(adresseWS + "/usagerscourrier/" + demarcheId).with(httpJwtBackUser())
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(usagerCourrierACreer)))
                .andExpect(status().isCreated()).andReturn();

        usagerCourrier = mapper.readValue(result.getResponse().getContentAsString(), UsagerCourrierDTO.class);

        Integer usagerCourrierId2 = usagerCourrier.getPkUsagersCourrier();

        LOGGER.info("REST call: création de deux demandes pour l'usager courrier 1");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setCanal(DemandeCanalEnum.COURRIER);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerCourrierId1
                        + "&premierStatut=" + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande1 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande1.getContenu().equals(contenu));
        assertTrue(demande1.getDemarcheId().equals(demarcheId));
        assertTrue(demande1.getUsagerId().equals(usagerCourrierId1));
        assertTrue(demande1.getCanal().equals(DemandeCanalEnum.COURRIER));
        Integer demandeId1 = demande1.getPkDemandes();

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerCourrierId1
                        + "&premierStatut=" + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande2 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertTrue(demande2.getContenu().equals(contenu));
        assertTrue(demande2.getDemarcheId().equals(demarcheId));
        assertTrue(demande2.getUsagerId().equals(usagerCourrierId1));
        assertTrue(demande2.getCanal().equals(DemandeCanalEnum.COURRIER));
        Integer demandeId2 = demande2.getPkDemandes();

        LOGGER.info("REST call: transfert de l'usager courrier 1 à l'usager courrier 2");

        mockMvc.perform(post(adresseWS + "/usagerscourrier/" + demarcheId + "/transferer/" + usagerCourrierId1 + "/"
                + usagerCourrierId2 + "?demandeIds=" + demandeId1 + "&demandeIds=" + demandeId2).with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isOk()).andReturn();

        LOGGER.info("REST call: récupération des demandes de l'usager courrier 1 et vérifier qu'il n'y en a plus");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerCourrierId1).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        DemandeDTO[] demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertTrue(demandes.length == 0);

        LOGGER.info(
                "REST call: récupération des demandes de l'usager courrier 2 et vérifier qu'il y en a deux et qu'elles correspondent à celles créées");

        result = mockMvc.perform(
                get(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerCourrierId2).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        demandes = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO[].class);

        assertEquals(2, demandes.length);
        boolean d1Found, d2Found;
        d1Found = d2Found = false;
        for (DemandeDTO demande : demandes) {
            if (demande.getPkDemandes().equals(demandeId1)) {
                d1Found = true;
            } else if (demande.getPkDemandes().equals(demandeId2)) {
                d2Found = true;
            }
        }
        assertTrue(d1Found && d2Found);

    }

    /**
     * 
     * Teste le WS de désinscription d'un usager.
     * 
     * @throws Exception
     */
    @Test
    public void testDesinscriptionUsager() throws Exception {

        LOGGER.info("Access object creation...");

        ObjectMapper mapper = new ObjectMapper();
        AccessInputDTO accessInput = new AccessInputDTO();
        JsonNode contenu = mapper.readTree("{ \"d1\":\"d2\" }");
        accessInput.setContenu(contenu);

        LOGGER.info("REST call: création d'un accès");

        // Appel mocké au WS Rest
        MvcResult result = mockMvc
                .perform(post(adresseWS + "/accesses/" + demarcheId + "/" + usagerId19).with(httpJwtFrontUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(accessInput)))
                .andExpect(status().isCreated()).andReturn();

        AccessDTO access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getUsagerId().equals(usagerId19));

        LOGGER.info("REST call: récupération de l'accès...");

        result = mockMvc.perform(get(adresseWS + "/accesses/" + demarcheId + "/" + usagerId19).with(httpJwtFrontUser()))
                .andExpect(status().isOk()).andReturn();

        access = mapper.readValue(result.getResponse().getContentAsString(), AccessDTO.class);

        assertTrue(access.getUsagerId().equals(usagerId19));

        LOGGER.info("REST call: création d'une demande relative à cet accès (EN_ATTENTE_TRAIT)");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        DemandeInputDTO demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId19 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande1 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId1 = demande1.getPkDemandes();

        assertTrue(demande1.getCanal().equals(canal));
        assertEquals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name(), demande1.getDernierStatut().getLibelle());

        LOGGER.info("REST call: création d'une 2ème demande relative à cet accès (EN_COURS_TRAIT)");

        contenu = mapper.readTree("{ \"d5\":\"d6\" }");
        demandeInput = new DemandeInputDTO();
        demandeInput.setContenu(contenu);
        demandeInput.setLangue(langueFR);
        demandeInput.setCanal(canal);

        result = mockMvc
                .perform(post(adresseWS + "/demandes/" + demarcheId + "?usagerId=" + usagerId19 + "&premierStatut="
                        + TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name()).with(httpJwtFrontUser())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(demandeInput)))
                .andExpect(status().isCreated()).andReturn();

        DemandeDTO demande2 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        Integer demandeId2 = demande2.getPkDemandes();

        assertTrue(demande2.getCanal().equals(canal));
        assertEquals(TestDemandeStatutEnum.EN_ATTENTE_TRAIT.name(), demande2.getDernierStatut().getLibelle());

        LOGGER.info("REST call: passage de la demande au statut VALIDEE");

        StatutInputDTO statutInput = new StatutInputDTO();
        statutInput.setStatut(TestDemandeStatutEnum.VALIDEE.name());
        statutInput.setAgentId(agentId1);

        mockMvc.perform(
                post(adresseWS + "/demandes/" + demarcheId + "/" + demandeId2 + "/statuts").with(httpJwtBackUser())
                        .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(statutInput)))
                .andExpect(status().isCreated());

        LOGGER.info("REST call: appel au WS de désinscription de l'usager...");

        String codeMotif = "UN_CODE_MOTIF";

        mockMvc.perform(delete(adresseWS + "/usagers/" + demarcheId + "/" + usagerId19 + "?statutsFinaux=" + TestDemandeStatutEnum.VALIDEE.name() + "&statutsFinaux="
                + TestDemandeStatutEnum.REFUSEE.name() + "&statutAnnulation=" + TestDemandeStatutEnum.ANNULEE.name()
                + "&codeMotif=" + codeMotif).with(httpJwtFrontUser())).andExpect(status().isOk());

        LOGGER.info("REST call: récupération de l'accès : erreur attendue");

        mockMvc.perform(get(adresseWS + "/accesses/" + demarcheId + "/" + usagerId19).with(httpJwtFrontUser()))
                .andExpect(status().isNotFound());

        LOGGER.info("REST call: récupération de la demande 1 : doit avoir été annulée");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId1).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        demande1 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertEquals(TestDemandeStatutEnum.ANNULEE.name(), demande1.getDernierStatut().getLibelle());

        LOGGER.info("REST call: récupération de la demande 2 : doit avoir été inchangée car en statut final");

        result = mockMvc.perform(get(adresseWS + "/demandes/" + demarcheId + "/" + demandeId2).with(httpJwtBackUser()))
                .andExpect(status().isOk()).andReturn();

        demande2 = mapper.readValue(result.getResponse().getContentAsString(), DemandeDTO.class);

        assertEquals(TestDemandeStatutEnum.VALIDEE.name(), demande2.getDernierStatut().getLibelle());

    }

}
