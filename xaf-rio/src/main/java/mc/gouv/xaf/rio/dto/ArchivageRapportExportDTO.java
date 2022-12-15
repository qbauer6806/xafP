package mc.gouv.xaf.rio.dto;

import mc.gouv.xaf.shared.dto.DemandeFlatDTO;

import java.util.ArrayList;
import java.util.List;

public class ArchivageRapportExportDTO {

    private String demarcheId;

    private DemandeFlatDTO demandeFlatDTO;

    private List<ArchivageFichierInitalDTO> fichiersInitiaux;

    private List<ArchivageFichierConvertiDTO> fichiersConvertis;

    private List<ArchivageFichierDeposeDTO> fichiersDeposes;

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public List<ArchivageFichierInitalDTO> getFichiersInitiaux() {
        return fichiersInitiaux;
    }

    public void setFichiersInitiaux(List<ArchivageFichierInitalDTO> fichiersInitiaux) {
        this.fichiersInitiaux = fichiersInitiaux;
    }

    public void addFichiersInitiaux(ArchivageFichierInitalDTO fichier) {
        if (this.fichiersInitiaux == null) {
            this.fichiersInitiaux = new ArrayList<>();
        }

        if (fichier != null) {
            fichier.setRang(this.fichiersInitiaux.size() + 1 + "");
            this.fichiersInitiaux.add(fichier);
        }
    }

    public List<ArchivageFichierConvertiDTO> getFichiersConvertis() {
        return fichiersConvertis;
    }

    public void setFichiersConvertis(List<ArchivageFichierConvertiDTO> fichiersConvertis) {
        this.fichiersConvertis = fichiersConvertis;
    }

    public void addFichiersConvertis(ArchivageFichierConvertiDTO fichier) {
        if (this.fichiersConvertis == null) {
            this.fichiersConvertis = new ArrayList<>();
        }

        if (fichier != null) {
            fichier.setRang(this.fichiersConvertis.size() + 1 + "");
            this.fichiersConvertis.add(fichier);
        }
    }

    public List<ArchivageFichierDeposeDTO> getFichiersDeposes() {
        return fichiersDeposes;
    }

    public void setFichiersDeposes(List<ArchivageFichierDeposeDTO> fichiersDeposes) {
        this.fichiersDeposes = fichiersDeposes;
    }

    public void addFichiersDeposes(ArchivageFichierDeposeDTO fichier) {
        if (this.fichiersDeposes == null) {
            this.fichiersDeposes = new ArrayList<>();
        }

        if (fichier != null) {
            fichier.setRang(this.fichiersDeposes.size() + 1 + "");
            this.fichiersDeposes.add(fichier);
        }
    }

    public DemandeFlatDTO getDemandeFlatDTO() {
        return demandeFlatDTO;
    }

    public void setDemandeFlatDTO(DemandeFlatDTO demandeFlatDTO) {
        this.demandeFlatDTO = demandeFlatDTO;
    }
}
