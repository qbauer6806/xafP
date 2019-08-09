#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.afimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPM;
import mc.gouv.af.back.bpm.model.GouvBPMTask;
import mc.gouv.af.back.dto.GenericStatusDTO;
import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.af.back.service.DemarchesDataProvider;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}StatutInterneEnum;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;

/**
 * Service implémenté par la démarche permettant de fournir à af-back des informations propres à chaque démarche.
 * 
 * @author qdeme
 *
 */
@Component
public class DemarchesDataProviderImpl implements DemarchesDataProvider {

    @Autowired
    private GouvBPM gouvBPM;

    @Override
    public String getStatusLibelle(String status) {
        ${artifactIdCamelCase}DemandeStatutEnum st = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(status);
        return st.libelle;
    }

    @Override
    public String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne) {
        if (statutPublicOuInterne != null && statutPublicOuInterne.getName() != null) {
            if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name().equals(statutPublicOuInterne.getName())) {
                return "en-attente-traitement";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statutPublicOuInterne.getName())) {
                return "en-attente-infos-compl";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statutPublicOuInterne.getName())) {
                return "en-cours-traitement";
            } else if (${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name()
                    .equals(statutPublicOuInterne.getName())) {
                return "en-attente-validation";
            } else if (${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name().equals(statutPublicOuInterne.getName())) {
                return "en-cours-traitement";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statutPublicOuInterne.getName())) {
                return "refusee";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name().equals(statutPublicOuInterne.getName())) {
                return "validee";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.name()
                    .equals(statutPublicOuInterne.getName())) {
                return "en-cours-traitement";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.name().equals(statutPublicOuInterne.getName())) {
                return "validee";
            } else if (${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(statutPublicOuInterne.getName())) {
                return "annulee";
            }
        }
        return "default-status-color";
    }

    @Override
    public String getDemandeur(Object contenuDemandeDTO) {
        if (contenuDemandeDTO != null) {
            return ((ContenuProjectDemandeDTO) contenuDemandeDTO).getUsager().getPrenom() + " "
                    + ((ContenuProjectDemandeDTO) contenuDemandeDTO).getUsager().getNom();
        }
        return null;
    }

    /**
     * Retourne la liste des statuts pour lesquels des motifs peuvent correspondre
     * 
     * @return
     */
    @Override
    public List<GenericStatusDTO> getCandidateStatusesForMotifs() {
        List<GenericStatusDTO> listEnumtoTrue = new ArrayList<GenericStatusDTO>();
        for (${artifactIdCamelCase}DemandeStatutEnum obj : ${artifactIdCamelCase}DemandeStatutEnum.values()) {
            if (obj.containsMotifs == true) {
                GenericStatusDTO st = new GenericStatusDTO();
                st.setName(obj.name());
                st.setLibelle(obj.libelle);
                listEnumtoTrue.add(st);
            }
        }
        return listEnumtoTrue;
    }

    @Override
    public StatutPublicOuInterneDTO getStatutPublicOuInterne(DemandeDTO demandeDto) {

        return getStatutPublicOuInterne(demandeDto.getPkDemandes(), demandeDto.getDernierStatut().getLibelle());
    }

    @Override
    public StatutPublicOuInterneDTO getStatutPublicOuInterne(Integer pkDemande, String statutLibelle) {
    	if (${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(statutLibelle)) {
            StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO();
            statut.setName(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());
            statut.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.libelle);
            return statut;
    	}
    	
        List<GouvBPMTask> tasks = gouvBPM.getActiveTasksForDemande(pkDemande);
        if (tasks != null && tasks.size() == 1) {
            if (tasks.get(0).getTaskDefinitionKey()
                    .startsWith(${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name())) {
                StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO();
                statut.setName(${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name());
                statut.setLibelle(${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.libelle);
                return statut;
            } else if (tasks.get(0).getTaskDefinitionKey()
                    .startsWith(${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name())) {
                StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO();
                statut.setName(${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name());
                statut.setLibelle(${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.libelle);
                return statut;
            }
        }

        StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO();
        ${artifactIdCamelCase}DemandeStatutEnum statutEnum = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(statutLibelle);
        statut.setName(statutEnum.name());
        statut.setLibelle(statutEnum.libelle);
        return statut;
    }

    @Override
    public Map<String, String> getStatusMap() {
        Map<String, String> statusMap = new LinkedHashMap<String, String>();
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name(),
                ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.libelle);
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name(), ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.libelle);
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name(),
                ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.libelle);
        statusMap.putAll(getPrivateStatusMap());
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.name(),
                ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.libelle);
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.name(),
                ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.libelle);
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name(), ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.libelle);
        statusMap.put(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name(), ${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.libelle);
        return statusMap;
    }

    @Override
    public Map<String, String> getPrivateStatusMap() {
        Map<String, String> statusMap = new LinkedHashMap<String, String>();
        statusMap.put(${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name(),
                ${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.libelle);
        statusMap.put(${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name(),
                ${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.libelle);
        return statusMap;
    }

    @Override
    public String getVersion() {
        return this.getClass().getPackage().getImplementationVersion();
    }
    
    @Override
    public Map<String, String> getLanguesDisponibles() {
        Map<String, String> langues = new HashMap<>();
        langues.put("fr", "Français");
        langues.put("en", "Anglais");
        return langues;
    }

}
