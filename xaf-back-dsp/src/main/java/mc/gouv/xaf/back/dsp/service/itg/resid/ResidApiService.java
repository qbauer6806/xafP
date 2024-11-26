package mc.gouv.xaf.back.dsp.service.itg.resid;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeCertificatResidenceCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeChangementSituationCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeDuplicataCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeNouvelleCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeRenouvellementCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidEtatsDemandesUpdatedAfterDTO;
import mc.gouv.xaf.back.dsp.dto.ResidHttpResponseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidIdTSDTO;
import mc.gouv.xaf.back.dsp.dto.ResidResidentCorrespondanceDTO;
import mc.gouv.xaf.back.dsp.dto.ResidStatutDemandeDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidInitialDemandeParamDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidUsagerNpdhlDTO;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

public interface ResidApiService {

    ResidHttpResponseDTO submitNouvelleCarteResid(ResidDemandeNouvelleCarteCompleteDTO nouvelleCarte,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException;

    ResidHttpResponseDTO submitRenouvellementCarteResid(ResidDemandeRenouvellementCarteCompleteDTO carteRenouvellement,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException;

    ResidHttpResponseDTO submitDuplicataCarteResid(ResidDemandeDuplicataCarteCompleteDTO duplicataCarte,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException;

    ResidHttpResponseDTO submitChangementSituationResid(ResidDemandeChangementSituationCompleteDTO changementsituation,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException;

    ResidHttpResponseDTO submitCertificatResid(ResidDemandeCertificatResidenceCompleteDTO certificatResidence,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException;

    ResidStatutDemandeDTO getEtatDemande(ResidIdTSDTO idDemande, String url, String jwt)
            throws JsonProcessingException, ResidHttpResponseException;

    List<ResidStatutDemandeDTO> getEtatMultipleDemandes(List<ResidIdTSDTO> idsDemandes, String url, String jwt)
            throws JsonProcessingException, ResidHttpResponseException;

    ResidEtatsDemandesUpdatedAfterDTO getEtatsDemandesUpdated(String updatedAfter, String url, String jwt)
            throws ResidHttpResponseException;

    List<ResidResidentCorrespondanceDTO> getListResidCorrespondance(String numeroCarte, String url, String jwt);

    ResidUsagerNpdhlDTO getUsagerDln1f(ResidInitialDemandeParamDTO paramDTO, String url, String jwt, Integer usagerId)
            throws ParseException;

    void setLastSuccessfulSynchroProperty(String lastSuccessfulSynchroTime);
}
