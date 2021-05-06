package mc.gouv.xaf.back.service.itg.gichuni.kafka.utils;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.properties.DemPropertyNotFoundException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.StatutSimplifieEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * 
 * Classe utilitaire pour les messages du Guichet Unique sur Kafka
 *
 * @author qdeme
 *
 */
@Component
public class GUKafkaUtils {
	
	public static final String XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT = "XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT";
	public static final String GU_TO_TS_TOPIC = "ts-to-gichuni";
	
	@Autowired
	private PropertiesService propertiesService;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Autowired
	private DemarchesDataProvider demarchesDataProvider;
	
	private Integer dltConsumerJobTimeout = null;
	
    @PostConstruct
    private void initProperties() throws DemPropertyNotFoundException {
        PropertiesDTO dltConsumerJobTimeoutProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT);
        if (dltConsumerJobTimeoutProp == null || StringUtils.isBlank(dltConsumerJobTimeoutProp.getValue())) {
        	throw new DemPropertyNotFoundException(XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT);
        }
        dltConsumerJobTimeout = Integer.parseInt(dltConsumerJobTimeoutProp.getValue());
    }
	
	public boolean isMessageVersionSupported(String version) {
		return Arrays.stream(demarchesDataProvider.getGUKafkaSupportedVersions()).anyMatch(version::equals);
	}
	
	public Integer getDltConsumerJobTimeout() {
		return dltConsumerJobTimeout;
	}
	
	public RecapDemandesDTO getRecapDemandes(List<DemandeDTO> demandes) {
		RecapDemandesDTO recap = new RecapDemandesDTO();
		Integer enCours = 0;
		Integer enAttenteUsager = 0;
		Integer terminees = 0;
		for (DemandeDTO demande : demandes) {
			StatutSimplifieEnum statutSimplifie = demarchesDataProvider.getStatutSimplifieFromStatutPublic(demande.getDernierStatut().getLibelle());
			if (StatutSimplifieEnum.EN_COURS.equals(statutSimplifie)) {
				enCours++;
			}
			else if (StatutSimplifieEnum.EN_ATTENTE_USAGER.equals(statutSimplifie)) {
				enAttenteUsager++;
			}
			else if (StatutSimplifieEnum.TERMINEE.equals(statutSimplifie)) {
				terminees++;
			}
		}
		recap.setTotal(enCours+enAttenteUsager+terminees);
		recap.setEnCours(enCours);
		recap.setEnAttenteUsager(enAttenteUsager);
		recap.setTerminees(terminees);
		return recap;
	}

}
