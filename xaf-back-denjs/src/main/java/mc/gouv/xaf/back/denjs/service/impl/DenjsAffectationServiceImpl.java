package mc.gouv.xaf.back.denjs.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.denjs.dto.DenjsAffectationAgentDTO;
import mc.gouv.xaf.back.denjs.dto.DenjsEtablissementDTO;
import mc.gouv.xaf.back.denjs.service.DenjsAffectationService;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesTypeEnum;

/**
 * Service permettant de gérer l'affectation des agents ou des demandes à des établissements, dans le cadre
 * des téléservices pour la DENJS
 * 
 * @author qdeme
 *
 */
@Component
public class DenjsAffectationServiceImpl implements DenjsAffectationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DenjsAffectationServiceImpl.class);
	
	private static final String LISTE_AFFECTATIONS_KEY = "LISTE_AFFECTATIONS";
	
	private static final String LISTE_ETABLISSEMENTS_KEY = "LISTE_ETABLISSEMENTS";
	
	private static final String DEMANDE_AFFECTATION_ETABLISSEMENT_KEY = "AFFECTATION_ETABLISSEMENT";

	@Autowired
	private PropertiesService propertiesService;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Autowired
	private DemandesDataService demandesDataService;
	
	@Override
	public List<DenjsAffectationAgentDTO> getAffectationsAgents() {
		LOGGER.info("DenjsAffectationServiceImpl.getAffectationsAgents()");
		
		PropertiesDTO affectations = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), LISTE_AFFECTATIONS_KEY);
		if (affectations == null) {
			LOGGER.warn("ATTENTION : aucune liste d'affectations en base !");
			return new ArrayList<DenjsAffectationAgentDTO>();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			DenjsAffectationAgentDTO[] affsArray = mapper.readValue(affectations.getValue(), DenjsAffectationAgentDTO[].class);
			List<DenjsAffectationAgentDTO> affsList = new ArrayList<DenjsAffectationAgentDTO>(Arrays.asList(affsArray));
			return affsList;
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur pendant mapper.readValue() des affectations en base", e);
		}
		return null;
	}

	@Override
	public List<DenjsEtablissementDTO> getEtablissements() {
		LOGGER.info("DenjsAffectationServiceImpl.getEtablissements()");
		
		PropertiesDTO etabs = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), LISTE_ETABLISSEMENTS_KEY);
		if (etabs == null) {
			LOGGER.warn("ATTENTION : aucune liste d'établissements en base !");
			return new ArrayList<DenjsEtablissementDTO>();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			DenjsEtablissementDTO[] etabsArray = mapper.readValue(etabs.getValue(), DenjsEtablissementDTO[].class);
			List<DenjsEtablissementDTO> etabsList = new ArrayList<DenjsEtablissementDTO>(Arrays.asList(etabsArray));
			return etabsList;
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur pendant mapper.readValue() des établissements en base", e);
		}
		return null;
	}

	@Override
	public List<DenjsAffectationAgentDTO> affecterAgentEtablissement(DenjsAffectationAgentDTO affectation) {
		LOGGER.info("DenjsAffectationServiceImpl.affecterAgent()");
		
		List<DenjsAffectationAgentDTO> affs = getAffectationsAgents();
		
		// Suppression
		if (StringUtils.isBlank(affectation.getEtablissementCode())) {
			DenjsAffectationAgentDTO toDelete = null;
			for (DenjsAffectationAgentDTO aff : affs) {
				if (aff.getAgentMatricule().equals(affectation.getAgentMatricule())) {
					toDelete = aff;
				}
			}
			if (toDelete != null) {
				affs.remove(toDelete);
			}
		}
		// Ajout/modification
		else {
			boolean found = false;
			for (DenjsAffectationAgentDTO aff : affs) {
				if (aff.getAgentMatricule().equals(affectation.getAgentMatricule())) {
					found = true;
					// Modification
					aff.setEtablissementCode(affectation.getEtablissementCode());
				}
			}
			// Ajout
			if (!found) {
				affs.add(affectation);
			}
		}
		
		PropertiesDTO affectations = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), LISTE_AFFECTATIONS_KEY);
		if (affectations == null) {
			affectations = new PropertiesDTO();
			affectations.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
			affectations.setKey("LISTE_AFFECTATIONS");
			affectations.setType(PropertiesTypeEnum.BACK);
		}
		
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
			json = mapper.writeValueAsString(affs);
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur pendant le mapper.writeValueAsString() des affectations", e);
		}
		affectations.setValue(json);
		propertiesService.saveOrUpdateProperties(affectations);
		
		return affs;
	}

	@Override
	public void affecterDemandeEtablissement(Integer pkDemande, String etablissementCode) {
		LOGGER.info("DenjsAffectationServiceImpl.affecterDemandeEtablissement(" + pkDemande + "," + etablissementCode + ")");
		try {
			demandesDataService.saveOrUpdateDemandeData(gouvPropertiesResolver.getDemarcheId(), pkDemande, DEMANDE_AFFECTATION_ETABLISSEMENT_KEY , etablissementCode);
		} catch (Exception e) {
			LOGGER.error("Erreur pendant la sauvegarde en base de l'affectation d'une demande à un établissement", e);
		}
	}

	@Override
	public String getAffectationDemandeEtablissement(Integer pkDemande) {
		LOGGER.info("DenjsAffectationServiceImpl.getAffectationDemandeEtablissement(" + pkDemande + ")");
		DemandeDataDTO demandeData = demandesDataService.getDemandeData(gouvPropertiesResolver.getDemarcheId(), pkDemande, DEMANDE_AFFECTATION_ETABLISSEMENT_KEY);
		if (demandeData == null) {
			return null;
		}
		return demandeData.getValue();
	}
	
	@Override
    public String getEtablissementNomFromCode(String code, List<DenjsEtablissementDTO> etabs) {
    	for (DenjsEtablissementDTO etab : etabs) {
    		if (etab.getCode().equals(code)) {
    			return etab.getNom();
    		}
    	}
    	return null;
    }

}
