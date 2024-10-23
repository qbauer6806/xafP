package mc.gouv.xaf.shared.enums;

import java.text.Collator;

/**
 * Enum représentant les modes de transmission (ou canaux) possibles pour une demande.
 *
 * @author qdeme
 */
public enum DemandeCanalEnum {

    GUICHET_VIRTUEL("Téléservice"),
    GUICHET_PHYSIQUE("Guichet physique"),
    COURRIER("Courrier");

    private final String libelle;

    DemandeCanalEnum(String libelle) {
        this.libelle = libelle;
    }

    public static DemandeCanalEnum getByLibelle(String libelle) {

        for (DemandeCanalEnum demandeCanalEnum : values()) {
            final Collator instance = Collator.getInstance();
            // This strategy mean it'll ignore the accents
            instance.setStrength(Collator.NO_DECOMPOSITION);

            if (demandeCanalEnum.libelle.equals(libelle)) {
                return demandeCanalEnum;
            }
        }

        return null;
    }

    public static String getSearchText(String text) {
        String serachText = text;
        if (text != null) {
            for (DemandeCanalEnum canal : values()) {
                if (text.toLowerCase().contains(canal.libelle.toLowerCase())) {
                    serachText = serachText.toLowerCase().replace(canal.libelle.toLowerCase(), canal.name());
                }
            }
        }

        return serachText;
    }

    public static String getLibelleFromName(String name) {
        String libelle = name;
        for (DemandeCanalEnum canal : values()) {
            if (canal.name().equals(name)) {
                libelle = canal.libelle;
            }
        }
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }

}
