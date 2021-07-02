var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");
var textRenderer = $.fn.dataTable.render.text().display; 

/* Create an array with the values of all the input boxes in a column */
$.fn.dataTable.ext.order['dom-text'] = function(settings, col) {
	return this.api().column(col, {
		order : 'index'
	}).nodes().map(function(td, i) {
		return $('input', td).val();
	});
}

/* Create an array with the values of all the checkboxes in a column */
$.fn.dataTable.ext.order['dom-checkbox'] = function(settings, col) {
	return this.api().column(col, {
		order : 'index'
	}).nodes().map(function(td, i) {
		return $('input', td).prop('checked') ? '1' : '0';
	});
}

	
// BUGFIX (9 juillet 2019): La colonne pour la date est la 2 au lieu de 1
var searchDefaultSort = [ [ 2, "desc" ] ];


