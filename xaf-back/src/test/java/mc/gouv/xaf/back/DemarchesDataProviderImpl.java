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
        return null;
    }

    @Override
    public String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne) {
        return null;
    }

    @Override
    public String getDemandeur(Object contenuDemandeDTO) {
        return null;
    }

    @Override
    public List<GenericStatusDTO> getCandidateStatusesForMotifs() {
        return null;
    }

    @Override
    public StatutPublicOuInterneDTO getStatutPublicOuInterne(DemandeDTO demandeDto) {
        return null;
    }

    @Override
    public Map<String, String> getStatusMap() {
        return null;
    }

    @Override
    public Map<String, String> getPrivateStatusMap() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public StatutPublicOuInterneDTO getStatutPublicOuInterne(Integer pkDemande, String statutLibelle) {
        return null;
    }

	@Override
	public Map<String, String> getLanguesDisponibles() {
		return null;
	}

    @Override
    public boolean getDemarcheCanGenerateCourriers() {
        return false;
    }

	@Override
	public boolean getDemarcheCanHandlePeriodesOuverture() {
		return false;
	}

    @Override
    public boolean getDemarcheCanHandleProperties() {
        return false;
    }

	@Override
	public boolean getDemarcheCanHandleDenjsGestionAgents() {
		return false;
	}

    @Override
    public boolean getDemarcheCanHandleTaches() {
        return false;
    }

    @Override
	public String[] getGUKafkaSupportedVersions() {
		return null;
	}

	@Override
	public StatutSimplifieEnum getStatutSimplifieFromStatutPublic(String statutPublic) {
		return null;
	}

    @Override
    public List<String> getStatutsAPurger() {
        return null;
    }

	@Override
	public boolean isValideTypedoc(String typedoc) {
		return false;
	}

	@Override
	public DemandeExcelGenerationDTO getDemandeExcelGenerationDTO() {
		return null;
	}

	@Override
	public boolean isEligibleRectification(DemandeDTO demande) {
		return false;
	}

    @Override
    public String getExportLibelle() {
        return null;
    }

    @Override
    public String getBrouillonStatutNotTransmitted() {
        return null;
    }

    @Override
    public String getBrouillonStatutDeprecated() {
        return null;
    }
}
