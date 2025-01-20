package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Map;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.itg.sms.SmsInfoDTO;
import mc.gouv.xaf.back.service.itg.sms.SmsService;
import mc.gouv.xaf.back.service.itg.sms.impl.AfSmsTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

/**
 * Classe service appelée par le process Activiti pour envoyer un SMS à l'usager.
 *
 * @author qdeme
 */
@Component
public class GouvBPMEnvoiSmsUsagerDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMEnvoiSmsUsagerDelegate.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private UsagersCache usagerCache;

    @Autowired
    private SmsService smsService;
    
    @Autowired
    private AfSmsTemplateModelProvider afMailTemplateModelProvider;
    
    @Autowired
    DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private DemandesService demandesService;

    @Setter
    @Getter
    private Expression smsBodyTemplateCode;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back ENVOI SMS USAGER ...");
        Integer usagerId = (Integer) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());
        GichuniUsagerDTO usager = usagerCache.get(usagerId, true);
        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
        DemandeDTO demande = demandesService.getDemande(demandeId);
        if (usager == null) {
            usager = new GichuniUsagerDTO();
            DemandeUsagerDTO usagerDto = demande.getUsager();
            if (usagerDto != null) {
                usager.setNom(usagerDto.getNom());
                usager.setPrenom(usagerDto.getPrenom());
                usager.setEmail(usagerDto.getEmail());
            }
        }
        
        String telephone = demarchesDataProvider.getUsagerTelephone(usager);

        String bodyTemplateCode = (String) smsBodyTemplateCode.getValue(execution);

        String langue = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name());
        SmsInfoDTO smsInfo = new SmsInfoDTO();
        smsInfo.setBodyTemplateCode(bodyTemplateCode);

        smsInfo.addTo(telephone);
        smsInfo.addParam(AfBackUtils.SMS_METADATA_DEMANDEID, execution.getProcessInstanceBusinessKey());
        smsInfo.setLangue(langue);

        String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        String commentaire = (String) execution.getVariable(
                GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        //commentaire = mailService.formatCommentaire(commentaire);
        Map<String, Object> model = afMailTemplateModelProvider.getModel(null, bodyTemplateCode, demande,
                execution.getVariables(), codeMotif, commentaire);

        try {
            smsService.sendSms(smsInfo, model);
        } catch (Exception e) {
            LOGGER.error("Échec lors de l'envoi du SMS", e);
        }

        LOGGER.info("==== xaf-back ENVOI SMS USAGER <fin>");
    }

}
