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

//############### partie upload fichier #####################

//les paramètres de la requête
const listeFichiers = []; // liste temporaire
let fileIdCounter = 0;

function resetChamps() {
  document.getElementById('messageErreurUploadFichier').innerHTML = "";
  document.getElementById('uploadFile').value = "";
  document.querySelector('select[data-field="typeFichier"]').value = "";
  document.getElementById('upload-button').disabled = true;
  document.getElementById("fichiers-body").innerHTML = "";
  document.getElementById("contenuFichiers").style.display = "none";
  listeFichiers.length = 0;
  fileIdCounter = 0;
}

document.addEventListener('DOMContentLoaded', function () {
  const boutonUploadPiece = document.getElementById(
      'upload-piece-justificative');
  if (boutonUploadPiece) {
    boutonUploadPiece.addEventListener('click', function () {
      afficherPopupUploadFichier();
    });
  }
  const uploadButton = document.getElementById('upload-button');
  if (uploadButton) {
    uploadButton.addEventListener('click', function () {
      // Désactive le bouton pour éviter un double clic
      uploadButton.disabled = true;
      uploadFichier();

      setTimeout(() => {
        uploadButton.disabled = false;
      }, 2000); // 2 secondes
    });
  }
  // Affichage du message de succès si présent dans localStorage
  const msg = localStorage.getItem("uploadMessage");
  if (msg) {
    $.notify({message: msg}, {type: 'success'});
    localStorage.removeItem("uploadMessage");
  }
});

function afficherPopupUploadFichier() {
  resetChamps();
  $('#import-piece-justificative').modal();
}

function uploadFichier() {
  const htmlErreur = document.getElementById('messageErreurUploadFichier');
  htmlErreur.innerHTML = "";

  const validFiles = listeFichiers.filter(f => f); // enlever les null

  if (validFiles.length === 0) {
    htmlErreur.innerHTML = "Aucun fichier à envoyer";
    return;
  }
  const formData = new FormData();
  validFiles.forEach(item => {
    formData.append("files", item.file); // Tous les fichiers
  });
  // Construction des métadonnées
  const metadonnees = validFiles.map(item => ({
    nom: item.file.name,
    type: item.type,
    visibilite: item.visibilitePiece
  }));
  const value = new Blob([JSON.stringify(metadonnees)],
      {type: "application/json"});
  formData.append("metadonnees", value);

  const header = $("meta[name='_csrf_header']").attr("content");
  const token = $("meta[name='_csrf']").attr("content");

  const pkDemande = document.querySelector('input[name="pkDemande"]').value;
  // Envoi
  const url = APP.getContextPath() + "/ws/file/upload/" + pkDemande;
  $.ajax({
    url: url,
    method: "POST",
    data: formData,
    processData: false,
    contentType: false,
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function (response) {
      localStorage.setItem("uploadMessage", response);
      location.assign(APP.getContextPath() + "/demandes/" + pkDemande
          + "?demandeTab=fichiers");
    },
    error: function (xhr, status, error) {
      htmlErreur.innerHTML = handleAjaxError(xhr, status, error);
      if (xhr.status === 401) {
        setTimeout(() => location.reload(), 2000);
      }
    }
  });
}

/**
 *
 */
function ajouterLigne() {
  const htmlErreur = document.getElementById('messageErreurUploadFichier');
  htmlErreur.innerHTML = "";

  const fileInput = document.getElementById('uploadFile');
  const files = fileInput.files;
  const typeSelect = document.querySelector('select[data-field="typeFichier"]');
  const selectedOption = typeSelect.options[typeSelect.selectedIndex];
  const type = selectedOption.value;
  if (!files.length || !type) {
    htmlErreur.innerHTML = "Veuillez ajouter un fichier ET sélectionner un type de fichier";
    return;
  }
  const file = files[0];
  const existeDeja = listeFichiers.some(item => item.file.name === file.name);
  if (existeDeja) {
    htmlErreur.innerHTML = "Vous avez déjà ajouté un fichier avec le même nom";
    return;
  }

  const ext = getExtension(file.name);

  // Vérification format
  const allowedExtensions = document.getElementById(
      "extensionsWhitelist").value;
  if (allowedExtensions && !allowedExtensions.includes(ext)) {
    htmlErreur.innerHTML = `Extension non autorisée : ${file.name}`;
    return;
  }

  // Vérification taille
  let maxTailleFichier = document.getElementById("maxTailleFichier").value;
  if (!maxTailleFichier) {
    maxTailleFichier = 5;
  }
  if (file.size > maxTailleFichier * 1024 * 1024) {
    htmlErreur.innerHTML = `Fichier trop volumineux : ${file.name} (${formatBytes(
        file.size)})`;
    return;
  }
  const visibilitePiece = document.getElementById("visibilitePiece").checked;
  // Ajouter à la liste
  const id = fileIdCounter++;
  listeFichiers.push({id, file, type, visibilitePiece});

  // Créer élément UI
  const fichiers = document.getElementById("fichiers-body");

  const row = document.createElement("tr");
  row.id = `file-${id}`

  row.innerHTML = `
      <td>${file.name}</td>
      <td>${selectedOption.text}</td>
      <td>
        <button class="btn btn-action ripple-effect" onclick="removeFile(${id})" title="Supprimer l'élément"><i class="icon-action_supprimer"></i></button>
      </td>
    `;
  fichiers.appendChild(row);

  // Reset champs
  fileInput.value = "";
  typeSelect.value = "";
  const enregistrer = document.getElementById('upload-button');
  if (enregistrer.disabled) {
    enregistrer.disabled = false;
  }
  const element = document.getElementById("contenuFichiers");
  if (element.style.display) {
    element.style.display = '';
  }
}

