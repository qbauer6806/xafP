package mc.gouv.xaf.back.service.itg.resid;

import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.itg.resid.dto.*;

import java.util.List;

public interface ResidApiService {

    ResidHttpResponseDTO submitNouvelleCarteResid(ResidDemandeNouvelleCarteCompleteDTO nouvelleCarte, List<DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitRenouvellementCarteResid(ResidDemandeRenouvellementCarteCompleteDTO carteRenouvellement, List<DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitDuplicataCarteResid(ResidDemandeDuplicataCarteCompleteDTO duplicataCarte, List<DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitChangementSituationResid(ResidDemandeChangementSituationCompleteDTO changementsituation, List<DemandeFileDTO> files, String url, String jwt) throws Exception;

    ResidHttpResponseDTO submitCertificatResid(ResidDemandeCertificatResidenceCompleteDTO certificatResidence, List<DemandeFileDTO> files, String url, String jwt) throws Exception;
}
