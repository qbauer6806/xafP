package mc.gouv.af.back.pdf;

public enum PdfType {
	
	COURRIER("COURRIER"),
	FICHIER_INTERNE("FICHIER_INTERNE"),
	FICHIER_REMIS_ADMINISTRATION("FICHIER_REMIS_ADMINISTRATION");

	public String libelle;

	PdfType(String libelle) {
		this.libelle = libelle;
	}

	public static PdfType getFromLibelle(String libelle) {
		for(PdfType type : PdfType.values()){
			if(libelle.equals(type.libelle)) {
				return type;
			}
		}
		return null;
	}
}
