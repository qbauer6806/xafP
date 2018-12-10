package mc.gouv.af.back.service.transformer;

import mc.gouv.af.back.data.es.model.UsagerEsDTO;
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
        usagerEsDTO.setId((bo.getId() != null) ? bo.getId().toString() : null);
        usagerEsDTO.setLogin(bo.getLogin());
        usagerEsDTO.setNom(bo.getNom());
        usagerEsDTO.setNomPays(bo.getNomPays());
        usagerEsDTO.setPrenom(bo.getPrenom());
        usagerEsDTO.setRaisonSociale(bo.getRaisonSociale());
        usagerEsDTO.setTitre((bo.getTitre() != null) ? bo.getTitre().toString() : null);
        usagerEsDTO.setVille(bo.getVille());

        return usagerEsDTO;

    }

}
