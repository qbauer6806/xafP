package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DemandeAgentDTO {

    private String id;

    private String nom;

    private String nomUsage;

    private String nomNaissance;

    private String prenom;

    private String mail;

    private String nomAffichage;

    public DemandeAgentDTO(String id) {
        this.id = id;
    }

}
