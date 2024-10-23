function exportEnCours(desactiverChamps, idBouton) {
  const bouton = $(idBouton);
  if (desactiverChamps) {
    bouton.addClass("loading");
  } else {
    bouton.removeClass("loading");
  }
  bouton.prop('disabled', desactiverChamps);
}

function setCookie(name, value, expiry) {
  const exdate = new Date();
  exdate.setTime(exdate.getTime() + expiry * 1000);
  const c_value = encodeURIComponent(value) + ((expiry == null) ? ""
      : "; expires=" + exdate.toUTCString());
  document.cookie = name + "=" + c_value + '; path=/';
}

function getCookie(name) {
  let i, x, y, ARRcookies = document.cookie.split(";");
  for (i = 0; i < ARRcookies.length; i++) {
    x = ARRcookies[i].substring(0, ARRcookies[i].indexOf("="));
    y = ARRcookies[i].substring(ARRcookies[i].indexOf("=") + 1);
    x = x.replace(/^\s+|\s+$/g, "");
    if (x === name) {
      return y ? decodeURIComponent(y.replace(/\+/g, ' ')) : y;
    }
  }
}

function checkDownloadCookie(idBouton) {
  if (getCookie("exportEnCours") === '0') {
    setCookie("exportEnCours", "0", 60 * 2);
    exportEnCours(false, idBouton);
  } else {
    // Lancer le check à nouveau chaque seconde
    setTimeout(() => {
      checkDownloadCookie(idBouton);
    }, 1000);
  }
}
