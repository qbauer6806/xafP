package mc.gouv.xaf.back.paiement.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.back.bpm.model.GouvBPMGroup;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;

@Component
public class GouvBPMTestImpl implements GouvBPM {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Override
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId, String codeAppli, Map<String, Object> businessVariables) {

    }

    @Override
    public Map<String, Object> getProcessBusinessVariables(Integer demandeId) {
        return new HashMap<>();
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
        Optional<DemandeBO> demandeOp = demandesRepository.findById(demandeId);
        if (demandeOp.isPresent()) {
            DemandeBO demande = demandeOp.get();
            DemandesStatutsBO dernierStatut = new DemandesStatutsBO();
            dernierStatut.setLibelle(DemandeStatutEnum.EN_ATTENTE_TRAIT.name());
            dernierStatut.setDate(new Date());
            demandesStatutsRepository.save(dernierStatut);
            demande.setDernierStatut(dernierStatut);
            demandesRepository.save(demande);
        }
    }

    @Override
    public List<GouvBPMTask> getTasksAssignedToUser(GouvBPMUser user) {
        return null;
    }

    @Override
    public List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId) {
        List<GouvBPMTask> tasks = new ArrayList<>();
        tasks.add(new GouvBPMTask());
        return tasks;
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

	@Override
	public void demanderRectification(Integer demandeId, GouvBPMUser agent, String codeMotif, String commentaire,
			String statutAnnulation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reponseRectification(Integer pkDemande, Integer usagerId)
			throws TaskAlreadyClaimedException, IOException, SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rectificationSpontanee(Integer demandeId) {
		// TODO Auto-generated method stub
		
	}
}
