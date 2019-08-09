var textRenderer = $.fn.dataTable.render.text().display;

var columns = [
	
	{
		"data" : "identifiant",
		"keyword" : true,
		render : function(data, type, demande) {
			return "<a href='" + APP.getContextPath() + "/demandes/" + demande.pkDemandes + "'><span class='identifiant-demande'>" + demande.identifiant + "</span></a>";
		}
	},
	{
		"data" : "dateCreation",
		"keyword" : false,
		render : function(data, type, demande) {
			// On ne format pas en STRING la demande si l'accès à la donnée se fait pendant un SORT ou un TYPE
			if (type === "sort" || type === "type") {
                return data;
            }
			return moment(demande.dateCreation).format("DD/MM/YYYY");
		}
	},
	{
		"data" : "dernierStatut.libelle",
		"keyword" : true,
		render : function(data, type, demande) {
			return '<span  class="label statusbox ' + APP.getStatusColorClass(demande.dernierStatut.code) + '">' + demande.dernierStatut.libelle + '</span>';
		//return demande.dernierStatut.libelle;
		}
	},
	
	{
		"data" : "contenu.usager.nomaffichage",
		"keyword" : true,
		render : function(data, type, demande) {
			return textRenderer(demande.contenu.usager.nomaffichage);
		}
	},
	{
		"data" : "contenu.donnee.vehicule.nomaffichageproprietaire",
		"keyword" : true,
		render : function(data, type, demande) {
			return textRenderer(demande.contenu.donnee.vehicule.nomaffichageproprietaire);
		}
	},
	{
		"data" : "data.numeroOrdre",
		"keyword" : true,
		render : function(data, type, demande) {
			return ( demande.data == null || typeof demande.data["numeroOrdre"] == 'undefined' )?"":demande.data.numeroOrdre;
		}
	},
	{
		"data" : "langue",
		"keyword" : true,
	},
	{
		"data" : "canal.libelle",
		"keyword" : true,
		render : function(data, type, demande) {
			return demande.canal.libelle;
		}
	},
	{
		"data" : "agentAffecteNomAffichage",
		"keyword" : true,
		render : function(data, type, demande) {
			return demande.agentAffecteNomAffichage;
		}
	},
	{
		"orderable" : false,
		"keyword" : false,
		render : function(data, type, demande) {
			return '<a href="' + APP.getContextPath() + '/demandes/' + demande.pkDemandes + '"><span class="btn btn-action ripple-effect greyTooltip" data-toggle="tooltip" data-placement="bottom" title="" data-original-title="Visualiser"><i class="icon-action_editer"></i></a>';
		}
	}
];

// BUGFIX (9 juillet 2019): La colonne pour la date est la 2 au lieu de 1
var searchDefaultSort = [ [ 2, "desc" ] ];

var configurationDataTable = APP.getConfigurationDataTable(columns);