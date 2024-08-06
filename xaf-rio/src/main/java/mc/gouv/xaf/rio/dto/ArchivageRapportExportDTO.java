package mc.gouv.xaf.rio.dto;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.dto.DemandeFlatDTO;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ArchivageRapportExportDTO {

    private String demarcheId;

    private String codeNotice;

    private String refDocument;

    private DemandeFlatDTO demandeFlatDTO;

    private List<ArchivageFichierInitalDTO> fichiersInitiaux;

    private List<ArchivageFichierConvertiDTO> fichiersConvertis;

    private List<ArchivageFichierDeposeDTO> fichiersDeposes;

    public void addFichiersInitiaux(ArchivageFichierInitalDTO fichier) {
        if (this.fichiersInitiaux == null) {
            this.fichiersInitiaux = new ArrayList<>();
        }

        if (fichier != null) {
            fichier.setRang(this.fichiersInitiaux.size() + 1 + "");
            this.fichiersInitiaux.add(fichier);
        }
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

    public void addFichiersDeposes(ArchivageFichierDeposeDTO fichier) {
        if (this.fichiersDeposes == null) {
            this.fichiersDeposes = new ArrayList<>();
        }

        if (fichier != null) {
            fichier.setRang(String.valueOf(this.fichiersDeposes.size() + 1));
            this.fichiersDeposes.add(fichier);
        }
    }

}