function formatBytes(bytes) {
  const kb = 1024;
  return (bytes / kb / kb).toFixed(2) + " Mo";
}

function getExtension(filename) {
  return filename.split('.').pop().toLowerCase();
}

function removeFile(idToRemove) {
  // Supprimer de la liste JS
  const index = listeFichiers.findIndex(f => f.id === idToRemove);
  if (index !== -1) {
    listeFichiers.splice(index, 1);
  }
  const row = document.getElementById(`file-${idToRemove}`);
  if (row) {
    row.remove();
  }
  if (listeFichiers.length === 0) {
    document.getElementById("contenuFichiers").style.display = "none";
    fileIdCounter = 0;
    document.getElementById('upload-button').disabled = true;
  }
}

function ouvrirModalSuppression(deleteButton, actionUrl, modal) {
  const id = deleteButton.parentNode.getAttribute('data');
  const modalElement = document.querySelector(modal);
  if (!modalElement) {
    return;
  }
  const btnConfirmer = modalElement.querySelector('#btn-confirmer-suppression');
  if (!btnConfirmer) {
    return;
  }
  btnConfirmer.setAttribute("data-id", id);
  btnConfirmer.setAttribute("data-url", actionUrl);
  $(modal).modal();
}

function supprimerLigne(deleteButton) {
  ouvrirModalSuppression(deleteButton,
      '/ws/file/suppression/', '#confirm-suppression-piece');
}

function supprimerLigneFichierDemandeInitiale(deleteButton) {
  ouvrirModalSuppression(deleteButton,
      '/ws/file/suppressionPJDemandeInitiale/',
      '#confirm-suppression-piece-init-info-comp');
}

function supprimerLigneFichierInfoComp(deleteButton) {
  ouvrirModalSuppression(deleteButton, '/ws/file/suppressionPJInfoComp/',
      '#confirm-suppression-piece-init-info-comp');
}

function updateVisibiliteFichier(updateButton) {
  const pkDemandeFile = updateButton.getAttribute('data-id');
  if (!pkDemandeFile) {
    $.notify({
      message: "Erreur technique : l’identifiant de la pièce est manquant ou indéfini"
    }, {
      type: 'danger'
    });
    return;
  }
  const header = $("meta[name='_csrf_header']").attr("content");
  const token = $("meta[name='_csrf']").attr("content");

  const pkDemande = document.querySelector('input[name="pkDemande"]').value;
  // Envoi
  $.ajax({
    url: APP.getContextPath() + "/ws/file/updateVisibilite/" + pkDemandeFile,
    method: "POST",
    data: {visibleUsager: updateButton.checked},
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function (response) {
      localStorage.setItem("uploadMessage", response);
      location.assign(APP.getContextPath() + "/demandes/" + pkDemande
          + "?demandeTab=fichiers");
    },
    error: function (xhr, status, error) {
      $('#confirm-suppression-piece').hide();
      const messageErreur = handleAjaxError(xhr, status, error);
      $.notify({
        message: messageErreur
      }, {
        type: 'danger'
      });
      if (xhr.status === 401) {
        setTimeout(() => location.reload(), 2000);
      }
    }
  });
}

function initSuppressionBouton() {
  const boutons = document.querySelectorAll('#btn-confirmer-suppression');
  if (!boutons.length) {
    return;
  }

  boutons.forEach(btn => {
    btn.addEventListener('click', function () {
      const pkDemandeFile = this.getAttribute('data-id');
      const url = this.getAttribute('data-url');

      if (!pkDemandeFile || !url) {
        $('.modal.show').modal('hide');
        $.notify(
            {message: "Erreur technique : l’identifiant de la pièce est manquant ou indéfini"},
            {type: 'danger'}
        );
        return;
      }

      const header = $("meta[name='_csrf_header']").attr("content");
      const token = $("meta[name='_csrf']").attr("content");
      const pkDemande = document.querySelector('input[name="pkDemande"]').value;

      this.classList.add('disabled', 'loading');

      $.ajax({
        url: APP.getContextPath() + url + pkDemandeFile,
        method: "POST",
        processData: false,
        contentType: false,
        beforeSend: xhr => xhr.setRequestHeader(header, token),
        success: response => {
          localStorage.setItem("uploadMessage", response);
          location.assign(
              APP.getContextPath() + "/demandes/" + pkDemande
              + "?demandeTab=fichiers"
          );
        },
        error: (xhr, status, error) => {
          $('.modal.show').modal('hide');
          const messageErreur = handleAjaxError(xhr, status, error);
          $.notify({message: messageErreur}, {type: 'danger'});

          if (xhr.status === 401) {
            setTimeout(() => location.reload(), 2000);
          }
        }
      });
    });
  });
}

initSuppressionBouton();

// Fonction dédiée pour gérer les erreurs
function handleAjaxError(xhr, status, error) {
  let messageErreur;

  switch (xhr.status) {
    case 401:
      messageErreur = "Session expirée. Veuillez vous reconnecter";
      break;
    case 403:
      messageErreur = "Accès non autorisé pour cette opération";
      break;
    case 413:
      messageErreur = "Le fichier est trop volumineux";
      break;
    default:
      if (status === 'timeout') {
        messageErreur = "Délai d'attente dépassé. Veuillez réessayer";
      } else if (xhr.responseText && !xhr.responseText.startsWith(
          "<!DOCTYPE html")) {
        messageErreur = xhr.responseText;
      } else {
        messageErreur = "Erreur technique rencontrée : Veuillez recharger la page et réessayer. Dans le cas où le problème persiste, veuillez contacter le support";
      }
  }
  return messageErreur;
}

/*]]>*/
