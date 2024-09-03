 package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import lombok.Setter;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.common.engine.api.delegate.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour générer un courrier PDF.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMExpirationCheckDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMExpirationCheckDelegate.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesService demandesService;
    
    @Autowired
    private GouvBPM gouvBPM;
    
    @Setter
    @Getter
    private Expression numberOfDays;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back GouvBPMExpirationCheckDelegate ...");

        DemandeDTO demandeDto = demandesService.getDemande(Integer.parseInt(execution.getProcessInstanceBusinessKey()));
        
        LocalDate lastStatusDate = Instant.ofEpochMilli(demandeDto.getDernierStatut().getDate().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        final long days = ChronoUnit.DAYS.between(lastStatusDate, currentDate);

        int numberOfDaysInt = Integer.parseInt((String)numberOfDays.getValue(execution));
        
        LOGGER.info("pkDemande = {}, days = {}, numberOfDaysInt = {}", demandeDto.getPkDemandes(), days, numberOfDaysInt);
        
        if (days > numberOfDaysInt) {
            LOGGER.info("Demande expirée !");
            gouvBPM.setProcessBusinessVariable(demandeDto.getPkDemandes(), GouvBPMProcessVariableTypeEnum.MC_EXPIRED.name(), true);
        }
        
        LOGGER.info("Demande : {}", demandeDto.getPkDemandes());
        
        LOGGER.info("==== xaf-back GouvBPMExpirationCheckDelegate <fin>");
    }

}
