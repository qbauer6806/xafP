package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeArticleBO;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeArticleDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xdecool.ext
 */
public class CommandeDemandeArticleTransformer {

    private CommandeDemandeArticleTransformer() {
    }

    public static CommandeDemandeArticleDTO bo2Dto(CommandeDemandeArticleBO bo) {
        CommandeDemandeArticleDTO dto = new CommandeDemandeArticleDTO();
        dto.setPkCommandesDemandesArticles(bo.getPkCommandesDemandesArticles());
        dto.setFkCommandeDemande(bo.getCommandeDemande().getPkCommandesDemandes());
        dto.setCodeTarif(bo.getCodeTarif());
        dto.setMontant(bo.getMontant());
        return dto;
    }

    public static CommandeDemandeArticleBO dto2Bo(CommandeDemandeArticleDTO dto) {
        CommandeDemandeArticleBO bo = new CommandeDemandeArticleBO();
        bo.setPkCommandesDemandesArticles(dto.getPkCommandesDemandesArticles());
        bo.setCodeTarif(dto.getCodeTarif());
        bo.setMontant(dto.getMontant());
        return bo;
    }

    public static List<CommandeDemandeArticleDTO> bos2Dtos(List<CommandeDemandeArticleBO> bos) {
        ArrayList<CommandeDemandeArticleDTO> dtos = new ArrayList<>();
        for (CommandeDemandeArticleBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<CommandeDemandeArticleBO> dtos2Bos(List<CommandeDemandeArticleDTO> dtos) {
        ArrayList<CommandeDemandeArticleBO> bos = new ArrayList<>();
        for (CommandeDemandeArticleDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
