/**
 * Gestion des erreurs générique
 */
var checkLengthGeneric = function(commentaireId, maxlength) {
	var val = $(commentaireId).val();
	var len = val ? val.length : 0;
	return showTooltip(commentaireId, len > maxlength);
};

var showTooltip = function(commentaireId, state) {
	var formGroupId = commentaireId + 'FormGroup';
	if (state) {
		$(formGroupId).addClass('error');
		$(commentaireId).tooltip("enable");
		$(commentaireId).tooltip("show");
	} else {
		$(commentaireId).tooltip("hide");
		$(formGroupId).removeClass('error');
		$(commentaireId).tooltip("disable");
	}
	return state;
};