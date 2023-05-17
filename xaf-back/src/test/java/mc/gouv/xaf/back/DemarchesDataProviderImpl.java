package mc.gouv.xaf.back;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.GenericStatusDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;

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

	@Override
	public boolean getDemarcheCanHandlePeriodesOuverture() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean getDemarcheCanHandleProperties() {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public boolean getDemarcheCanHandleDenjsGestionAgents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getGUKafkaSupportedVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatutSimplifieEnum getStatutSimplifieFromStatutPublic(String statutPublic) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<String> getStatutsAPurger() {
        return null;
    }

	@Override
	public boolean isValideTypedoc(String typedoc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DemandeExcelGenerationDTO getDemandeExcelGenerationDTO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEligibleRectification(DemandeDTO demande) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public String getExportLibelle() {
        return null;
    }
}
