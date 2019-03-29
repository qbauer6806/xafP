package mc.gouv.af.back.data.es.model;

import java.util.List;

public class DemandeEsJmsDto {

    DemandeEsDTO demande;
    List<DemandeFileEsDTO> files;

    public DemandeEsJmsDto() {
        super();
    }

    public DemandeEsJmsDto(DemandeEsDTO demande, List<DemandeFileEsDTO> files) {
        super();
        this.demande = demande;
        this.files = files;
    }

    public DemandeEsDTO getDemande() {
        return demande;
    }

    public void setDemande(DemandeEsDTO demande) {
        this.demande = demande;
    }

    public List<DemandeFileEsDTO> getFiles() {
        return files;
    }

    public void setFiles(List<DemandeFileEsDTO> files) {
        this.files = files;
    }

}
