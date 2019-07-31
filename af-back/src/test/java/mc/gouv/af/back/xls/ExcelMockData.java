package mc.gouv.af.back.xls;

import java.util.Calendar;
import java.util.Date;

import mc.gouv.dem.shared.model.DemandeCanalEnum;

public class ExcelMockData {

	public static final Integer PK_DEMANDES = 2056;
	public static final Integer PK_USAGER_ID = 1024;
	public static final String USAGER_PRENOM = "Martin";
	public static final String USAGER_NOM = "Gautier";
	public static final String USAGER_EMAIL = "test@email.net";
	public static final Date DATE_CREATION = getDate(1, 3, 2019);
	public static final String LANGUE = "fr";
	public static final String CANAL = DemandeCanalEnum.GUICHET_VIRTUEL.libelle;
	public static final String OBSERVATIONS = "Test de observation";
	public static final String AGENT_AFFECT_ID = "1234";
	public static final String AGENT_AFFECT_NAME = "Jaques Mora";
	public static final String DERNIER_STATUT = "EN_ATTENTE_COMPL";
	public static final String IDENTIFIANT = "ECS-20190305-GGK3";
	public static final Date COURRIER_DATE_RECEPTION = getDate(12, 3, 2019);
	public static final String COURRIER_REF_INTERNE = "Ref-Test-1234";
	public static final Date COURRIER_DATE_DERNIER_MODIFICATION = getDate(15, 3, 2019);
	public static final String USAGER_TITRE = "Mr";
	public static final String ADDRESS_LIGNE1 = "Avenue romain rolland";
	public static final String ADDRESS_LIGNE2 = "Bat B.";
	public static final String ADDRESS_LIGNE3 = "";
	public static final String CODE_POSTAL = "06000";
	public static final String VILLE = "Cannes";
	public static final String COMMENT = "Commentaire de test afback";
	public static final String COUNTRY = "France";
	public static final String DIPLOME = "DEUG/IUT/License";
	public static final String INFO_SUPPL = "Information supplémentaire pour le demarche de test";
	public static final String LANGUEUAGES = "Français,Anglais,Chinois";
	public static final String SKILLS = "Conception de base de donné, Gestion de projet";
	public static final String VACANCY = "Informatique";
	
	
	

	private static Date getDate(int d, int m, int y) {
		Calendar cal = Calendar.getInstance();
		cal.set(y, m, d);
		return cal.getTime();
	}

}
