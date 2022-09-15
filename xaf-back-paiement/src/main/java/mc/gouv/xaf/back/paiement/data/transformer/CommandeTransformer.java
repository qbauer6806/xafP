package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;

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
        dto.setMontant(bo.getMontant());
        dto.setPkCommande(bo.getPkCommande());
        dto.setDateCreation(bo.getDateCreation());
        return dto;
    }

    public static CommandeBO dto2Bo(CommandeDTO dto) {
        CommandeBO bo = new CommandeBO();
        bo.setMontant(dto.getMontant());
        bo.setPkCommande(dto.getPkCommande());
        bo.setDateCreation(dto.getDateCreation());
        return bo;
    }

    public static List<CommandeDTO> bos2Dtos(List<CommandeBO> bos) {
        ArrayList<CommandeDTO> dtos = new ArrayList<>();
        for (CommandeBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<CommandeBO> dtos2Bs(List<CommandeDTO> dtos) {
        ArrayList<CommandeBO> bos = new ArrayList<>();
        for (CommandeDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
