package mc.gouv.xaf.shared.dto;

import java.util.Objects;

/**
 * Contient le nom et le libellé d'un statut public (DEM) ou interne (démarche)
 *
 * @author qdeme
 */
public class StatutPublicOuInterneDTO {

    private String name;

    private String libelle;

    public StatutPublicOuInterneDTO() {}

    public StatutPublicOuInterneDTO(String name, String libelle) {
        this.name = name;
        this.libelle = libelle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        final StatutPublicOuInterneDTO other = (StatutPublicOuInterneDTO) o;
        return Objects.equals(name, other.name) && Objects.equals(libelle, other.libelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, libelle);
    }
}
