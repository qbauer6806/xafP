package mc.gouv.xaf.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contient le nom et le libellé d'un statut public (DEM) ou interne (démarche)
 *
 * @author qdeme
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatutPublicOuInterneDTO {
    
    private String name;

    private String libelle;

    @Override
    public String toString() {
        return libelle;
    }

}
