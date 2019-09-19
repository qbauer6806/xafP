package mc.gouv.af.back;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import mc.gouv.af.back.dto.GenericStatusDTO;
import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.af.back.service.DemarchesDataProvider;
import mc.gouv.dem.shared.model.DemandeDTO;

@Component
public class DemarchesDataProviderImpl implements DemarchesDataProvider {

    @Override
    public String getStatusLibelle(String status) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDemandeur(Object contenuDemandeDTO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GenericStatusDTO> getCandidateStatusesForMotifs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatutPublicOuInterneDTO getStatutPublicOuInterne(DemandeDTO demandeDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getStatusMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getPrivateStatusMap() {
        return null;
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatutPublicOuInterneDTO getStatutPublicOuInterne(Integer pkDemande, String statutLibelle) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public Map<String, String> getLanguesDisponibles() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public boolean getDemarcheCanGenerateCourriers() {
        // TODO Auto-generated method stub
        return false;
    }
}
