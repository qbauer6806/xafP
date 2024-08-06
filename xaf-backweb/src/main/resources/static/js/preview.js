var makePreviewCall = function (form, successCallback) {
  var token = $("meta[name='_csrf']").attr("content");
  var header = $("meta[name='_csrf_header']").attr("content");

  var body = {
    action: form.statutChoisi,
    codeMotifChoisi: form.motifChoisiCode,
    pkDemande: form.pkDemande,
    commentaire: form.commentaire,
    texteAEnvoyer: form.texteAEnvoyer
  };

  $.ajax({
    url: APP.getContextPath() + '/ws/mailpreview',
    type: 'post',
    contentType: 'application/json',
    data: JSON.stringify(body),
    dataType: "html",
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function (data) {
      successCallback(data, form.message)
    }
  });
};

var makePdfPreviewCall = function (form, pdfType, successCallback) {
  var token = $("meta[name='_csrf']").attr("content");
  var header = $("meta[name='_csrf_header']").attr("content");

  var body = {
    action: form.statutChoisi,
    codeMotifChoisi: form.motifChoisiCode,
    pkDemande: form.pkDemande,
    commentaire: form.commentaire,
    texteAEnvoyer: form.texteAEnvoyer,
    pdfType: pdfType
  };

  $.ajax({
    url: APP.getContextPath() + '/ws/pdf/apercu',
    type: 'post',
    contentType: 'application/json',
    data: JSON.stringify(body),
    xhrFields: {
      responseType: 'arraybuffer'
    },
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    success: function (data) {
      successCallback(data, form, pdfType)
    }
  });
};

var generateURI = function (form, pdfType) {
  return APP.getContextPath() + '/ws/pdf/apercu?pkDemande=' + form.pkDemande
      + '&statut=' + form.statutChoisi + '&texteAEnvoyer='
      + encodeURIComponent(form.texteAEnvoyer) + '&commentaire='
      + encodeURIComponent(form.commentaire) + '&codeMotif='
      + form.motifChoisiCode + '&pdfType=' + pdfType;
};

var openMailModal = function (containerId, canvasId, contentId, data) {
  $(contentId).html(data);
  $(containerId).modal();
  $(containerId).on('hidden.bs.modal', onClosePreview);
  $(canvasId).remove();
  $(".valider-preview").prop('disabled', false);
};

function createPdfCanvas(containerId, currPage, numPages, thePDF, page) {
  var scale = 2;
  var viewport = page.getViewport({scale: scale});

  // On va créer un canvas pour chaque page
  var canvas = document.createElement("canvas");
  canvas.style.display = "block";
  canvas.className = "pdf-canvas";
  var context = canvas.getContext('2d');
  canvas.height = viewport.height;
  canvas.width = viewport.width;

  // Création du canvas
  page.render({canvasContext: context, viewport: viewport});

  // Ajout au container
  document.getElementById(containerId).appendChild(canvas);

  // Passage à la prochaine page
  currPage++;
  if (thePDF !== null && currPage <= numPages) {
    return thePDF.getPage(currPage).then(
        createPdfCanvas.bind(null, containerId, currPage, numPages, thePDF));
  }
}
