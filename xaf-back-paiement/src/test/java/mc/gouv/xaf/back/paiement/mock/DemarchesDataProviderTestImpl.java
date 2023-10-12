package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.GenericStatusDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DemarchesDataProviderTestImpl implements DemarchesDataProvider {
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
        return new String[0];
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEligibleRectification(DemandeDTO demande) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public List<String> getStatutsPourDuplication() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getBuildIdsPourDuplication() {
        // TODO Auto-generated method stub
        return null;
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

	@Override
	public String getStatutAnnulee() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCodeMotifAnnulationParUsager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCodeMotifAnnulationDesinscription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPremierStatutCreationDemande() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatutEnAttenteRectification() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkAssociationCourrier(DemandeDTO demande, String stringToCheck) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getStatutsFinauxPourAnnulationCauseDesinscription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMailBodyTemplateCodeDesinscriptionUsagerPourAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMailSubjectTemplateCodeDesinscriptionUsagerPourAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMailBodyTemplateCodeDesinscriptionUsagerPourUsager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMailSubjectTemplateCodeDesinscriptionUsagerPourUsager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAllStatuts() {
		// TODO Auto-generated method stub
		return null;
	}
}
