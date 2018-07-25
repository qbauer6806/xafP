var APP = {};

APP.getContextPath = function() {
	   return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}

/**
 * Paramètre pour les datatables
 */

APP.buildDefaultDatafunction = function (params){
    
 
    //Suppression de toutes les valeurs renvoyées par datatable
    params.columns = null;
    params.search = null;
    
    //conversion des params pour Pageable Spring MVC
    params.size = params.length;
    params.page = params.start / params.length;
    
    //sort=identifiant,desc
    params.sort = columns[params.order[0].column].data+","+params.order[0].dir;
}

APP.getConfigurationDataTable = function(columns) {
	var configurationDataTable = {
		serverSide : true,
		"ajax": {
    		
    		//Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
    		"traditional": true,
		    "url": APP.getContextPath()+"/ws/demandes/pageable",
		    "dataSrc": function(json) {
		        json['recordsTotal'] = json['totalElements'];
		        json['recordsFiltered'] = json['totalElements'];
		        
		        //json['data'] = json['content'];
		        return json.content;
		     }
		  },
		  "columns": columns,
		  autoWidth: false,
	      filter : false,
		  language: frenchTranslation,
		  iDisplayLength : 10,
		            
	}
	return configurationDataTable;
}

$(document).ready(function() {
    $("#topRechercheButton").click(function(e){
        $("#topRechercheButtonInput").click();
    	return false;
     });
});

var configurationSpinner = {
		  lines: 13 // The number of lines to draw
		, length: 28 // The length of each line
		, width: 14 // The line thickness
		, radius: 42 // The radius of the inner circle
		, scale: 1 // Scales overall size of the spinner
		, corners: 1 // Corner roundness (0..1)
		, color: '#15627C' // #rgb or #rrggbb or array of colors
		, opacity: 0.25 // Opacity of the lines
		, rotate: 0 // The rotation offset
		, direction: 1 // 1: clockwise, -1: counterclockwise
		, speed: 1 // Rounds per second
		, trail: 60 // Afterglow percentage
		, fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
		, zIndex: 2e9 // The z-index (defaults to 2000000000)
		, className: 'spinner' // The CSS class to assign to the spinner
		, top: '50%' // Top position relative to parent
		, left: '50%' // Left position relative to parent
		, shadow: false // Whether to render a shadow
		, hwaccel: false // Whether to use hardware acceleration
		, position: 'absolute' // Element positioning
}
