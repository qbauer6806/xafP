package mc.gouv.af.backweb.dto;

/**
 * Contient les données à afficher dans la page de recherche d'usagers courrier
 * 
 * @author qdeme
 *
 */
public class UsagerCourrierResultDTO {
    
    private Integer usagerId;
    
    private String nomRaisonSociale;
    
    private String nomPrenom;
    
    private String codePostal;
    
    private String ville;
    
    private String adresse;
    
    private String raisonSociale;
    
    private int nbDemandes;
    
    public Integer getUsagerId() {
        return usagerId;
    }
    
    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public String getNomRaisonSociale() {
        return nomRaisonSociale;
    }

    public void setNomRaisonSociale(String nomRaisonSociale) {
        this.nomRaisonSociale = nomRaisonSociale;
    }

    public String getNomPrenom() {
        return nomPrenom;
    }

    public void setNomPrenom(String nomPrenom) {
        this.nomPrenom = nomPrenom;
    }

    public String getCodePostal() {
        return codePostal;
    }
    
    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }
    
    public String getVille() {
        return ville;
    }
    
    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getRaisonSociale() {
        return raisonSociale;
    }
    
    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public int getNbDemandes() {
        return nbDemandes;
    }

    public void setNbDemandes(int nbDemandes) {
        this.nbDemandes = nbDemandes;
    }

}
