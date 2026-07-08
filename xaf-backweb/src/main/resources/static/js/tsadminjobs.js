var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");

var jobsColumns = [
  {
    "data": "jobName",
    render: function (data, type, job) {
      return '<span >' + job.jobName.trim() + '</span>';
    }
  },
  {
    "data": "dateCreation",
    render: function (data, type, job) {

      return '<span>'
          + moment(job.dateCreation)
          .format("DD/MM/YYYY HH:mm:ss") + '</span>';
    }
  },
  {
    "data": "dateCreation",
    render: function (data, type, job) {
      return '<span>'
          + moment(job.dateDernModif).format(
              "DD/MM/YYYY HH:mm:ss") + '</span>';
    }
  }, {
    "data": "statut",
    render: function (data, type, job) {

      var statutClass = '';

      switch (job.statutCode.trim()) {
        case 'RUNNING':
          statutClass = 'job-en-cours-execution'
          break;
        case 'ERROR':
          statutClass = 'job-erreur'
          break;
        case 'SUCCEEDED':
          statutClass = 'job-reussi'
          break;
        default:
          statutClass = 'job-statut-inconnu';
      }

      return '<span class="label statusbox ' + statutClass + '">'
          + job.statut.trim() + '</span>';
    }
  }, {
    "data": "msg",
    render: function (data, type, job) {
      var msg = job.msg.trim();
      // Si le message contient des logs d'exécution, afficher un résumé + bouton pour ouvrir la modale
      if (msg.indexOf('--- Logs d') !== -1) {
        var parts = msg.split("--- Logs d'exécution ---");
        var msgHeader = (parts[0] || '').replace(/\n/g, '<br>');
        return '<div>' + msgHeader
            + '<a href="javascript:void(0);" class="btn-voir-logs" style="cursor:pointer;color:#337ab7;font-size:0.9em;text-decoration:underline;"'
            + ' data-jobname="' + job.jobName.trim() + '"'
            + ' data-msg="' + encodeURIComponent(job.msg) + '"'
            + '>Voir les logs d\'exécution</a></div>';
      }
      return '<span>' + msg.replace(/\n/g, '<br>') + '</span>';
    }
  }];

var configurationDataTableJobs = {
  serverSide: true,
  "ajax": {

    // Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
    "traditional": true,
    "url": APP.getContextPath() + "/ws/admin/job/list",

    "dataSrc": function (json) {

      json['recordsTotal'] = json['totalElements'];
      json['recordsFiltered'] = json['totalElements'];

      $("#jobsSize").html(json.totalElements)
      // json['data'] = json['content'];
      return json.content;
    },
    "data": function (params) {

      // Suppression de toutes les valeurs renvoyées par datatable
      params.columns = null;
      params.search = null;

      // conversion des params pour Pageable Spring MVC
      params.size = params.length;
      params.page = params.start / params.length;

      params.sort = jobsColumns[params.order[0].column].data + ","
          + params.order[0].dir;
    }
  },
  "columns": jobsColumns,
  "order": [[1, "desc"]],

  autoWidth: false,
  filter: false,
  language: frenchTranslation,
  iDisplayLength: 10

}

var tableJobs = $('#datatable-jobs').DataTable(configurationDataTableJobs);

$("#executeJobButton").click(function () {

  $('#executeJobConfirmPanel').modal();

});

$("#executeJobConfirmationButton").click(
    function () {

      $.ajax({
        url: APP.getContextPath() + "/ws/admin/job/execute?jobName="
            + $("#jobsSelectId").val(),
        method: "POST",
        traditional: true,
        contentType: "application/json",
        beforeSend: function (xhr) {
          xhr.setRequestHeader(header, token);
        },
        success: function (data) {
          $("#successMessage").data("message",
              "Demande d'exécution prise en compte").click();
          console.log("Success" + data);
          tableJobs.ajax.reload(null, true);
          $('.modal').modal('hide');

        },
        error: function (xhr) {
          var error = jQuery.parseJSON(xhr.responseText);
          $("#errorMessage").data(
              "message",
              "Un problème est survenu lors du lancement du job : "
              + error.message).click();
          console.log("failed to submit : " + error);
          $('.modal').modal('hide');
        }
      });
    });

$("#cancelExecuteJobButton").click(function () {

  $('.modal').modal('hide');

});

// Ouverture de la modale des logs d'exécution
$('#datatable-jobs').on('click', '.btn-voir-logs', function () {
  var jobName = $(this).data('jobname');
  var fullMsg = decodeURIComponent($(this).data('msg'));

  var separator = "--- Logs d'exécution ---";
  var parts = fullMsg.split(separator);
  var msgHeader = (parts[0] || '').trim();
  var logs = (parts[1] || '').trim();
  $('#jobLogsModalTitle').text(jobName);
  $('#jobLogsModalHeader').html(msgHeader.replace(/\n/g, '<br>'));

  // Coloriser les logs par niveau
  var colorizedLogs = colorizeLogLines(logs);
  $('#jobLogsModalContent').html(colorizedLogs);

  $('#jobLogsModal').modal();
});

function colorizeLogLines(logs) {
  if (!logs) return '';
  var lines = logs.split('\n');
  var result = '';
  for (var i = 0; i < lines.length; i++) {
    var line = escapeHtml(lines[i]);
    if (!line.trim()) continue;
    if (line.indexOf('] ERROR ') !== -1) {
      result += '<div style="color:#fc8181;font-weight:bold;">' + line + '</div>';
    } else if (line.indexOf('] WARN ') !== -1) {
      result += '<div style="color:#fbd38d;font-weight:bold;">' + line + '</div>';
    } else if (line.indexOf('] DEBUG ') !== -1) {
      result += '<div style="color:#a0aec0;">' + line + '</div>';
    } else {
      result += '<div style="color:#e2e8f0;">' + line + '</div>';
    }
  }
  return result;
}

function escapeHtml(text) {
  return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
}


