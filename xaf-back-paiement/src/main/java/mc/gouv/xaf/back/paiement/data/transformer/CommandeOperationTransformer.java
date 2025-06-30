package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.CommandeOperationBO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class CommandeOperationTransformer {

    private CommandeOperationTransformer() {
    }

    public static CommandeOperationDTO bo2Dto(CommandeOperationBO bo) {
        CommandeOperationDTO dto = new CommandeOperationDTO();
        dto.setPkOperations(bo.getPkOperations());
        dto.setFkCommandes(bo.getCommande().getPkCommandes());
        dto.setDateCreation(bo.getDateCreation());
        dto.setMontant(bo.getMontant());
        dto.setDateRealisation(bo.getDateRealisation());
        dto.setErrorCode(bo.getErrorCode());
        dto.setErrorMessage(bo.getErrorMessage());
        dto.setOperationType(OperationTypeTransformer.bo2String(bo.getOperationType()));
        dto.setOperationStatut(OperationStatutTransformer.bo2String(bo.getOperationStatut()));
        return dto;
    }

    public static mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO bo2DtoMonetico(CommandeOperationBO bo) {
        mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO dto = new mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO();
        //dto.setPkOperations(bo.getPkOperations());
        dto.setFkCommandes(bo.getCommande().getPkCommandes());
        dto.setDateCreation(bo.getDateCreation());
        dto.setMontant(bo.getMontant());
        dto.setDateRealisation(bo.getDateRealisation());
        dto.setErrorCode(bo.getErrorCode());
        dto.setErrorMessage(bo.getErrorMessage());
        dto.setOperationType(OperationTypeTransformer.bo2String(bo.getOperationType()));
        dto.setOperationStatut(OperationStatutTransformer.bo2String(bo.getOperationStatut()));
        return dto;
    }

    public static CommandeOperationBO dto2Bo(CommandeOperationDTO dto) {
        CommandeOperationBO bo = new CommandeOperationBO();
        bo.setPkOperations(dto.getPkOperations());
        bo.setDateCreation(dto.getDateCreation());
        bo.setMontant(dto.getMontant());
        bo.setDateRealisation(dto.getDateRealisation());
        bo.setErrorCode(dto.getErrorCode());
        bo.setErrorMessage(dto.getErrorMessage());
        bo.setOperationType(OperationTypeTransformer.string2Bo(dto.getOperationType()));
        bo.setOperationStatut(OperationStatutTransformer.string2Bo(dto.getOperationStatut()));
        return bo;
    }

    public static CommandeOperationBO dto2BoMonetico(mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO dto) {
        CommandeOperationBO bo = new CommandeOperationBO();
        // TODO bo.setPkOperations(dto.getPkOperations());
        bo.setDateCreation(dto.getDateCreation());
        bo.setMontant(dto.getMontant());
        bo.setDateRealisation(dto.getDateRealisation());
        bo.setErrorCode(dto.getErrorCode());
        bo.setErrorMessage(dto.getErrorMessage());
        bo.setOperationType(OperationTypeTransformer.string2Bo(dto.getOperationType()));
        bo.setOperationStatut(OperationStatutTransformer.string2Bo(dto.getOperationStatut()));
        return bo;
    }

    public static List<CommandeOperationDTO> bos2Dtos(List<CommandeOperationBO> bos) {
        ArrayList<CommandeOperationDTO> dtos = new ArrayList<>();
        for (CommandeOperationBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO> bos2DtosMonetico(List<CommandeOperationBO> bos) {
        ArrayList<mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO> dtos = new ArrayList<>();
        for (CommandeOperationBO bo : bos) {
            dtos.add(bo2DtoMonetico(bo));
        }
        return dtos;
    }

    public static List<CommandeOperationBO> dtos2Bos(List<CommandeOperationDTO> dtos) {
        ArrayList<CommandeOperationBO> bos = new ArrayList<>();
        for (CommandeOperationDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
}
