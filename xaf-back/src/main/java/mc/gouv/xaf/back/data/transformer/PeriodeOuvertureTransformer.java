package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.PeriodesOuvertureBO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;

/**
 * 
 * @author qdeme
 *
 */
public class PeriodeOuvertureTransformer {

    private PeriodeOuvertureTransformer() {
    }
    
    public static PeriodeOuvertureDTO bo2Dto(PeriodesOuvertureBO bo) {
        PeriodeOuvertureDTO dto = new PeriodeOuvertureDTO();
        dto.setDateDebut(bo.getDateDebut());
        dto.setDateFin(bo.getDateFin());
        dto.setDemarcheId(bo.getDemarcheId());
        dto.setPkPeriodesOuverture(bo.getPkPeriodesOuverture());
        return dto;
    }
    
    public static PeriodesOuvertureBO dto2Bo(PeriodeOuvertureDTO dto) {
        PeriodesOuvertureBO bo = new PeriodesOuvertureBO();
        bo.setDateDebut(dto.getDateDebut());
        bo.setDateFin(dto.getDateFin());
        bo.setDemarcheId(dto.getDemarcheId());
        bo.setPkPeriodesOuverture(dto.getPkPeriodesOuverture());
        return bo;
    }
    
    public static List<PeriodeOuvertureDTO> bo2Dto(List<PeriodesOuvertureBO> bos) {
        ArrayList<PeriodeOuvertureDTO> dtos = new ArrayList<PeriodeOuvertureDTO>();
        for (PeriodesOuvertureBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }
    
    public static List<PeriodesOuvertureBO> dto2Bo(List<PeriodeOuvertureDTO> dtos) {
        ArrayList<PeriodesOuvertureBO> bos = new ArrayList<PeriodesOuvertureBO>();
        for (PeriodeOuvertureDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
    
}
