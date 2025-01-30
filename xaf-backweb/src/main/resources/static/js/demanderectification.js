// Gestion pour la demande de rectification d'une demande

var demandeRectifMailCallback = function (data) {
  openMailModal("#preview-container-second", null, "#preview-content-second", data);
  bindValiderAndCloseDemandeRectifButton();
};

var bindValiderAndCloseDemandeRectifButton = function() {
  const actionValider = $(".valider-preview");
  actionValider.unbind();
  actionValider.click(function (e) {
    $("#confirmer-button").removeClass("loading");
    e.preventDefault();
    $("#confirmer-rectif-button").click();
    return false;
  });
};
var checkRectificationFormValidity = function() {
  var commentaireLengthInvalid = checkLengthGeneric("#commentaireRectification", 1000);
  $("#envoyerDemandeRectifButton").prop('disabled', commentaireLengthInvalid);
};

function handleDemandeRectification(statut, motif) {
  $("#envoyerDemandeRectifButton").click(function (e) {
    e.preventDefault();
    const commentaire = $("#commentaireRectification").val();
    if (commentaire == null || commentaire.trim() === '') {
      $.notify({
        message: "Veuillez saisir les informations à modifier par l'usager"
      }, {
        type: 'danger'
      });
      return;
    }
    var form = {
      statutChoisi: statut,
      pkDemande: pkDemande,
      motifChoisiCode: motif,
      commentaire: commentaire
    };
    makePreviewCall(form, demandeRectifMailCallback);
  });
}