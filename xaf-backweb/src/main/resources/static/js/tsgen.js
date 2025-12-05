var APP = {};

APP.getContextPath = function () {
  return window.location.pathname.substring(0,
      window.location.pathname.indexOf("/", 2));
}

/**
 * Paramètre pour les datatables
 */

APP.buildDefaultDatafunction = function (params) {

  //Suppression de toutes les valeurs renvoyées par datatable
  params.columns = null;
  params.search = null;

  //conversion des params pour Pageable Spring MVC
  params.size = params.length;
  params.page = params.start / params.length;

  //sort=identifiant,desc
  params.sort = columns[params.order[0].column].data + ","
      + params.order[0].dir;

  if (!params.fields || params.fields.length === 0) {
    params.fields = 'data';
  } else if (!params.fields.includes('data')) {
    params.fields += ',data';
  }
}

APP.getConfigurationDataTableCourriers = function (columns, imprimes) {

  return {
    "processing": true,
    serverSide: true,
    "ajax": {

      //Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
      "traditional": true,
      "url": APP.getContextPath() + "/ws/courriers/pageable",
      "dataSrc": function (json) {

        json['recordsTotal'] = json['totalElements'];
        json['recordsFiltered'] = json['totalElements'];

        //json['data'] = json['content'];
        if (imprimes) {
          $("#numberImprimes").html(json.totalElements);
        } else {
          $("#numberEnAttente").html(json.totalElements);
        }
        return json.content;
      },
      "error": function (xhr, error, thrown) {
        if (xhr.status === 500 || xhr.status === 404 || xhr.status === 403) {
          window.location.href = APP.getContextPath() + '/error/' + xhr.status;
        }
      }
    },

    "columns": columns,
    autoWidth: false,
    filter: false,
    language: frenchTranslation,
    iDisplayLength: 5,
    lengthChange: false,
    order: [ [1, "desc"]], // NE PAS supprimer l'espace entre les deux accolades de debut
    // bugfix #29868 - afficher les tooltips dans les datatable
    drawCallback: function (settings) {
      $('[data-toggle="tooltip"]').tooltip();
    }
  };
}

APP.getConfigurationDataTable = function (columns) {
  return {
    serverSide: true,
    "ajax": {

      //Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
      "traditional": true,
      "url": APP.getContextPath() + "/ws/demandes/pageable",
      "dataSrc": function (json) {

        json['recordsTotal'] = json['totalElements'];
        json['recordsFiltered'] = json['totalElements'];

        //json['data'] = json['content'];
        $("#demandesSize").html(json.totalElements)
        return json.content;
      },
      "error": function (xhr, error, thrown) {
        if (xhr.status === 500 || xhr.status === 404 || xhr.status === 403) {
          window.location.href = APP.getContextPath() + '/error/' + xhr.status;
        }
      }
    },

    "columns": columns,
    autoWidth: false,
    filter: false,
    language: frenchTranslation,
    iDisplayLength: 10,
    // bugfix #29868 - afficher les tooltips dans les datatable
    drawCallback: function (settings) {
      $('[data-toggle="tooltip"]').tooltip();
    }
  };
}

$(document).ready(function () {

  $("#topRechercheButton").click(function (e) {
    $("#topRechercheButtonInput").click();
    return false;
  });

  /**
   * icônes ouvrir/fermer les accordéons
   */
  $('.panel-heading a').click(function () {
    var pannelState = $(this).find('.pannelState');
    if ($(this).hasClass('collapsed') === true) {
      pannelState.addClass('icon-action_accordeon-ouvert');
      pannelState.removeClass('icon-action_accordeon-ferme');
    } else {
      pannelState.addClass('icon-action_accordeon-ferme');
      pannelState.removeClass('icon-action_accordeon-ouvert');
    }
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

//en cas d'erreur lors des appels AJAX, on redirige vers la page des erreurs
$.ajaxSetup({
  error: function (xhr) {
    if (xhr.status === 500 || xhr.status === 404 || xhr.status === 403
        || xhr.status === 405) {
      window.location.href = APP.getContextPath() + '/error/' + xhr.status;
    }
  }
});

/**
 * Permet d'ajouter un commentaire à la discussion sans recharger toute la demande
 * @param $this L'objet jQuery du formulaire
 */
function ajouterCommentaireDiscussion($this) {
  console.log("toto");
  const commentaire = $('#com-interne-input');
  if (!commentaire.val()) {
    $.notify({
      message: "Veuillez renseigner un commentaire."
    }, {
      type: 'danger'
    });
    return false;
  }
  $('#envoyer-commentaire-button').addClass("loading");
  // Envoi de la requête HTTP en mode asynchrone
  $.ajax({
    dataType: "json",
    url: $this.attr('action'), // Le nom du fichier indiqué dans le formulaire
    type: $this.attr('method'), // La méthode indiquée dans le formulaire (get ou post)
    data: $this.serialize(), // On sérialise les données (on envoie toutes les valeurs présentes dans le formulaire)
    success: function (json) { // On récupère la réponse du fichier PHP
      //json retourné {"agentId":"19723","date":"2017-05-03T17:24:41+0200","commentaire":"salut 3"}
      //On récupère le commentaire sauvegardé pour pouvoir l'afficher.
      const contentMsg = '<div class="row"><div class="col-xs-12" style="background-color: #ffffff;line-height:1em"><div style="font-size:0.8em;font-weight:bold;color: rgb(94,97,100);">'
          + moment(json.date).format("DD/MM/YYYY HH:mm:ss")
          + '</div><div style="font-weight: bold;">' + utilisateurConnecte
          + '</div><div style="margin: 5px 0 8px 0 ;">' + filterXSS(
              json.commentaire) + '</div></div>'

      $("#commentairesInternes").append(contentMsg)

      //Scroll en bas pour voir le nouveau commentaire
      scrollCommentairesToBottom();
      //Remise à 0 du commentaire
      commentaire.val("");
      $('#envoyer-commentaire-button').removeClass("loading");
    },
    error: function () {
      $.notify({
        message: "Un problème est survenu lors de la publication du commentaire."
      }, {
        type: 'danger'
      });
      $('#envoyer-commentaire-button').removeClass("loading");
    }
  });
}
