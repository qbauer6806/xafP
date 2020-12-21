package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsStatutEnum;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesComplementsTransformer {

    private DemandesComplementsTransformer() {
    }

    public static DemandeComplementsDTO bo2Dto(DemandesComplementsBO bo) {
        if (bo == null) {
            return null;
        }

        DemandeComplementsDTO dto = new DemandeComplementsDTO();
        if (bo.getAgentId() != null || bo.getDateCreation() != null || bo.getQuestion() != null) {
            DemandeComplementsQuestionDTO question = new DemandeComplementsQuestionDTO();
            // Cacher l'agentId au Front Office
            if (!DemarchesUtils.isFrontUser()) {
                question.setAgentId(bo.getAgentId());
            }
            question.setDate(bo.getDateCreation());
            question.setTexte(bo.getQuestion());
            question.setCodeMotif(bo.getCodeMotif());
            dto.setQuestion(question);
        }
        if (bo.getReponseAgentId() != null || bo.getReponseUsagerId() != null || bo.getDateReponse() != null
                || (bo.getFiles() != null && !bo.getFiles().isEmpty()) || bo.getReponse() != null) {
            DemandeComplementsReponseDTO reponse = new DemandeComplementsReponseDTO();
            // Cacher l'agentId au Front Office
            if (!DemarchesUtils.isFrontUser()) {
                reponse.setAgentId(bo.getReponseAgentId());
            }
            reponse.setUsagerId(bo.getReponseUsagerId());
            reponse.setDate(bo.getDateReponse());
            ArrayList<DemandesComplementsFilesBO> filesBo = new ArrayList<DemandesComplementsFilesBO>(bo.getFiles());
            reponse.setFichiers(DemandesComplementsFilesTransformer.bo2Dto(filesBo)
                    .toArray(new DemandeComplementsFileDTO[filesBo.size()]));
            reponse.setTexte(bo.getReponse());
            dto.setReponse(reponse);
        }
        dto.setDemandeId(bo.getFkDemandes().getPkDemandes());
        dto.setPkDemandeComplements(bo.getPkDemandesComplements());
        dto.setStatut(DemandeComplementsStatutEnum.valueOf(bo.getStatut()));
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction Ainsi que les fichiers associés
     * 
     * @param dto
     * @return
     */
    public static DemandesComplementsBO dto2Bo(DemandeComplementsDTO dto) {
        if (dto == null) {
            return null;
        }
        DemandesComplementsBO bo = new DemandesComplementsBO();
        if (dto.getReponse() != null) {
            bo.setDateReponse(dto.getReponse().getDate());
            List<DemandesComplementsFilesBO> filesBo = DemandesComplementsFilesTransformer
                    .dto2Bo(new ArrayList<DemandeComplementsFileDTO>(Arrays.asList(dto.getReponse().getFichiers())));
            bo.setFiles(new HashSet<DemandesComplementsFilesBO>(filesBo));
            bo.setReponse(dto.getReponse().getTexte());
            bo.setReponseAgentId(dto.getReponse().getAgentId());
            bo.setReponseUsagerId(dto.getReponse().getUsagerId());
        }
        if (dto.getQuestion() != null) {
            bo.setAgentId(dto.getQuestion().getAgentId());
            bo.setDateCreation(dto.getQuestion().getDate());
            bo.setQuestion(dto.getQuestion().getTexte());
            bo.setCodeMotif(dto.getQuestion().getCodeMotif());
        }
        bo.setPkDemandesComplements(dto.getPkDemandeComplements());
        bo.setStatut(dto.getStatut().name());
        return bo;
    }

    public static List<DemandeComplementsDTO> bo2Dto(List<DemandesComplementsBO> bos) {
        ArrayList<DemandeComplementsDTO> dtos = new ArrayList<DemandeComplementsDTO>();
        for (DemandesComplementsBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static Set<DemandeComplementsDTO> bo2Dto(Set<DemandesComplementsBO> bos) {
        Set<DemandeComplementsDTO> dtos = new HashSet<>();
        for (DemandesComplementsBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandesComplementsBO> dto2Bo(List<DemandeComplementsDTO> dtos) {
        ArrayList<DemandesComplementsBO> bos = new ArrayList<DemandesComplementsBO>();
        for (DemandeComplementsDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
