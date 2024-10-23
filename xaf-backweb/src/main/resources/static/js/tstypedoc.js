/*<![CDATA[*/

const filesJSON = {};
const filesCheckbox = {};
const complementsJSON = {};
const complementsCheckbox = {};

updateTypedocJSON = (event) => {
  const idEvent = event.id;
  const isComp = idEvent.includes("comp");
  if (isComp) {
    $("#typedoc-complements-button").attr("disabled", false);
    complementsJSON[idEvent.replace("comp", "")] = event.value;
  } else {
    $("#typedoc-files-button").attr("disabled", false);
    filesJSON[idEvent.replace("file", "")] = event.value;
  }
  const idToUpdateObj = document.getElementById(idEvent);
  document.getElementById(
      idEvent).title = idToUpdateObj.options[idToUpdateObj.selectedIndex].title;
}

updateCheckboxJSON = (event) => {
  const idEvent = event.id;
  const isComp = idEvent.includes("comp");
  if (isComp) {
    $("#typedoc-complements-button").attr("disabled", false);
    complementsCheckbox[idEvent.replace("compCheckbox", "")] = event.checked;
  } else {
    $("#typedoc-files-button").attr("disabled", false);
    filesCheckbox[idEvent.replace("fileCheckbox", "")] = event.checked;
  }
}

$("#typedoc-files-button").click(function (e) {
  execEnregistrerFichiers();
  e.preventDefault();
  return false;
});

$("#typedoc-complements-button").click(function (e) {
  execEnregistrerFichiers();
  e.preventDefault();
  return false;
});

var execEnregistrerFichiers = function () {
  $("#typedoc-complements").prop('value', JSON.stringify(complementsJSON));
  $("#typedoc-files").prop('value', JSON.stringify(filesJSON));
  $("#typedoc-complements-checkbox").prop('value',
      JSON.stringify(complementsCheckbox));
  $("#typedoc-files-checkbox").prop('value', JSON.stringify(filesCheckbox));
  $("#confirmer-typedoc-button").click();
};

var setCurrentPopup = function (previewButton) {
  const href = previewButton.parentNode.getAttribute('value');
  document.getElementById(previewButton.id).setAttribute('disabled',
      'disabled');
  document.getElementById(previewButton.id).classList.add("loading");
  // On close les tooltip
  $("[class='tooltip fade bottom in']").remove();
  if (!href.toLowerCase().endsWith('.pdf')) {
    basicLightbox.create('<img src="' + href + '">', {
      onClose: (instance) => enableButton(previewButton.id)
    }).show();
  } else {
    pdfjsLib.getDocument(href).promise.then(
        function (pdf) {
          // Supprimer tous les anciens canvas générés dans le dom
          $('.pdf-canvas').remove();
          //Start with first page
          pdf.getPage(1).then(
              createPdfCanvas.bind(null, 'canvas-container-file-pdf', 1,
                  pdf.numPages, pdf))
          .then(openPdfPrevisuModal('#preview-container-file-pdf',
              previewButton.id), function () {
            alert("Erreur lors de l'affichage du pdf");
          });
        },
        function (error) {
          enableButton(previewButton.id);
          $.notify({
            message: "Erreur lors de l'affichage du pdf"
          }, {
            type: 'danger'
          });
        }
    );
  }
};

$("#allDownloadButton").click(function () {
  var zipName = demandeId + "_Fichiers";
  var url = domZip + demandeId + "?fileType=all&zipName=" + zipName;
  location.href = url;
});

$("#validesDownloadButton").click(function () {
  if (clickable) {
    var zipName = demandeId + "_Fichiers_Valides";
    var url = domZip + demandeId + "?fileType=valides&zipName=" + zipName;
    location.href = url;
  }
});

$("#allDownloadButtonPdf").click(function () {
  var pdfName = demandeId + "_Fichiers";
  var url = domPdf + demandeId + "?fileType=all&pdfName=" + pdfName;
  $.ajax({
    url: url,
    method: 'GET',
    xhrFields: {
      responseType: 'blob'
    },
    beforeSend: function (xhr) {
      setPanelLoading();
    },
    success: function (data, textStatus, xhr) {
      disablePanelLoading();
      var a = document.createElement('a');
      var url = window.URL.createObjectURL(data);
      a.href = url;
      a.download = pdfName + '.pdf';
      document.body.append(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    }
  });
});

$("#validesDownloadButtonPdf").click(function () {
  if (clickable) {
    var pdfName = demandeId + "_Fichiers";
    var url = domPdf + demandeId + "?fileType=valides&pdfName=" + pdfName;
    $.ajax({
      url: url,
      method: 'GET',
      xhrFields: {
        responseType: 'blob'
      },
      beforeSend: function (xhr) {
        setPanelLoading();
      },
      success: function (data, textStatus, xhr) {
        disablePanelLoading();
        var a = document.createElement('a');
        var url = window.URL.createObjectURL(data);
        a.href = url;
        a.download = pdfName + '.pdf';
        document.body.append(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
      }
    });
  }
});

var setPanelLoading = function () {
  var tab2 = document.getElementById('tab2');
  var tabLoader = document.getElementById('tabLoader');
  var loaderMessage = document.getElementById('loaderMessage');
  tab2.classList.remove("active");
  loaderMessage.classList.remove("hide");
  tabLoader.classList.add("active");
}

var disablePanelLoading = function () {
  var tab2 = document.getElementById('tab2');
  var tabLoader = document.getElementById('tabLoader');
  var loaderMessage = document.getElementById('loaderMessage');
  tabLoader.classList.remove("active");
  tab2.classList.add("active");
  loaderMessage.classList.add("hide");
}

function enableButton(buttonId) {
  // Déverouillage des champs en cas d'annulation
  document.getElementById(buttonId).removeAttribute('disabled');
  document.getElementById(buttonId).classList.remove("loading");
}

var openPdfPrevisuModal = function (containerId, buttonId) {
  $(containerId).modal();
  $(containerId).on('hidden.bs.modal', enableButton(buttonId));

};

function checkRemplissageFichiers() {
  let tousRemplis = true;
  $('select[id^="file"], select[id^="comp"]').each(function (index, element) {
    if (!element.value || element.value === '') {
      return tousRemplis = false;
    }
  });
  return tousRemplis;
}

function checkVerificationFichiers() {
  let tousVerif = true;
  $('input[id^="fileCheckbox"], input[id^="compCheckbox"]').each(
      function (index, element) {
        if (!element.checked) {
          return tousVerif = false;
        }
      });
  return tousVerif;
}

/*]]>*/
