var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");

var jobsColumns = [
		{
			"data" : "jobName",
			render : function(data, type, job) {
				return '<span >' + job.jobName.trim() + '</span>';
			}
		},
		{
			"data" : "dateCreation",
			render : function(data, type, job) {

				return '<span>'
						+ moment(job.dateCreation)
								.format("DD/MM/YYYY HH:mm:ss") + '</span>';
			}
		},
		{
			"data" : "dateCreation",
			render : function(data, type, job) {
				return '<span>'
						+ moment(job.dateDernModif).format(
								"DD/MM/YYYY HH:mm:ss") + '</span>';
			}
		}, {
			"data" : "statut",
			render : function(data, type, job) {
				
				var statutClass='';
				
				switch (job.statutCode.trim()) {
				  case 'RUNNING':
					  statutClass='job-en-cours-execution'
				    break;
				  case 'ERROR':
					  statutClass='job-erreur'
				    break;
				  case 'SUCCEEDED':
					  statutClass='job-reussi'
				    break;	  
				  default:
					  statutClass='job-statut-inconnu';
				}
				
				return '<span class="label statusbox '+statutClass+'">' + job.statut.trim() + '</span>';
			}
		}, {
			"data" : "msg",
			render : function(data, type, job) {
				return '<span>' + job.msg.trim() + '</span>';
			}
		} ];

var configurationDataTableJobs = {
	serverSide : true,
	"ajax" : {

		// Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
		"traditional" : true,
		"url" : APP.getContextPath() + "/ws/admin/job/list",

		"dataSrc" : function(json) {

			json['recordsTotal'] = json['totalElements'];
			json['recordsFiltered'] = json['totalElements'];

			$("#jobsSize").html(json.totalElements)
			// json['data'] = json['content'];
			return json.content;
		},
		"data" : function(params) {

			// Suppression de toutes les valeurs renvoyées par datatable
			params.columns = null;
			params.search = null;

			// conversion des params pour Pageable Spring MVC
			params.size = params.length;
			params.page = params.start / params.length;

			params.sort = "dateCreation,desc";
		}
	},
	"columns" : jobsColumns,

	autoWidth : false,
	filter : false,
	language : frenchTranslation,
	iDisplayLength : 10

}

var tableJobs = $('#datatable-jobs').DataTable(configurationDataTableJobs);

$("#executeJobButton").click(function() {

	$('#executeJobConfirmPanel').modal();

});

$("#executeJobConfirmationButton").click(
		function() {

			$.ajax({
				url : APP.getContextPath() + "/ws/admin/job/execute?jobName="
						+ $("#jobsSelectId").val(),
				method : "POST",
				traditional : true,
				contentType : "application/json",
				beforeSend : function(xhr) {
					xhr.setRequestHeader(header, token);
				},
				success : function(data) {
					$("#successMessage").data("message",
							"Demande d'exécution prise en compte").click();
					console.log("Success" + data);
					tableJobs.ajax.reload(null, true);
					$('.modal').modal('hide');

				},
				error : function(xhr) {
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

$("#cancelExecuteJobButton").click(function() {

	$('.modal').modal('hide');

});

