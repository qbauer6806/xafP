package mc.gouv.af.servlet.dto;

public class AnnulationRequestDTO {
    
    private String codeMotif;
    
    private String commentaire;

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

}
