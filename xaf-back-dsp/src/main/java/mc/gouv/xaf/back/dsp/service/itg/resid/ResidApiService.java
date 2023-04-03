package mc.gouv.xaf.back.dsp.service.itg.resid;

import mc.gouv.xaf.back.dsp.dto.*;
import mc.gouv.xaf.back.dsp.dto.v2.ResidUsagerDLN1FDTO;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.List;
import java.util.Map;

public interface ResidApiService {

    ResidHttpResponseDTO submitNouvelleCarteResid(ResidDemandeNouvelleCarteCompleteDTO nouvelleCarte, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitRenouvellementCarteResid(ResidDemandeRenouvellementCarteCompleteDTO carteRenouvellement, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitDuplicataCarteResid(ResidDemandeDuplicataCarteCompleteDTO duplicataCarte, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitChangementSituationResid(ResidDemandeChangementSituationCompleteDTO changementsituation, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitCertificatResid(ResidDemandeCertificatResidenceCompleteDTO certificatResidence, Map<Integer, DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidStatutDemandeDTO getEtatDemande(ResidIdTSDTO idDemande, String url, String jwt) throws Exception;

    List<ResidStatutDemandeDTO> getEtatMultipleDemandes(List<ResidIdTSDTO> idsDemandes, String url, String jwt) throws Exception;

    ResidEtatsDemandesUpdatedAfterDTO getEtatsDemandesUpdated(String updatedAfter, String url, String jwt) throws ResidHttpResponseException;

    List<ResidResidentCorrespondanceDTO> getListResidCorrespondance(String numeroCarte, String url, String jwt);
    
    ResidUsagerDLN1FDTO getUsagerDln1f(String nom, String prenom, String dateNaissance, String heureNaissance, String villeNaissance, String paysNaissance, String url, String jwt);
}
