var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");

/* Create an array with the values of all the input boxes in a column */
$.fn.dataTable.ext.order['dom-text'] = function(settings, col) {
	return this.api().column(col, {
		order : 'index'
	}).nodes().map(function(td, i) {
		return $('input', td).val();
	});
}

$.fn.dataTable.ext.order['dom-select'] = function(settings, col) {
	return this.api().column(col, {
		order : 'index'
	}).nodes().map(function(td, i) {
		return $('select', td).val();
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

var configurationDataTableFacets = {
	serverSide : false,
	"ajax" : {

		// Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
		"traditional" : true,
		"url" : APP.getContextPath() + "/ws/demandes/recherchechamps",
		"dataSrc" : function(json) {

			json['recordsTotal'] = json['length'];
			json['recordsFiltered'] = json['length'];

			// json['data'] = json['content'];
			$("#propertiesSize").html(json.length)
			return json;
		}
	},

	"columns" : [
			{
				"data" : "name",
				render : function(data, type, property) {
					var id = property.name.replace(/\./g, '');
					return '<span id="' + id + '" >' + property.name.trim()
							+ '</span>';
				}
			},
			{
				"data" : "label",
				"orderDataType" : "dom-text",
				type : 'string',
				render : function(data, type, property) {
					var disabled = '';
					var label = '';
					if (property.label) {
						label = property.label;
					}
					if (!property.editable) {
						disabled = 'disabled';
					}
					var id = property.name.replace(/\./g, '') + "Label";
					return '<div class="col-xs-10 col-md-10"><input type="text" value="'
							+ label
							+ '" name="'
							+ property.name
							+ '" class="form-control" id="'
							+ id
							+ '" title="" ' + disabled + '/></div>';
				}
			},
			{
				"data" : "categoryId",
				"orderDataType" : "dom-select",
				render : function(data, type, property) {
					var disabled = '';
					if (!property.editable) {
						disabled = 'disabled';
					}

					var id = property.name.replace(/\./g, '') + "Category";
					var selectBox = '<div class="col-xs-8 col-md-8" >'
							+ '<select style="margin-left: 1em"  name="'
							+ property.name
							+ '" id="'
							+ id
							+ '" class="form-control " '
							+ disabled
							+ '>'
							+ '<option value="">Merci de selectionner une catégorie</option>';
					if (property.allCategories) {
						property.allCategories.forEach(function(cat) {

							var selected = '';
							if (cat.id === property.categoryId) {
								selected = 'selected="selected"';
							}

							selectBox += '<option value="' + cat.id + '" '
									+ selected + '>' + cat.label + '</option>'
						});
					}
					selectBox += '</select></div>';
					return selectBox;
				}
			},
			{
				"data" : "enabled",
				"orderDataType" : "dom-checkbox",
				render : function(data, type, property) {
					var id = property.name.replace(/\./g, '') + "Check";
					var checked = '';
					var chkedLabel = '';
					if (property.enabled) {
						checked = 'checked';
						chekedLabel = 'Actif';
					} else {
						chekedLabel = 'Inactif';
					}

					var disabled = '';
					if (property.name.startsWith("complement.fichiers")
							|| property.name
									.startsWith("fichierinterne.fichiers")) {
						disabled = 'disabled';
					}

					return '<div class="form-group">  <div class="checkbox-custom checkbox-default">  <input type="checkbox" '
							+ checked
							+ ' id="'
							+ id
							+ '" name="'
							+ property.name
							+ '" '
							+ disabled
							+ '><label for="'
							+ id
							+ '" id="'
							+ property.name
							+ 'CheckLabel"  >'
							+ chekedLabel + '</label>' + '</div> </div>';

				}
			} ],
	"scrollY" : "700px",
	"scrollCollapse" : true,
	"paging" : false,
	autoWidth : false,
	filter : false,
	colReorder : true,
	language : frenchTranslation,
	iDisplayLength : 10
}

var tableProperties = $('#datatable-search-properties').DataTable(configurationDataTableFacets);

function equals(val1, val2) {
	if ((val1 == null && val2 === "") || (val1 === "" && val2 == null))
		return true;
	else
		return val1 === val2;
}

var changedProperties = [];

function checkSavePropertiesButton() {
	if (typeof changedProperties !== 'undefined'
			&& changedProperties.length > 0) {
		$("#savePropertiesButton").removeAttr("disabled");
	} else {
		$("#savePropertiesButton").attr("disabled", true);
	}
}

function checkPropertiesToUpdate(that) {
	var rowData = tableProperties.row($(that).parents('tr')).data();

	var id = that.name.replace(/\./g, '');
	var propLabel = tableProperties.$('#' + id + 'Label')[0].value;
	var catId = tableProperties.$('#' + id + 'Category')[0].value;
	var enabled = tableProperties.$('#' + id + 'Check').is(":checked");

	var propIndex = changedProperties.indexOf(that.name);

	if (propIndex === -1
			&& (!equals(propLabel, rowData.label)
					|| !equals(catId, rowData.categoryId) || enabled !== rowData.enabled)) {
		changedProperties.push(that.name);
		$(that).parents("tr").addClass("updated-value");
	}
	if (propIndex !== -1 && equals(propLabel, rowData.label)
			&& equals(catId, rowData.categoryId) && enabled === rowData.enabled) {
		changedProperties.splice(propIndex, 1);
		$(that).parents("tr").removeClass("updated-value");
	}
}

$("#datatable-search-properties").on('keyup', "input,select", function(e) {

	checkPropertiesToUpdate(this);
	checkSavePropertiesButton();
	
});

$("#datatable-search-properties").on('change', "select", function(e) {

	checkPropertiesToUpdate(this);
	checkSavePropertiesButton();
	
});

$("#datatable-search-properties").on(
		'change',
		"input[type='checkbox']",
		function(e) {

			if (this.labels[0].innerText === "Actif") {
				this.labels[0].innerText = "Inactif";
			} else {
				this.labels[0].innerText = "Actif";
			}

			if (this.name.startsWith("fichier")) {
				var idComplement = "complement" + this.name.replace(/\./g, '') + "Check";
				$("#" + idComplement).removeAttr("disabled").click().attr("disabled", true);
				var idFichierInterne = "fichierinterne" + this.name.replace(/\./g, '') + "Check";
				$("#" + idFichierInterne).removeAttr("disabled").click().attr("disabled", true);
			}

			checkPropertiesToUpdate(this);
			checkSavePropertiesButton();

		});

$("#savePropertiesButton").click(function() {

	$('#savePropertiesConfirmPanel').modal();
});

$("#savePropertiesConfirmButton").click(function() {

	var properties = [];

	changedProperties.forEach(function(p) {

		var id = p.replace(/\./g, '');
		properties.push({
			"name" : tableProperties.$('#' + id)[0].innerText,
			"label" : tableProperties.$('#' + id + 'Label')[0].value,
			"categoryId" : tableProperties.$('#' + id + 'Category')[0].value,
			"enabled" : tableProperties.$('#' + id + 'Check').is(":checked")
		});
	});

	$.ajax({
		url : APP.getContextPath() + "/ws/admin/updaterecherchechamps",
		method : "POST",
		traditional : true,
		contentType : "application/json",
    data : JSON.stringify(properties),
		beforeSend : function(xhr) { xhr.setRequestHeader(header, token); },
		success : function(data) {
			$("#successMessage").data("message", "La configuration a été enregistrée avec succès").click();
			console.log("Success" + data);
			changedProperties = [];
			tableProperties.ajax.reload(null, true);
			$('.modal').modal('hide');
		},
		error : function(e) {
			$("#errorMessage").data("message", "Un problème est survenu lors de l'enregistrement de la configuration").click();
			console.log("failed to submit : " + e);
		}
	});

});

var configurationDataTableCategories = {
	serverSide : false,
	"ajax" : {

		// Pour ne pas envoyer canal[]=XXX non géré par Spring mvc
		"traditional" : true,
		"url" : APP.getContextPath() + "/ws/admin/categories",
		"dataSrc" : function(json) {

			json['recordsTotal'] = json['length'];
			json['recordsFiltered'] = json['length'];

			// json['data'] = json['content'];
			$("#categoriesSize").html(json.length)
			return json;
		}
	},

	"columns" : [
			{
				"data" : "label",
				"orderDataType" : "dom-text",
				type : 'string',
				render : function(data, type, property) {
					var disabled = '';
					if (!property.editable) {
						disabled = 'disabled';
					}
					return '<div class="col-xs-10 col-md-10"><input type="text" value="'
							+ property.label
							+ '" class="form-control" id="'
							+ property.id
							+ 'Label" title="" '
							+ disabled
							+ ' name="' + property.id + '"/></div>';
				}
			},
			{
				"data" : "action",
				"orderable" : false,
				render : function(data, type, property) {
					var disabled = '';
					if (!property.editable) {
						disabled = 'disabled';
					}
					return '<input type="submit" value="Supprimer" id="remove'
							+ property.id
							+ 'Button" class="btn btn-action ripple-effect "  name="'
							+ property.id + '" ' + disabled + '>';
				}
			} ],
	"scrollY" : "700px",
	"scrollCollapse" : true,
	"paging" : false,
	autoWidth : false,
	filter : false,
	colReorder : true,
	language : frenchTranslation,
	iDisplayLength : 10
}

var tableCategories = $('#datatable-categories').DataTable(
		configurationDataTableCategories);

var changedCategories = [];

function checkSaveCatButton() {
	if (typeof changedCategories !== 'undefined'
			&& changedCategories.length > 0) {
		$("#saveCatButton").removeAttr("disabled");
	} else {
		$("#saveCatButton").attr("disabled", true);
	}
}

$("#datatable-categories").on('keyup', "input", function(e) {

	var catId = Number(this.name);

	var rowData = tableCategories.row($(this).parents('tr')).data();
	var categoryValue = tableCategories.$('#' + catId + 'Label')[0].value;

	var catIndex = changedCategories.indexOf(catId);
	if (catIndex === -1 && rowData.label !== categoryValue) {
		changedCategories.push(catId);
		$(this).parents("tr").addClass("updated-value");

	}
	if (catIndex !== -1 && rowData.label === categoryValue) {
		changedCategories.splice(catIndex, 1);
		$(this).parents("tr").removeClass("updated-value");
	}

	checkSaveCatButton();

});

$("#saveCatButton").click(function() {

	$('#saveCategoriesConfirmPanel').modal();
});

$("#saveCategoriesButton")
		.click(
				function() {

					var categories = [];

					changedCategories.forEach(function(catId) {
						categories
								.push({
									"id" : catId,
									"label" : tableCategories.$('#' + catId
											+ 'Label')[0].value
								});
					});

					$
							.ajax({
								url : APP.getContextPath()
										+ "/ws/admin/updatecategories",
								method : "POST",
								traditional : true,
								data : JSON.stringify(categories),
								contentType : "application/json",
								beforeSend : function(xhr) {
									xhr.setRequestHeader(header, token);
								},
								success : function(categories) {
									$("#successMessage")
											.data("message",
													"Les catégories ont bien été mis à jour avec succès")
											.click();
									console.log("Success" + categories);
									categories.forEach(function(category) {
										tableProperties.$(
												"select option[value='"
														+ category.id + "']")
												.text(category.label);
									});
									tableCategories.ajax.reload(null, true);
									$('.modal').modal('hide');

								},
								error : function(xhr) {
									var error = jQuery
											.parseJSON(xhr.responseText);
									$("#errorMessage").data(
											"message",
											"Un problème est survenu lors de la mise à jour des catégories: "
													+ error.message).click();
									console.log("failed to submit : " + error);
								}
							});

				});

var categoryToDelete;
$("#datatable-categories").on('click', "input", function(e) {
	if (this.type == "submit") {
		categoryToDelete = this.name;
		$('#removeCategoryConfirmPanel').modal();
	}
});

$("#deleteCategoryButton").click(
		function() {

			$
					.ajax({
						url : APP.getContextPath()
								+ "/ws/admin/deletecategory?id="
								+ categoryToDelete,
						method : "DELETE",
						traditional : true,
						contentType : "application/json",
						beforeSend : function(xhr) {
							xhr.setRequestHeader(header, token);
						},
						success : function(data) {

							tableCategories.ajax.reload(null, true);
							tableProperties.$(
									"select option[value='" + categoryToDelete
											+ "']").remove();
							categoryToDelete = null;
							$('.modal').modal('hide');
							$("#successMessage").data("message",
									"La catégorie a été supprimée avec succès")
									.click();

						},
						error : function(xhr) {

							var error = jQuery.parseJSON(xhr.responseText);
							$("#errorMessage").data(
									"message",
									"Un problème est survenu lors de la suppression de la catégorie: "
											+ error.message).click();
							$('.modal').modal('hide');
							console.log("failed to submit : " + error);
						}
					});

		});

$(
		"#cancelDeleteCategoryButton, #cancelSaveCategoriesButton, #cancelAddCategoryConfirmButton, #cancelSavePropertiesButton")
		.click(function() {

			$('.modal').modal('hide');

		});

$("#addCategoryButton").click(function() {

	$("#libelleCatInput").val('');
	$("#catErrorMessage").css("visibility", "hidden").css("display", "none");

	$('#addCategoryPanel').modal();
});

$("#addCategoryConfirmButton").click(
		function() {
			$.ajax({
				url : APP.getContextPath() + "/ws/admin/addcategory?label="
						+ $("#libelleCatInput").val(),
				method : "POST",
				traditional : true,
				contentType : "application/json",
				beforeSend : function(xhr) {
					xhr.setRequestHeader(header, token);
				},
				success : function(data) {
					tableCategories.ajax.reload(null, true);
					tableProperties.$("select").append($('<option/>', {
						value : data.id,
						text : data.label
					}));

					$("#successMessage").data("message",
							"La catégorie a été enregistrée avec succès")
							.click();
					$("#libelleCatInput").val('');
					$("#catErrorMessage").css("visibility", "hidden").css(
							"display", "none");
					$('.modal').modal('hide');

				},
				error : function(xhr) {
					var error = jQuery.parseJSON(xhr.responseText);

					$("#catErrorMessage").text(
							"Un problème est survenu lors de l'enregistrement de la catégorie: "
									+ error.message);
					$("#catErrorMessage").css("visibility", "visible").css(
							"display", "block");

					console.log("failed to submit : " + error);
				}
			});
		});

$("#submitImportConfigButtonButton").click(
		function() {

			if ($("#importConfigButtonButton").val() === '') {
				$("#errorMessage").data("message",
						"Le fichier à importer est obligatoire").click();
			} else {
				var filedata = $('#importConfigButtonButton').prop('files')[0];
				var formdata = new FormData();
				formdata.append('file', filedata);
				$.ajax({
					url : APP.getContextPath() + '/ws/admin/import', // point
					// to
					// server-side
					// PHP
					// script
					cache : false,
					contentType : false,
					processData : false,
					beforeSend : function(xhr) {
						xhr.setRequestHeader(header, token);
					},
					data : formdata,
					type : 'post',
					success : function(message) {
						$("#successMessage").data("message", message).click();
						tableCategories.ajax.reload(null, true);
						tableProperties.ajax.reload(null, true);
						$("#importConfigButtonButton").val('');
					},
					error : function(xhr) {
						var error = jQuery.parseJSON(xhr.responseText);
						$("#errorMessage").data(
								"message",
								"Un problème est survenu lors de l'import de la configuration: "
										+ error.message).click();
					}
				});
			}
		});
