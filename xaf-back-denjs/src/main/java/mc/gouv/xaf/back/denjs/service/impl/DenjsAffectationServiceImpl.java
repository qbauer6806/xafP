package mc.gouv.xaf.back.denjs.service.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.denjs.dto.DenjsAffectationAgentDTO;
import mc.gouv.xaf.back.denjs.dto.DenjsEtablissementDTO;
import mc.gouv.xaf.back.denjs.service.DenjsAffectationService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.PropertiesTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Service permettant de gérer l'affectation des agents ou des demandes à des établissements, dans le cadre des
 * téléservices pour la DENJS
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class DenjsAffectationServiceImpl implements DenjsAffectationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DenjsAffectationServiceImpl.class);

    private static final String LISTE_AFFECTATIONS_KEY = "LISTE_AFFECTATIONS";

    private static final String LISTE_ETABLISSEMENTS_KEY = "LISTE_ETABLISSEMENTS";

    private static final String DEMANDE_AFFECTATION_ETABLISSEMENT_KEY = "AFFECTATION_ETABLISSEMENT";

    private final PropertiesService propertiesService;

    private final DemandesDataService demandesDataService;

    @Override
    public DenjsAffectationAgentDTO getAffectationAgent(String matricule) {
        List<DenjsAffectationAgentDTO> liste = getAffectationsAgents();
        for (DenjsAffectationAgentDTO aff : liste) {
            if (matricule != null && aff.getAgentMatricule() != null && matricule.equals(aff.getAgentMatricule())) {
                return aff;
            }
        }
        return null;
    }

    @Override
    public List<DenjsAffectationAgentDTO> getAffectationsAgents() {
        LOGGER.info("DenjsAffectationServiceImpl.getAffectationsAgents()");
        PropertiesDTO affectations = propertiesService.getProperty(LISTE_AFFECTATIONS_KEY);
        if (affectations == null) {
            LOGGER.warn("ATTENTION : aucune liste d'affectations en base !");
            return new ArrayList<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            DenjsAffectationAgentDTO[] affsArray = mapper.readValue(affectations.getValue(),
                    DenjsAffectationAgentDTO[].class);
            return new ArrayList<>(Arrays.asList(affsArray));
        } catch (JacksonException e) {
            LOGGER.error("Erreur pendant mapper.readValue() des affectations en base", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<DenjsEtablissementDTO> getEtablissements() {
        LOGGER.info("DenjsAffectationServiceImpl.getEtablissements()");
        PropertiesDTO etabs = propertiesService.getProperty(LISTE_ETABLISSEMENTS_KEY);
        if (etabs == null) {
            LOGGER.warn("ATTENTION : aucune liste d'établissements en base !");
            return new ArrayList<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            DenjsEtablissementDTO[] etabsArray = mapper.readValue(etabs.getValue(), DenjsEtablissementDTO[].class);
            return Arrays.asList(etabsArray);
        } catch (JacksonException e) {
            LOGGER.error("Erreur pendant mapper.readValue() des établissements en base", e);
        }
        return new ArrayList<>();
    }

    private void deleteAffectation(List<DenjsAffectationAgentDTO> affs, DenjsAffectationAgentDTO affectation) {
        DenjsAffectationAgentDTO toDelete = null;
        for (DenjsAffectationAgentDTO aff : affs) {
            if (StringUtils.equals(aff.getAgentMatricule(), affectation.getAgentMatricule())) {
                toDelete = aff;
            }
        }
        if (toDelete != null) {
            affs.remove(toDelete);
        }
    }

    private void ajoutModificationAffectation(List<DenjsAffectationAgentDTO> affs,
            DenjsAffectationAgentDTO affectation) {
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

    @Override
    public List<DenjsAffectationAgentDTO> affecterAgentEtablissement(DenjsAffectationAgentDTO affectation) {
        LOGGER.info("DenjsAffectationServiceImpl.affecterAgent()");

        List<DenjsAffectationAgentDTO> affs = getAffectationsAgents();

        // Suppression
        if (StringUtils.isBlank(affectation.getEtablissementCode())) {
            deleteAffectation(affs, affectation);
        }
        // Ajout/modification
        else {
            ajoutModificationAffectation(affs, affectation);
        }

        PropertiesDTO affectations = propertiesService.getProperty(LISTE_AFFECTATIONS_KEY);
        if (affectations == null) {
            affectations = new PropertiesDTO();
            affectations.setKey(LISTE_AFFECTATIONS_KEY);
            affectations.setType(PropertiesTypeEnum.BACK);
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(affs);
        } catch (JacksonException e) {
            LOGGER.error("Erreur pendant le mapper.writeValueAsString() des affectations", e);
        }
        affectations.setValue(json);
        propertiesService.saveOrUpdateProperties(affectations);

        return affs;
    }

    @Override
    public void affecterDemandeEtablissement(Integer pkDemande, String etablissementCode) {
        LOGGER.info("DenjsAffectationServiceImpl.affecterDemandeEtablissement({}, {})", pkDemande, etablissementCode);
        try {
            demandesDataService.saveOrUpdateDemandeData(pkDemande, DEMANDE_AFFECTATION_ETABLISSEMENT_KEY,
                    etablissementCode);
        } catch (Exception e) {
            LOGGER.error("Erreur pendant la sauvegarde en base de l'affectation d'une demande à un établissement", e);
        }
    }

    @Override
    public String getAffectationDemandeEtablissement(Integer pkDemande) {
        LOGGER.info("DenjsAffectationServiceImpl.getAffectationDemandeEtablissement({})", pkDemande);
        DemandeDataDTO demandeData = demandesDataService.getDemandeData(pkDemande,
                DEMANDE_AFFECTATION_ETABLISSEMENT_KEY);
        if (demandeData == null) {
            return null;
        }
        return demandeData.getValue();
    }

    @Override
    public DenjsEtablissementDTO getEtablissementFromCode(String code, List<DenjsEtablissementDTO> etabs) {
        for (DenjsEtablissementDTO etab : etabs) {
            if (etab.getCode().equals(code)) {
                return etab;
            }
        }
        return null;
    }

    @Override
    public void desaffecterDemandeEtablissement(Integer pkDemande) {
        LOGGER.info("DenjsAffectationServiceImpl.desaffecterDemandeEtablissement({})", pkDemande);
        try {
            demandesDataService.deleteDemandeData(pkDemande, DEMANDE_AFFECTATION_ETABLISSEMENT_KEY);
        } catch (Exception e) {
            LOGGER.error("Erreur pendant la suppression en base de l'affectation d'une demande à un établissement", e);
        }
    }
}
