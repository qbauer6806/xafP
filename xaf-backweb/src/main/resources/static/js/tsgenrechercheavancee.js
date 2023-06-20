$(document).ready(function() {


	configurationDataTable.order = [ 0  ]

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
						fragmentLabel = escapeHtml(fragmentLabels.libelle)
						fragmentCategory = escapeHtml(fragmentLabels.categorie)

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
					
					categoryId = categoryId.replace(/('|;|&|\/|\(|\)|=)/g,"");

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

