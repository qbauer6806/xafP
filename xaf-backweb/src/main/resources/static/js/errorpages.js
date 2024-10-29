function copyErrorInfo() {
  navigator.clipboard.writeText(
      $("#errinfo").text().replace(/\t/g, '').replace(/\n\n\n\n\n\n\n\n\n\n\n/g,
          '').replace(/\n\n\n/g, "\n"));
}

function copyStackTrace() {
  navigator.clipboard.writeText($("#stacktrace").text());
}

navigator.sayswho = (function () {
  var ua = navigator.userAgent;
  var tem;
  var M = ua.match(
      /(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
  if (/trident/i.test(M[1])) {
    tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
    return 'IE ' + (tem[1] || '');
  }
  if (M[1] === 'Chrome') {
    tem = ua.match(/\b(OPR|Edge)\/(\d+)/);
    if (tem != null) {
      return tem.slice(1).join(' ').replace('OPR', 'Opera');
    }
  }
  M = M[2] ? [M[1], M[2]] : [navigator.appName, navigator.appVersion, '-?'];
  if ((tem = ua.match(/version\/(\d+)/i)) != null) {
    M.splice(1, 1, tem[1]);
  }
  return M.join(' ');
})();

$(document).ready(function () {
  $("#url").text(window.location.href);
  $("#browser").text(navigator.sayswho);
});

const errorStep = [200, 200, 300, 200, 200, 500];

let timestamp = [];
let timeoutId;

function detectError() {
  if (timestamp.length < errorStep.length + 1) {
    return;
  }

  let intervals = [];
  for (let i = 1; i < timestamp.length; i++) {
    intervals.push(timestamp[i] - timestamp[i - 1]);
  }

  let matched = errorStep.every((time, index) => {
    const interval = intervals[index];
    return Math.abs(interval - time) < 150;
  });

  if (matched) {
    $("#wlib-error").css("display", "block");
  }
  timestamp = [];
}

function resetError() {
  clearTimeout(timeoutId);
  timeoutId = setTimeout(() => {
    timestamp = [];
  }, 2000);
}

document.addEventListener("click", () => {
  timestamp.push(Date.now());
  detectError();
  resetError();
});
