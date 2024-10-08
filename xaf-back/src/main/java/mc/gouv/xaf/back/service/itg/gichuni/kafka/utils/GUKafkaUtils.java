package mc.gouv.xaf.back.service.itg.gichuni.kafka.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.properties.DemPropertyNotFoundException;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeRecapProjection;
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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GUKafkaUtils.class);
	
	public static final String XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT = "XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT";
	public static final String GU_TO_TS_TOPIC = "ts-to-gichuni";
	
	@Autowired
	private PropertiesService propertiesService;

	@Autowired
	private DemarchesDataProvider demarchesDataProvider;
	
	@Autowired
	private DemandesRepository demandesRepository;
	
	@Autowired
	private AccessService accessService;
	
	private Integer dltConsumerJobTimeout = null;
	
    @PostConstruct
    private void initProperties() throws DemPropertyNotFoundException {
        PropertiesDTO dltConsumerJobTimeoutProp = propertiesService.getProperty(XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT);
        if (dltConsumerJobTimeoutProp == null || StringUtils.isBlank(dltConsumerJobTimeoutProp.getValue())) {
        	throw new DemPropertyNotFoundException(XAF_GU_KAFKA_DLT_CONSUMER_JOB_TIMEOUT);
        }
        dltConsumerJobTimeout = Integer.parseInt(dltConsumerJobTimeoutProp.getValue());
    }
	
	public boolean isMessageVersionSupported(String version) {
		return Arrays.asList(demarchesDataProvider.getGUKafkaSupportedVersions()).contains(version);
	}
	
	public Integer getDltConsumerJobTimeout() {
		return dltConsumerJobTimeout;
	}
	
	public RecapDemandesDTO getRecapDemandes(List<DemandeRecapDTO> demandeRecaps) {
		RecapDemandesDTO recap = new RecapDemandesDTO();
		Integer enCours = 0;
		Integer enAttenteUsager = 0;
		Integer terminees = 0;
		
		for (DemandeRecapDTO demande : demandeRecaps) {
			if (StatutSimplifieEnum.EN_COURS.name().equals(demande.getStatutSimplifie())) {
				enCours++;
			}
			else if (StatutSimplifieEnum.EN_ATTENTE_USAGER.name().equals(demande.getStatutSimplifie())) {
				enAttenteUsager++;
			}
			else if (StatutSimplifieEnum.TERMINEE.name().equals(demande.getStatutSimplifie())) {
				terminees++;
			}
		}
		recap.setTotal(enCours+enAttenteUsager+terminees);
		recap.setEnCours(enCours);
		recap.setEnAttenteUsager(enAttenteUsager);
		recap.setTerminees(terminees);
		return recap;
	}
	
	public List<DemandeRecapDTO> getDemandeRecapsFromUsagerId(Integer usagerId) {
		LOGGER.info("Constitution de la liste de DemandeRecapDTO...");
		List<DemandeRecapDTO> demandeRecaps = new ArrayList<>();
		List<DemandeRecapProjection> recapsProj = demandesRepository.findByUsagerIdForDemandeRecapDTO(usagerId);
		for (DemandeRecapProjection r : recapsProj) {
			DemandeRecapDTO recap = new DemandeRecapDTO();
			recap.setDemandeId(r.getPkDemandes());
			recap.setIdentifiant(r.getIdentifiant());
			recap.setDateCreation(r.getDateCreation());
			StatutSimplifieEnum statutSimplifie = demarchesDataProvider.getStatutSimplifieFromStatutPublic(r.getDernierStatut());
			recap.setStatutSimplifie(statutSimplifie.name());
			demandeRecaps.add(recap);
		}
		return demandeRecaps;
	}
	
	public UsagerDemandesRecapDTO getUsagerDemandesRecap(Integer usagerId) {
		LOGGER.info("==================== getUsagerDemandesRecap({})", usagerId);
		UsagerDemandesRecapDTO usagerDemandesRecap = new UsagerDemandesRecapDTO();
		usagerDemandesRecap.setUsagerId(usagerId.toString());

		usagerDemandesRecap.setDemandeRecaps(getDemandeRecapsFromUsagerId(usagerId));
		
		LOGGER.info("Constitution du RecapDemandesDTO...");
		RecapDemandesDTO recapDemandes = getRecapDemandes(usagerDemandesRecap.getDemandeRecaps());
		usagerDemandesRecap.setRecapDemandes(recapDemandes);
		
		LOGGER.info("==================== Fin getUsagerDemandesRecap({})", usagerId);
		return usagerDemandesRecap;
	}
	
	public List<UsagerDemandesRecapDTO> getUsagerDemandesRecapList() {
		List<UsagerDemandesRecapDTO> ret = new ArrayList<>();
		List<Integer> usagerIds = accessService.getUsagersIds();
		for (Integer usagerId : usagerIds) {
			if (!DemarchesUtils.isUsagerCourrier(usagerId)) {
				ret.add(getUsagerDemandesRecap(usagerId));
			}
		}
		return ret;
	}

}
