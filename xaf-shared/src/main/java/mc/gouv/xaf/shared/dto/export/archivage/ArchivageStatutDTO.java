package mc.gouv.xaf.shared.dto.export.archivage;

public class ArchivageStatutDTO {

    private double progression;

    private int nbFichiersEnErreur;

    private ArchivageStatutAvancementEnum avancement;

    public double getProgression() {
        return progression;
    }

    public void setProgression(double progression) {
        this.progression = progression;
    }

    public int getNbFichiersEnErreur() {
        return nbFichiersEnErreur;
    }

    public void setNbFichiersEnErreur(int nbFichiersEnErreur) {
        this.nbFichiersEnErreur = nbFichiersEnErreur;
    }

    public ArchivageStatutAvancementEnum getAvancement() {
        return avancement;
    }

    public void setAvancement(ArchivageStatutAvancementEnum avancement) {
        this.avancement = avancement;
    }
}
