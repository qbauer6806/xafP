/**
 * Gestion des erreurs générique
 */
function checkLengthGeneric(commentaireId, maxlength) {
  const val = $(commentaireId).val();
  const len = val ? val.length : 0;
  const formGroupId = commentaireId + 'FormGroup';
  return showTooltip(commentaireId, formGroupId, len > maxlength);
}

/**
 * Affiche des tooltips contenant les messages d'erreur, et passe le champ en classe CSS d'erreur
 *
 * @param commentaireId l'id du champ en erreur
 * @param formGroupId l'id du groupe contenant le champ en erreur
 * @param state l'état du champ
 * @return state
 */
function showTooltip(commentaireId, formGroupId, state) {
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
}

/**
 * Permet de générer la partie détails de la demande
 */
function genererDemandeDetails() {
  if ($(".dynamictab").length === 1) {
    const demarcheId = window.location.pathname.split("/")[1];
    const pkDemande = window.location.pathname.split("/")[3];
    $.get(`/${demarcheId}/ws/recap/${pkDemande}`, function (data) {
      $(".dynamictab")[0].innerHTML = data;
    });
  }
}

/**
 * Permet de désactiver les boutons après validation
 */
function changeBackgroundColorRadio() {
  //Gestion du background pour le radio choisi
  $('#form-validation input[type="radio"]').each(function () {
    const _this = $(this);
    //Dans le cas ou on doit cliquer sur modifier pour modifier
    _this.parent().toggleClass('disabled', _this.prop('disabled'));
  });
}

/**
 * Permet de gérer le scroll du champ commentaires internes
 */
function scrollCommentairesToBottom() {
  const commentairesInternes = $("#commentairesInternes");
  if (commentairesInternes.length > 0) {
    commentairesInternes.scrollTop(commentairesInternes[0].scrollHeight);
  }
}

/**
 * Permet d'afficher la liste des messages au load de la page
 * Les listes des messages sont loadés
 */
function afficherListeMessages() {
  if (messagesSuccessList !== 'undefined' && messagesSuccessList !== null) {
    $.each(messagesSuccessList, function (key, msg) {
      $.notify({
        message: msg
      }, {
        type: 'success'
      });
    });
  }

  if (messagesErrorList !== 'undefined' && messagesErrorList !== null) {
    $.each(messagesErrorList, function (key, msg) {
      $.notify({
        message: msg
      }, {
        type: 'danger'
      });
    });
  }
}

/**
 * Affiche une popup pour confirmer une reprise en charge
 *
 * utilisée dans la page sectiontraitement.html des démarches
 */
function confirmerRepriseEnChrage(e) {
  if (!confirm(
      "La demande ne sera plus affectée à l’utilisateur " + utilisateurAffecte
      + ", êtes-vous sûr de vouloir continuer ?")) {
    e.preventDefault();
    return false;
  }
}

/**
 * Validation du formulaire de traitement de la demadne
 * @param e
 */
function validerFormulaireTraitement(e) {

  //Vérification si un radio required a bien été coché
  if ($("#NoRadio").length === 0 && $(
      '#form-validation input[type="radio"]').length > 0 && $(
      '#form-validation input[type="radio"]:checked').length === 0) {
    $.notify({
      message: "Veuillez cocher une décision concernant la demande."
    }, {
      type: 'danger'
    });
    e.preventDefault();
    return false;
  } else {
    // Prevent double form submission
    $('form input[type=submit]').prop('disabled', true);
  }

  // enable fields to send the info through submit
  codeMotifChoisi.prop('readonly', true);
  codeMotifChoisi.prop('disabled', false);
  commentaireUsager.prop('readonly', true);
  commentaireUsager.prop('disabled', false);
  texteAEnvoyer.prop('readonly', true);
  texteAEnvoyer.prop('disabled', false);

  //On est obligé de mettre un input hidden avec action=Poursuivre.... pour permettre d'envoyer la bonne action cliquée car une fois le bouton disablé
  //action=XXX n'est pas envoyé puisque cela fait parti du bouton submit.
  const form = $("form[id='form-validation']");
  form.find("input[type=submit]").addClass("loading");
  //on disable toutes les ancres et les buttons submit
  const buttonClicked = $("input[type=submit][data-clicked=true]");
  const val = buttonClicked.val(); //Valeur de l'action
  const name = buttonClicked.prop("name"); //action
  $('<input>').attr({
    type: 'hidden',
    name: name,
    value: val
  }).appendTo(form);
}

/**
 * Met à jour les commentaires d'annulation dans la popup d'annulation d'une demande
 */
function updateCommentairesAnnulation() {
  const codeMotifChosi = $("#codeMotifAnnuleeChoisi").val();
  for (const motif of motifs) {
    if (motif.code === codeMotifChosi) {
      $('#com-annulation').val(motif.commentairePrerempli);
    }
  }
}

/**
 * Ajoute un element HTML <option> à un <select>
 * @param select
 * @param optionCode
 * @param optionLibelle
 */
function appendSelectOption(select, optionCode, optionLibelle) {
  select.append($('<option>', {
    value: optionCode,
    text: optionLibelle
  }));
}

function getFloat(s) {
  if (s === "") {
    return 0;
  }
  return parseFloat(s.replace(',', '.'));
}

function getStr(f) {
  if (isNaN(f)) {
    return "0,0";
  }
  return f.toFixed(2).toString().replace('.', ',');
}
