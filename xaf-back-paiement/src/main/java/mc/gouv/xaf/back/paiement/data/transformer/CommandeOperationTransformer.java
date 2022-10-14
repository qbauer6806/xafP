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
        dto.setOperationType(OperationTypeTransformer.bo2String(bo.getOperationType()));
        dto.setOperationStatut(OperationStatutTransformer.bo2String(bo.getOperationStatut()));
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerniereModification(bo.getDateDerniereModification());
        dto.setMontant(bo.getMontant());
        dto.setNumeroAutorisation(bo.getNumeroAutorisation());
        dto.setNumeroFacture(bo.getNumeroFacture());
        dto.setCodeRetour(bo.getCodeRetour());
        dto.setLibelle(bo.getLibelle());
        return dto;
    }

    public static CommandeOperationBO dto2Bo(CommandeOperationDTO dto) {
        CommandeOperationBO bo = new CommandeOperationBO();
        bo.setPkOperations(dto.getPkOperations());
        bo.setOperationType(OperationTypeTransformer.string2Bo(dto.getOperationType()));
        bo.setOperationStatut(OperationStatutTransformer.string2Bo(dto.getOperationStatut()));
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerniereModification(dto.getDateDerniereModification());
        bo.setMontant(dto.getMontant());
        bo.setNumeroAutorisation(dto.getNumeroAutorisation());
        bo.setNumeroFacture(dto.getNumeroFacture());
        bo.setCodeRetour(dto.getCodeRetour());
        bo.setLibelle(dto.getLibelle());
        return bo;
    }

    public static List<CommandeOperationDTO> bos2Dtos(List<CommandeOperationBO> bos) {
        ArrayList<CommandeOperationDTO> dtos = new ArrayList<>();
        for (CommandeOperationBO bo : bos) {
            dtos.add(bo2Dto(bo));
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
