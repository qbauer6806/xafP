package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class CommandeDemandeTransformer {

    private CommandeDemandeTransformer() {
    }

    public static CommandeDemandeDTO bo2Dto(CommandeDemandeBO bo) {
        CommandeDemandeDTO dto = new CommandeDemandeDTO();
        dto.setPkCommandeDemandes(bo.getPkCommandesDemandes());
        dto.setFkCommandes(bo.getCommande().getPkCommandes());
        if (null != bo.getDemande()) {
            dto.setFkDemandes(bo.getDemande().getPkDemandes());
        }
        dto.setMontant(bo.getMontant());
        dto.setCommandeDemandeArticles(CommandeDemandeArticleTransformer.bos2Dtos(bo.getCommandesDemandesArticles()));
        return dto;
    }

    public static CommandeDemandeBO dto2Bo(CommandeDemandeDTO dto) {
        CommandeDemandeBO bo = new CommandeDemandeBO();
        bo.setPkCommandesDemandes(dto.getPkCommandeDemandes());
        bo.setMontant(bo.getMontant());
        return bo;
    }

    public static List<CommandeDemandeDTO> bos2Dtos(List<CommandeDemandeBO> bos) {
        ArrayList<CommandeDemandeDTO> dtos = new ArrayList<>();
        for (CommandeDemandeBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<CommandeDemandeBO> dtos2Bos(List<CommandeDemandeDTO> dtos) {
        ArrayList<CommandeDemandeBO> bos = new ArrayList<>();
        for (CommandeDemandeDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
