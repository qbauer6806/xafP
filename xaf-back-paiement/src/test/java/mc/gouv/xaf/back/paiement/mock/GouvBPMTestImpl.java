package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.back.bpm.model.GouvBPMGroup;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class GouvBPMTestImpl implements GouvBPM {
    @Override
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId, String codeAppli, Map<String, Object> businessVariables) {

    }

    @Override
    public Map<String, Object> getProcessBusinessVariables(Integer demandeId) {
        return null;
    }

    @Override
    public void setProcessBusinessVariables(Integer demandeId, Map<String, Object> businessVariables) {

    }

    @Override
    public void setProcessBusinessVariable(Integer demandeId, String key, Object value) {

    }

    @Override
    public void claimTask(GouvBPMTask task, GouvBPMUser user) throws TaskAlreadyClaimedException {

    }

    @Override
    public void completeTask(GouvBPMTask task, Integer demandeId) throws IOException, TikaException, SAXException {

    }

    @Override
    public List<GouvBPMTask> getTasksAssignedToUser(GouvBPMUser user) {
        return null;
    }

    @Override
    public List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId) {
        return null;
    }

    @Override
    public List<String> getNumberActiveDemandesInState(String name) {
        return null;
    }

    @Override
    public List<GouvBPMTask> getTasksWhereUserIsCandidate(GouvBPMUser user, String codeAppli) {
        return null;
    }

    @Override
    public List<GouvBPMTask> getTasksForDemandeWhereUserIsCandidate(GouvBPMUser user, String codeAppli, Integer demandeId) {
        return null;
    }

    @Override
    public List<GouvBPMTask> getTasksWhereGroupIsCandidate(GouvBPMGroup group, String codeAppli) {
        return null;
    }

    @Override
    public boolean isProcessInstanceAlive(Integer demandeId) {
        return false;
    }

    @Override
    public void jump(Integer demandeId, GouvBPMTask taskFrom, GouvBPMTask taskTo) {

    }

    @Override
    public List<Integer> getDemandesIdsByCodeAppliAndTacheCourante(String codeAppli, GouvBPMTask task) {
        return null;
    }

    @Override
    public List<Integer> getDemandesIdsByCodeAppliAndTacheCouranteAndCandidateUser(String codeAppli, GouvBPMTask task, GouvBPMUser user) {
        return null;
    }

    @Override
    public List<CommentaireInterneDTO> getCommentairesInternes(Integer demandeId) {
        return null;
    }

    @Override
    public void putCommentaireInterne(Integer demandeId, CommentaireInterneDTO commentaire) {

    }

    @Override
    public void submitTaskFormData(GouvBPMTask task, Map<String, String> properties, Integer demandeId) throws IOException, TikaException, SAXException {

    }

    @Override
    public List<GouvBPMStatutAction> getTaskStatutActions(GouvBPMTask task) {
        return null;
    }

    @Override
    public void removeProcessBusinessVariables(Integer demandeId, String businessVariable) {

    }

    @Override
    public void annulerDemande(Integer demandeId, GouvBPMUser agent, GouvBPMUser usager, String codeMotif, String commentaire, String statutAnnulation) {

    }

    @Override
    public void startProcessInstanceByMessage(String messageName, GouvBPMUser user, Integer demandeId, String codeAppli, Map<String, Object> businessVariables) {

    }

    @Override
    public void setAssignee(Integer demandeId, String assignee) {

    }
}
