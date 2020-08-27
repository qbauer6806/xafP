package mc.gouv.xaf.servlet.dto;

import java.time.LocalDateTime;

public class FileUploadCompteurDTO {

    private int compteur;
    private LocalDateTime datePremierUpload;

    public int getCompteur() {
        return compteur;
    }

    public void setCompteur(int compteur) {
        this.compteur = compteur;
    }

    public LocalDateTime getDatePremierUpload() {
        return datePremierUpload;
    }

    public void setDatePremierUpload(LocalDateTime datePremierUpload) {
        this.datePremierUpload = datePremierUpload;
    }
}
