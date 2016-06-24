package mc.gouv.af.back;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mc.gouv.af.back.bpm.GouvBPM;
import mc.gouv.af.back.bpm.model.GouvBPMTask;
import mc.gouv.af.back.bpm.model.GouvBPMUser;

/**
 * Classe de tests unitaires pour le BPM d'AppFactory
 * ContextConfiguration : TestConfig pour la conf des tests (DB in memory et mocks), ActivitiConfig pour
 * reprendre la conf initiale du projet telle qu'elle est pour tous les clients d'af-back
 * 
 * @author qdeme
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class, ActivitiConfig.class })
public class AfBPMTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBPMTest.class);
    
    @Autowired
    private GouvBPM gouvBPM;
    
    public static final String USER_MATRICULE = "99999";
    
    public static final String DEMANDE_ID = "12";
    
    public static final String HAB_NOMLOC = "Dupont";
    
    public static final String HAB_PRENOMLOC = "Martin";
    
    public static final String HAB_COMMENTAIRE = "Cette demande me semble satisfaisante, on accepte";
    
    public static final String task1DefinitionKey = "traiterDemandeTask";
    
    public static final String task1Name = "Traiter demande";
    
    public static final String task2DefinitionKey = "validerDemandeTask";
    
    public static final String task2Name = "Valider demande";
    
    public static boolean majDemandeServiceExecuted = false;

    /**
     * 
     * Effectue des appels WS pour vérifier le bon fonctionnement des services /accesses
     * et /demandes.
     * 
     * @throws Exception
     */
    @Test
    public void testBPM() throws Exception {

        LOGGER.info("Access object creation...");
        
        GouvBPMUser user = new GouvBPMUser();
        user.setId(USER_MATRICULE);
        
        Map<String, Object> businessVariables = new HashMap<String, Object>();
        businessVariables.put("HAB_NOMLOC", HAB_NOMLOC);
        businessVariables.put("HAB_PRENOMLOC", HAB_PRENOMLOC);
        
        LOGGER.info("==== isProcessInstanceAlive() ?");
        assertTrue(!gouvBPM.isProcessInstanceAlive(DEMANDE_ID));
        
        LOGGER.info("==== Démarrage du process...");
        gouvBPM.startProcessInstance("habitatProcess", user, DEMANDE_ID, businessVariables);
        
        LOGGER.info("==== isProcessInstanceAlive() ?");
        assertTrue(gouvBPM.isProcessInstanceAlive(DEMANDE_ID));
        
        LOGGER.info("==== listActiveTasksForDemande() :");
        List<GouvBPMTask> tasks = gouvBPM.getActiveTasksForDemande(DEMANDE_ID);
        assertTrue(tasks.size() == 1);
        assertTrue(tasks.get(0).getName().equals(task1Name));
        assertTrue(tasks.get(0).getTaskDefinitionKey().equals(task1DefinitionKey));
        assertTrue(tasks.get(0).getAssignee().equals(USER_MATRICULE));
        
        GouvBPMTask task = tasks.get(0);
        
        LOGGER.info("==== listTasksAssignedToUser() :");
        tasks = gouvBPM.getTasksAssignedToUser(user);
        // Car activiti:assignee="${MC_USERID}" dans habitat.bpmn
        assertTrue(tasks.size() == 1);
        assertTrue(tasks.get(0).getName().equals(task1Name));
        assertTrue(tasks.get(0).getTaskDefinitionKey().equals(task1DefinitionKey));
        assertTrue(tasks.get(0).getAssignee().equals(USER_MATRICULE));
        
        LOGGER.info("==== completeTask()...");
        gouvBPM.completeTask(task);

        LOGGER.info("==== listActiveTasksForDemande() :");
        tasks = gouvBPM.getActiveTasksForDemande(DEMANDE_ID);
        assertTrue(tasks.size() == 1);
        assertTrue(tasks.get(0).getName().equals(task2Name));
        assertTrue(tasks.get(0).getTaskDefinitionKey().equals(task2DefinitionKey));
        assertTrue(tasks.get(0).getAssignee() == null);
        task = tasks.get(0);

        LOGGER.info("==== listTasksAssignedToUser() :");
        tasks = gouvBPM.getTasksAssignedToUser(user);
        assertTrue(tasks.size() == 0);
        
        LOGGER.info("==== claimTask()...");
        gouvBPM.claimTask(task, user);
        
        LOGGER.info("==== listTasksAssignedToUser() :");
        tasks = gouvBPM.getTasksAssignedToUser(user);
        assertTrue(tasks.size() == 1);
        assertTrue(tasks.get(0).getName().equals(task2Name));
        assertTrue(tasks.get(0).getTaskDefinitionKey().equals(task2DefinitionKey));
        assertTrue(tasks.get(0).getAssignee().equals(USER_MATRICULE));
        
        LOGGER.info("==== Ajout d'un commentaire...");
        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(DEMANDE_ID);
        variables.put("HAB_COMMENTAIRE", HAB_COMMENTAIRE);
        gouvBPM.setProcessBusinessVariables(DEMANDE_ID, variables);
        
        LOGGER.info("==== getProcessBusinessVariables() :");
        variables = gouvBPM.getProcessBusinessVariables(DEMANDE_ID);
        boolean habNomLocFound, habPrenomLocFound, habCommentaireFound, mcUserIdFound;
        habNomLocFound = habPrenomLocFound = habCommentaireFound = mcUserIdFound = false;
        for (String key : variables.keySet()) {
            LOGGER.info("  (" + key + "," + variables.get(key) + ")");
            if (key.equals("HAB_NOMLOC")) {
                habNomLocFound = true;
                assertTrue(variables.get(key).equals(HAB_NOMLOC));
            }
            else if (key.equals("HAB_PRENOMLOC")) {
                habPrenomLocFound = true;
                assertTrue(variables.get(key).equals(HAB_PRENOMLOC));
            }
            else if (key.equals("HAB_COMMENTAIRE")) {
                habCommentaireFound = true;
                assertTrue(variables.get(key).equals(HAB_COMMENTAIRE));
            }
            else if (key.equals("MC_USERID")) {
                mcUserIdFound = true;
                assertTrue(variables.get(key).equals(USER_MATRICULE));
            }
        }
        assertTrue(habNomLocFound && habPrenomLocFound && habCommentaireFound && mcUserIdFound);
        
        LOGGER.info("==== completeTask()...");
        assertTrue(!majDemandeServiceExecuted);
        gouvBPM.completeTask(task);
        assertTrue(majDemandeServiceExecuted);
        
        LOGGER.info("==== listActiveTasksForDemande() :");
        tasks = gouvBPM.getActiveTasksForDemande(DEMANDE_ID);
        for (GouvBPMTask t : tasks) {
            LOGGER.info("    - task : " + t);
        }
        assertTrue(tasks.size() == 0);
        
        LOGGER.info("==== isProcessInstanceAlive() ?");
        LOGGER.info("    - " + gouvBPM.isProcessInstanceAlive(DEMANDE_ID));
        assertTrue(!gouvBPM.isProcessInstanceAlive(DEMANDE_ID));
        
    }
    
}
