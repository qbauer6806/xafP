package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.data.model.RechercheChampDTO;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.RechercheChampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RechercheChampServiceImpl implements RechercheChampService {

    @Autowired
    private DemandesConfigService demandesConfigService;

	@Override
	public List<RechercheChampDTO> getRechercheChamps() {
        List<RechercheChampDTO> rechercheChamps = new ArrayList<>();
		// agent
		rechercheChamps.add(new RechercheChampDTO("agent.mail"));
		rechercheChamps.add(new RechercheChampDTO("agent.id"));
		rechercheChamps.add(new RechercheChampDTO("agent.nom"));
		rechercheChamps.add(new RechercheChampDTO("agent.nomNaissance"));
		rechercheChamps.add(new RechercheChampDTO("agent.nomUsage"));
		rechercheChamps.add(new RechercheChampDTO("agent.prenom"));

		// complement
		rechercheChamps.add(new RechercheChampDTO("complement.contenu"));
		rechercheChamps.add(new RechercheChampDTO("complement.name"));

		// courrier ?
//		demandesFacets.add(new DemandesFacet("dateCreation", 1));
//		demandesFacets.add(new DemandesFacet("dateDerModif", 1));

		// demande
		rechercheChamps.add(new RechercheChampDTO("identifiant"));
		rechercheChamps.add(new RechercheChampDTO("langue"));

		// dernier statut utile ?
//		demandesFacets.add(new DemandesFacet("dernierStatut.libelle", 1));
//		demandesFacets.add(new DemandesFacet("dernierStatut.date", 1));
//		demandesFacets.add(new DemandesFacet("dernierStatut.commentaire", 1));
//		demandesFacets.add(new DemandesFacet("dernierStatut.libelleMotif", 1));

		// fichiers internes utile ? doublon de "pièces jointes"
//		demandesFacets.add(new DemandesFacet("fichierinterne.contenu", 1));
//		demandesFacets.add(new DemandesFacet("fichierinterne.name", 1));

		// historique des statuts utile ?

		// contenu
        for (String chemin : demandesConfigService.getModelPathsRechercheAvancee()) {
            rechercheChamps.add(new RechercheChampDTO(chemin));
        }

		// informations réservées à l'administration
		rechercheChamps.add(new RechercheChampDTO("observations"));

		// pièces jointes
		rechercheChamps.add(new RechercheChampDTO("fichiers.contenu"));
		rechercheChamps.add(new RechercheChampDTO("fichiers.name"));

		// usager
		rechercheChamps.add(new RechercheChampDTO("usager.adresse1"));
		rechercheChamps.add(new RechercheChampDTO("usager.adresse2"));
		rechercheChamps.add(new RechercheChampDTO("usager.codePostal"));
		rechercheChamps.add(new RechercheChampDTO("usager.complementAdresse"));
		rechercheChamps.add(new RechercheChampDTO("usager.email"));
		rechercheChamps.add(new RechercheChampDTO("usager.login"));
		rechercheChamps.add(new RechercheChampDTO("usager.nom"));
		rechercheChamps.add(new RechercheChampDTO("usager.nomPays"));
		rechercheChamps.add(new RechercheChampDTO("usager.prenom"));
		rechercheChamps.add(new RechercheChampDTO("usager.raisonSociale"));
		rechercheChamps.add(new RechercheChampDTO("usager.titre"));
		rechercheChamps.add(new RechercheChampDTO("usager.ville"));

		return rechercheChamps;
	}


}
