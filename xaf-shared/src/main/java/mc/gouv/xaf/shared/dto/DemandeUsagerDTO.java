package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DemandeUsagerDTO {

	private Integer id;
	private String etat;
	private String email;
	private String titre;
	private String prenom;
	private String nom;
	private String raisonSociale;
	private String adresse1;
	private String adresse2;
	private String complementAdresse;
	private String codePostal;
	private String ville;
	private String nomPays;
	private String login;

}
