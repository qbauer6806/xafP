package mc.gouv.xaf.back.paiement.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class GouvBPMTestImpl implements GouvBPM {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Override
    public void startProcessInstance(String processDefinitionKey, GouvBPMUser user, Integer demandeId,
            Map<String, Object> businessVariables) {
        
    }

    @Override
    public Map<String, Object> getProcessBusinessVariables(Integer demandeId) {
        return Map.of();
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
    public List<GouvBPMTask> getActiveTasksForDemande(Integer demandeId) {
        List<GouvBPMTask> tasks = new ArrayList<>();
        tasks.add(new GouvBPMTask());
        return tasks;
    }

    @Override
    public boolean isProcessInstanceAlive(Integer demandeId) {
        return false;
    }

    @Override
    public void submitTaskFormData(GouvBPMTask task, Map<String, String> properties, Integer demandeId)
            throws IOException, TikaException, SAXException {

    }

    @Override
    public List<GouvBPMStatutAction> getTaskStatutActions(GouvBPMTask task) {
        return List.of();
    }

    @Override
    public void removeProcessBusinessVariables(Integer demandeId, String businessVariable) {

    }

    @Override
    public void annulerDemande(Integer demandeId, GouvBPMUser agent, GouvBPMUser usager, String codeMotif,
            String commentaire, String statutAnnulation) {

    }

    @Override
    public void startProcessInstanceByMessage(String messageName, GouvBPMUser user, Integer demandeId,
            Map<String, Object> businessVariables) {

    }

    @Override
    public void setAssignee(Integer demandeId, String assignee) {

    }

    @Override
    public void reponseRectification(Integer pkDemande, Integer usagerId) {

    }

    @Override
    public void rectificationSpontanee(Integer demandeId) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEngineVersion(Integer pkDemandes) {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminerProcess(Integer pkDemandes, String message) {

    }

}
