package mc.gouv.xaf.back.paiement.data.transformer;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.InformationFacturationBO;
import java.time.LocalDateTime;

public class InfoFacturationTransformer {

    private InfoFacturationTransformer() {
    }

    public static InformationFacturationBO infoFacturationResponseDTOToInfoFacturationBO(/*InfoFacturationResponseDTO dto, CommandeBO commande*/) {
        /*InformationFacturationBO bo = new InformationFacturationBO();
        bo.setDateCreation(LocalDateTime.now());
        VousDTO vous = dto.getVous();
        bo.setCivilite(vous.getTitre());
        bo.setPrenom(vous.getPrenom());
        bo.setNom(vous.getNom());
        bo.setRaisonSociale(dto.getRaisonSociale());
        AdresseDTO adresse = dto.getAdresse();
        bo.setAdresseLigne1(adresse.getLigne1());
        //    	bo.setAdresseLigne2(adresse.getLigne2());
        //    	bo.setAdresseLigne3(adresse.getLigne3());
        bo.setCodePostal(adresse.getCodePostal());
        bo.setVille(adresse.getVille());
        bo.setPays(adresse.getPays());
        bo.setEmail(dto.getEmail());
        bo.setCommande(commande);*/
        return null;

        //return bo;
    }

    public static InformationFacturationBO infoFacturationResponseDTOToInfoFacturationBO(String toto/*InfoFacturationResponseDTO dto*/) {
        /*InformationFacturationBO bo = new InformationFacturationBO();
        bo.setDateCreation(LocalDateTime.now());
        VousDTO vous = dto.getVous();
        bo.setCivilite(vous.getTitre());
        bo.setPrenom(vous.getPrenom());
        bo.setNom(vous.getNom());
        bo.setRaisonSociale(dto.getRaisonSociale());
        AdresseDTO adresse = dto.getAdresse();
        bo.setAdresseLigne1(adresse.getLigne1());
        //    	bo.setAdresseLigne2(adresse.getLigne2());
        //    	bo.setAdresseLigne3(adresse.getLigne3());
        bo.setCodePostal(adresse.getCodePostal());
        bo.setVille(adresse.getVille());
        bo.setPays(adresse.getPays());
        bo.setEmail(dto.getEmail());*/
        return null;
        //return bo;
    }

}
