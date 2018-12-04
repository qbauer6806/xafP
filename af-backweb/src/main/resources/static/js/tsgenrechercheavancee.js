$(document).ready(function() {


	configurationDataTable.order = [ 0 , "asc" ]

	var default_category = "Autres"


	if (document.URL.includes("demandes")) {

		columns.unshift({
			"data" : "highlightedField",
			"orderable": false,
			"keyword" : false,
			render : function(data, type, demande) {

				var fragments = ""
				var fragmentsCat = []
				for (var fragment in demande.highlightedField) {

					var fragmentLabel = fragment
					var fragmentCategory = default_category
					if (recherche_libelles.get(fragment)) {
						var fragmentLabels = recherche_libelles.get(fragment)
						fragmentLabel = fragmentLabels.libelle.replace(/'/g,"&apos;")
						fragmentCategory = fragmentLabels.categorie.replace(/'/g,"&apos;")

					}

					fragmentsCat.push({
						category : fragmentCategory + ' - ' + fragmentLabel,
						fragment : demande.highlightedField[fragment]
					})

				}


				fragmentsCat.sort(function(a, b) {
					return (a.category < b.category) ? -1 : (a.category > b.category) ? 1 : 0;
				});

				for (var fc in fragmentsCat) {
					fragments += '<span style="font-size: 15px;text-decoration: underline;">' + fragmentsCat[fc].category + ' :</span> <br/>' + fragmentsCat[fc].fragment + '<br/>'
				}


				if (fragments !== "") {
					return "<a href='javascript:;' class='col-md-12 text-center greyTooltip'   data-toggle='tooltip'	data-placement='right' " +
					"title='" + fragments + "' data-html='true' href='#'><span class='badge'>i</span></a>";
				}
				
				return ""


				
			}
		})
	}


	APP.selectedField = null

	APP.buildFacets = function() {
		var facetParams = self.buildFacetsSerachData({})

		var orderAndInsertFacets = function(categories, facetsByCat) {
			var categoriesArray = Object.values(categories)
			categoriesArray.sort(function(a, b) {
				var contentA = $(a).attr('id');
				var contentB = $(b).attr('id');
				return (contentA < contentB) ? -1 : (contentA > contentB) ? 1 : 0;
			});

			
			for(var i=0; i<categoriesArray.length;i=i+6)
			{
				var endSubTab=i+6;
				if(i+5>=categoriesArray.length)
					endSubTab=categoriesArray.length;
				$("#rechercheavancee").append($("<div class='row' style='margin-bottom:1em'>").append(categoriesArray.slice(i, endSubTab)))
				
			}	
				

			for (cat in facetsByCat) {
				facetsByCat[cat].sort(function(a, b) {
					var contentA = $(a).find("#facetName")[0].innerText;
					var contentB = $(b).find("#facetName")[0].innerText;
					return (contentA < contentB) ? -1 : (contentA > contentB) ? 1 : 0;
				});

				$("#" + cat).append(facetsByCat[cat])
			}
		}

		$.ajax({
			url : APP.getContextPath() + "/ws/demandes/facets",
			method : "GET",
			traditional : true,
			data : facetParams
		}).done(function(facets) {
			
			
			if(facets !== undefined && facets.length > 0)
			{
				$("#affinerDiv").show()
			}
			else
			{
				$("#affinerDiv").hide()
			}

			if ($("#affinerLink").hasClass('collapsed') === true) {
				$("#affinerLink").click()
			}

			$("#rechercheavancee").empty()

			var linkIdSuffix = ".link"

			var facetClick = function() {

				$("#rechercheavancee  a").removeClass("active-facet")
				$(this).addClass("active-facet")
				APP.selectedField = $(this).attr('id').replace(linkIdSuffix, '');
				$('#datatable-demandes').DataTable().ajax.reload(null, true);
				
			}

			var categories = []
			var facetsByCat = {}
			for (facet in facets) {

				var facetSize = facets[facet].size;
				if (facetSize != 0) {

					var facetName = facets[facet].name;
					var category = default_category
					var categoryId = default_category
					var libelle = facetName
					facetName=facetName.replace(/'/g,"")
					var facetLink = facetName + linkIdSuffix
					if (recherche_libelles.get(facetName)) {
						category = recherche_libelles.get(facetName).categorie
						categoryId = category.split(' ').join('_')
						libelle = recherche_libelles.get(facetName).libelle
					}
					
					categoryId = categoryId.replace(/'/g,"")

					if (!categories[categoryId]) {
						var newfacetCategoryDiv = $("#facetCategory").clone();
						newfacetCategoryDiv.attr("id", categoryId.replace(/'/g,""))
						newfacetCategoryDiv
							.find("#facetCategoryName").html(category);
						newfacetCategoryDiv.show()
						categories[categoryId] = newfacetCategoryDiv
						facetsByCat[categoryId] = []
					}
					

					var newfacetDiv = $("#facet").clone()
					newfacetDiv.attr("id", facetName).find("#facetName").html(libelle.replace(/'/g,"&apos;"))
					newfacetDiv.find("#facetLink").attr("id", facetLink).on("click", facetClick)
					newfacetDiv.find("#facetSize").html(facetSize)
					facetsByCat[categoryId].push(newfacetDiv)
					//newfacetDiv.appendTo("#" + categoryId)
					newfacetDiv.show();

				}


			}
			
			

			orderAndInsertFacets(categories, facetsByCat)

		}).fail(function(e) {
			console.log("failed tu get facets : " + e);
		});

	}
});

recherche_libelles.set("dateCreation", {
	libelle : "Date de création",
	categorie : "Autres"
});
recherche_libelles.set("dateDerModif", {
	libelle : "Date de dernière modification",
	categorie : "Autres"
});
recherche_libelles.set("canal.code", {
	libelle : "Code",
	categorie : "Canal"
});
recherche_libelles.set("canal.libelle", {
	libelle : "Mode de transmission",
	categorie : "Canal"
});
recherche_libelles.set("langue", {
	libelle : "Langue",
	categorie : "Autres"
});
recherche_libelles.set("dernierStatut.pkStatut", {
	libelle : "Id du statut",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.libelle", {
	libelle : "Libelle",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.date", {
	libelle : "Date",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.agentId", {
	libelle : "Id agent",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.usagerId", {
	libelle : "Id usager",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.codeMotif", {
	libelle : "Code du motif",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.commentaire", {
	libelle : "Commentaire",
	categorie : "Dernier statut"
});
recherche_libelles.set("dernierStatut.libelleMotif", {
	libelle : "Libelle du motif",
	categorie : "Dernier statut"
});
recherche_libelles.set("identifiant", {
	libelle : "Identifiant",
	categorie : "Autres"
});
recherche_libelles.set("access.usagerId", {
	libelle : "Id usager",
	categorie : "Usager"
});
recherche_libelles.set("access.active", {
	libelle : "Actif",
	categorie : "Usager"
});
recherche_libelles.set("access.demarcheId", {
	libelle : "Id démarche",
	categorie : "Usager"
});
recherche_libelles.set("access.fkAccess", {
	libelle : "FK access",
	categorie : "Usager"
});
recherche_libelles.set("fichiers.content", {
	libelle : "Contenu",
	categorie : "Pièce jointe"
});
recherche_libelles.set("fichiers.name", {
	libelle : "Nom",
	categorie : "Pièce jointe"
});
recherche_libelles.set("fichiers.url", {
	libelle : "Url",
	categorie : "Pièce jointe"
});
recherche_libelles.set("fichiers.complement.content", {
	libelle : "Contenu",
	categorie : "Complément de demande"
});
recherche_libelles.set("fichiers.complement.name", {
	libelle : "Nom de la pièce jointe",
	categorie : "Complément de demande"
});
recherche_libelles.set("fichiers.complement.url", {
	libelle : "Url de la pièce jointe",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.statut", {
	libelle : "Statut",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.pkDemandeComplements", {
	libelle : "Id du complément",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.question.texte", {
	libelle : "Question",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.reponse.date", {
	libelle : "Date de la réponse",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.question.agentId", {
	libelle : "Identifiant de l'agent qui a posé la question",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.question.date", {
	libelle : "Date de la question",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.reponse.usagerId", {
	libelle : "Identifiant de l'usager qui à répondu",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.demandeId", {
	libelle : "Identifiant de la demande",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.reponse.texte", {
	libelle : "Réponse",
	categorie : "Complément de demande"
});
recherche_libelles.set("complements.question.codeMotif", {
	libelle : "Code du motif",
	categorie : "Complément de demande"
});
recherche_libelles.set("statuts.pkStatut", {
	libelle : "Id du statut",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.libelle", {
	libelle : "Libelle du statut",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.date", {
	libelle : "Date du statut",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.agentId", {
	libelle : "Id agent",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.usagerId", {
	libelle : "Id usager",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.codeMotif", {
	libelle : "Code du motif",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.commentaire", {
	libelle : "Commentaire",
	categorie : "Historique des statuts"
});
recherche_libelles.set("statuts.libelleMotif", {
	libelle : "Libellé du motif",
	categorie : "Historique des statuts"
});
recherche_libelles.set("data.demandeId", {
	libelle : "Id de la demande",
	categorie : "Données"
});
recherche_libelles.set("data.key", {
	libelle : "Clé",
	categorie : "Données"
});
recherche_libelles.set("data.value", {
	libelle : "Valeur",
	categorie : "Données"
});
recherche_libelles.set("data.pkDemandesData", {
	libelle : "Id de la donnée",
	categorie : "Données"
});
recherche_libelles.set("usager.id", {
	libelle : "Id de l'usager",
	categorie : "Usager"
});
recherche_libelles.set("usager.nom", {
	libelle : "Nom",
	categorie : "Usager"
});
recherche_libelles.set("usager.prenom", {
	libelle : "Prénom",
	categorie : "Usager"
});
recherche_libelles.set("usager.adresse1", {
	libelle : "Adresse 1",
	categorie : "Usager"
});
recherche_libelles.set("usager.adresse2", {
	libelle : "Adresse 2",
	categorie : "Usager"
});
recherche_libelles.set("usager.codePostal", {
	libelle : "Code postal",
	categorie : "Usager"
});
recherche_libelles.set("usager.ville", {
	libelle : "Ville",
	categorie : "Usager"
});
recherche_libelles.set("usager.nomPays", {
	libelle : "Pays",
	categorie : "Usager"
});
recherche_libelles.set("usager.paysCode", {
	libelle : "Code du pays",
	categorie : "Usager"
});
recherche_libelles.set("usager.paysId", {
	libelle : "Id du pays",
	categorie : "Usager"
});
recherche_libelles.set("usager.complementAdresse", {
	libelle : "Complément d'adresse",
	categorie : "Usager"
});
recherche_libelles.set("usager.raisonSociale", {
	libelle : "Raison sociale",
	categorie : "Usager"
});
recherche_libelles.set("usager.dateActivation", {
	libelle : "Date d'activation",
	categorie : "Usager"
});
recherche_libelles.set("usager.dateCreation", {
	libelle : "Date de création",
	categorie : "Usager"
});
recherche_libelles.set("usager.dateDerConnexion", {
	libelle : "Date de dernière connexion",
	categorie : "Usager"
});
recherche_libelles.set("usager.email", {
	libelle : "Email",
	categorie : "Usager"
});
recherche_libelles.set("usager.etat", {
	libelle : "Etat",
	categorie : "Usager"
});
recherche_libelles.set("usager.login", {
	libelle : "Login",
	categorie : "Usager"
});
recherche_libelles.set("usager.titre", {
	libelle : "Titre",
	categorie : "Usager"
});

recherche_libelles.set("agent.civilite", {
	libelle : "Civilité",
	categorie : "Agent"
});
recherche_libelles.set("agent.matricule", {
	libelle : "Matricule",
	categorie : "Agent"
});
recherche_libelles.set("agent.nom", {
	libelle : "Nom",
	categorie : "Agent"
});
recherche_libelles.set("agent.nomUsage", {
	libelle : "Nom d'usage",
	categorie : "Agent"
});
recherche_libelles.set("agent.nomNaissance", {
	libelle : "Nom de naissance",
	categorie : "Agent"
});
recherche_libelles.set("agent.prenom", {
	libelle : "Prénom",
	categorie : "Agent"
});
recherche_libelles.set("agent.mail", {
	libelle : "Email",
	categorie : "Agent"
});
recherche_libelles.set("agent.service", {
	libelle : "Service",
	categorie : "Agent"
});
recherche_libelles.set("agent.ufCode", {
	libelle : "Code de l'unité fonctionnelle",
	categorie : "Agent"
});
recherche_libelles.set("agent.dateCreation", {
	libelle : "Date de création",
	categorie : "Agent"
});
recherche_libelles.set("agent.telPro", {
	libelle : "Téléphone professionnel",
	categorie : "Agent"
});
recherche_libelles.set("agent.telMobilePro", {
	libelle : "Téléphone mobile professionnel",
	categorie : "Agent"
});
recherche_libelles.set("agent.fonction", {
	libelle : "Fonction",
	categorie : "Agent"
});