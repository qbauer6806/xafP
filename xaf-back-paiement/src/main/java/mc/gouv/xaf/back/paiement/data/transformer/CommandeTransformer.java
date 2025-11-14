package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class CommandeTransformer {



    private CommandeTransformer() {
    }

    public static CommandeDTO bo2Dto(CommandeBO bo) {
        CommandeDTO dto = new CommandeDTO();
        dto.setPkCommandes(bo.getPkCommandes());
        dto.setDateCreation(bo.getDateCreation());
        dto.setMontantInitial(bo.getMontantInitial());
        dto.setMontantDejaCapture(bo.getMontantDejaCapture());
        dto.setMontantRestant(bo.getMontantRestant());
        dto.setMoyenPaiement(MoyenPaiementTransformer.bo2Dto(bo.getMoyenPaiement()));
        dto.setCommandesDemandes(CommandeDemandeTransformer.bos2Dtos(bo.getCommandesDemandes()));
        if (bo.getOperations() != null) {
            dto.setOperations(CommandeOperationTransformer.bos2Dtos(bo.getOperations()));
        }
        return dto;
    }

    public static CommandeBO dto2Bo(CommandeDTO dto) {
        CommandeBO bo = new CommandeBO();
        bo.setPkCommandes(dto.getPkCommandes());
        bo.setDateCreation(dto.getDateCreation());
        bo.setMontantInitial(dto.getMontantInitial());
        bo.setMontantDejaCapture(dto.getMontantDejaCapture());
        bo.setMontantRestant(dto.getMontantRestant());
        bo.setMoyenPaiement(MoyenPaiementTransformer.dto2Bo(dto.getMoyenPaiement()));
        return bo;
    }

    public static List<CommandeDTO> bos2Dtos(List<CommandeBO> bos) {
        ArrayList<CommandeDTO> dtos = new ArrayList<>();
        for (CommandeBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<CommandeBO> dtos2Bos(List<CommandeDTO> dtos) {
        ArrayList<CommandeBO> bos = new ArrayList<>();
        for (CommandeDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
