package mc.gouv.xaf.back;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DemarchesDataProviderImpl implements DemarchesDataProvider {

    @Override
    public String getStatusLibelle(String statusName) {
        return "";
    }

    @Override
    public String getDemandeur(DemandeDTO contenuDemandeDTO) {
        return "";
    }

    @Override
    public Map<String, String> getStatusMap() {
        return Map.of();
    }

    @Override
    public Map<String, String> getPrivateStatusMap() {
        return Map.of();
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public boolean getDemarcheCanGenerateCourriers() {
        return false;
    }

    @Override
    public List<String> getStatutsAPurger() {
        return List.of();
    }

    @Override
    public String getStatutAnnulee() {
        return "";
    }

    @Override
    public String getCodeMotifAnnulationParUsager() {
        return "";
    }

    @Override
    public String getCodeMotifAnnulationDesinscription() {
        return "";
    }

    @Override
    public String getPremierStatutCreationDemande() {
        return "";
    }

    @Override
    public boolean checkAssociationCourrier(DemandeDTO demande, String stringToCheck) {
        return false;
    }

    @Override
    public StatutSimplifieEnum getStatutSimplifie(String statut) {
        return null;
    }

    @Override
    public String getMailBodyTemplateCodeDesinscriptionUsagerPourAgents() {
        return "";
    }

    @Override
    public String getMailSubjectTemplateCodeDesinscriptionUsagerPourAgents() {
        return "";
    }

    @Override
    public String getMailBodyTemplateCodeDesinscriptionUsagerPourUsager() {
        return "";
    }

    @Override
    public String getMailSubjectTemplateCodeDesinscriptionUsagerPourUsager() {
        return "";
    }
}
