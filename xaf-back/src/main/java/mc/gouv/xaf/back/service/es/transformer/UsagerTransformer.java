package mc.gouv.xaf.back.service.es.transformer;

import mc.gouv.xaf.back.data.es.model.UsagerEsDTO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.servicerest.usager.model.UsagerBean;

public class UsagerTransformer {

    private UsagerTransformer() {
    }

    public static UsagerEsDTO bo2Dto(UsagerBean bo) {

        if (bo == null)
            return null;

        UsagerEsDTO usagerEsDTO = new UsagerEsDTO();
        usagerEsDTO.setAdresse1(bo.getAdresse1());
        usagerEsDTO.setAdresse2(bo.getAdresse2());
        usagerEsDTO.setCodePostal(bo.getCodePostal());
        usagerEsDTO.setComplementAdresse(bo.getComplementAdresse());
        usagerEsDTO.setEmail(bo.getEmail());
        usagerEsDTO.setEtat((bo.getEtat() != null) ? bo.getEtat().toString() : null);
        usagerEsDTO.setNom(bo.getNom());
        usagerEsDTO.setNomPays(bo.getNomPays());
        usagerEsDTO.setPrenom(bo.getPrenom());
        usagerEsDTO.setRaisonSociale(bo.getRaisonSociale());
        usagerEsDTO.setTitre((bo.getTitre() != null) ? AfBackUtils.getTitreStr(bo.getTitre()) : null);
        usagerEsDTO.setVille(bo.getVille());

        return usagerEsDTO;

    }

}
