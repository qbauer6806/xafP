package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.data.entity.MotifBO;
import mc.gouv.xaf.shared.dto.ExportMotifDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;

/**
 * @author qdeme
 */
public class MotifTransformer {

    private MotifTransformer() {
    }

    public static MotifDTO bo2Dto(MotifBO bo) {
        MotifDTO dto = new MotifDTO();
        dto.setLangue(bo.getLangue());
        dto.setLibelle(bo.getLibelle());
        dto.setCode(bo.getCode());
        dto.setStatut(bo.getStatut());
        dto.setStatutCourant(bo.getStatutCourant());
        dto.setPkMotifs(bo.getPkMotifs());
        dto.setDateArchive(bo.getDateArchive());
        dto.setCommentairePrerempli(bo.getCommentairePrerempli());
        dto.setTexteAEnvoyer(bo.getTexteAEnvoyer());
        return dto;
    }

    public static MotifBO dto2Bo(MotifDTO dto) {
        MotifBO bo = new MotifBO();
        bo.setLangue(dto.getLangue());
        bo.setLibelle(dto.getLibelle());
        bo.setCode(dto.getCode());
        bo.setStatut(dto.getStatut());
        bo.setStatutCourant(dto.getStatutCourant());
        bo.setPkMotifs(dto.getPkMotifs());
        bo.setDateArchive(dto.getDateArchive());
        bo.setCommentairePrerempli(dto.getCommentairePrerempli());
        bo.setTexteAEnvoyer(dto.getTexteAEnvoyer());
        return bo;
    }

    public static List<MotifDTO> bo2Dto(List<MotifBO> bos) {
        ArrayList<MotifDTO> dtos = new ArrayList<>();
        for (MotifBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<MotifBO> dto2Bo(List<MotifDTO> dtos) {
        ArrayList<MotifBO> bos = new ArrayList<>();
        for (MotifDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

    public static List<MotifBO> exportDto2Bo(List<ExportMotifDTO> dtos) {
        ArrayList<MotifBO> bos = new ArrayList<>();
        for (ExportMotifDTO dto : dtos) {
            bos.add(exportDto2Bo(dto));
        }
        return bos;
    }

    public static MotifBO exportDto2Bo(ExportMotifDTO dto) {
        MotifBO bo = new MotifBO();
        bo.setCode(dto.getCode());
        bo.setLibelle(dto.getLibelle());
        bo.setStatut(dto.getStatut());
        bo.setStatutCourant(dto.getStatutCourant());
        bo.setLangue(dto.getLangue());
        bo.setDateArchive(dto.getDateArchive());
        bo.setCommentairePrerempli(dto.getCommentairePrerempli());
        bo.setTexteAEnvoyer(dto.getTexteAEnvoyer());
        return bo;
    }

    public static List<ExportMotifDTO> bo2ExportDto(List<MotifBO> bos) {
        ArrayList<ExportMotifDTO> dtos = new ArrayList<>();
        for (MotifBO bo : bos) {
            dtos.add(bo2ExportDto(bo));
        }
        return dtos;
    }

    public static ExportMotifDTO bo2ExportDto(MotifBO bo) {
        ExportMotifDTO dto = new ExportMotifDTO();
        dto.setCode(bo.getCode());
        dto.setLibelle(bo.getLibelle());
        dto.setStatut(bo.getStatut());
        dto.setStatutCourant(bo.getStatutCourant());
        dto.setLangue(bo.getLangue());
        dto.setDateArchive(bo.getDateArchive());
        dto.setCommentairePrerempli(bo.getCommentairePrerempli());
        dto.setTexteAEnvoyer(bo.getTexteAEnvoyer());
        return dto;
    }

}
