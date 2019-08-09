mcRuntime.factory('mcProjectDebutEtapeSimulation', [
  '$q',
  'mcRuntimeRoute',
  'mcCharteTshiDebutEtape',
  'mcCharteTshiPageRecap',
  function($q, mcRuntimeRoute, mcCharteTshiDebutEtape, mcCharteTshiPageRecap) {
    function goToError(scope, deferred, errorPage, errorCode) {
      if (!errorPage) {
        errorPage = 'erreur';
      }
      if (!errorCode) {
        errorCode = 1;
      }
      deferred.resolve({ errorCode: errorCode });
      mcRuntimeRoute(scope, { type: 'internal', page: errorPage });
      return deferred.promise;
    }
    var fractionRegexp = /^(-?)([0-9]+)(,[0-9]{1,2})?$/;
    function toFraction(userInput) {
      var result = { base: 0, frac: 0, neg: false };
      var res = fractionRegexp.exec(userInput);
      if (!res || res[0] != userInput || typeof res[2] != 'string') {
        return result;
      }
      if (res[1] === '-') {
        result.neg = true;
      }
      result.base = parseInt(res[2], 10);
      if (
        !angular.isNumber(result.base) ||
        (isFinite && !isFinite(result.base))
      ) {
        result.base = 0;
        return result;
      }
      if (typeof res[3] != 'string') {
        return result;
      }
      res[3] = res[3].substring(1);
      // dans le cas un seul chiffre après la virgule on doit rajouter le chiffre
      result.frac = parseInt(res[3].length === 2 ? res[3] : res[3] + '0', 10);
      if (
        !angular.isNumber(result.frac) ||
        (isFinite && !isFinite(result.frac))
      ) {
        result.frac = 0;
      }
      return result;
    }
    // on met une fraction sur un entier du coup (<=> *100)
    function frToInteger(fr) {
      var result = toFraction(
        (fr.neg ? '-' : '') + fr.base + (fr.frac < 10 ? '0' + fr.frac : fr.frac)
      );
      return result.neg ? -result.base : result.base;
    }
    // on remet un entier qui avait été multiplié par 100 en fraction (<=> /100)
    function frIntegerToFrac(base) {
      var strRes = base + '';
      if (strRes.startsWith('-')) {
        strRes = strRes.substring(1);
      }
      var len = strRes.length;
      return toFraction(
        (base < 0 ? '-' : '') +
          strRes.substring(0, len - 2) +
          ',' +
          strRes.substring(len - 2)
      );
    }
    function frAdd(fr1, fr2) {
      return frIntegerToFrac(frToInteger(fr1) + frToInteger(fr2));
    }
    function frSub(fr1, fr2) {
      return frIntegerToFrac(frToInteger(fr1) - frToInteger(fr2));
    }
    function frPercent(fr, percent) {
      var fr2 = frIntegerToFrac(frToInteger(fr) * percent);
      return frIntegerToFrac(fr2.neg ? -fr2.base : fr2.base);
    }
    function frMin(fr1, fr2) {
      return frIntegerToFrac(Math.min(frToInteger(fr1), frToInteger(fr2)));
    }
    function frToString(fr) {
      var res = (fr.neg ? '-' : '') + fr.base;
      if (fr.frac != 0) {
        res += ',' + (fr.frac < 10 ? '0' + fr.frac : fr.frac);
      }
      return res;
    }
    return function mcProjectDebutEtapeSimulation(scope, parameter) {
      var deferred = $q.defer();
      mcCharteTshiDebutEtape(scope, parameter)
        .then(function(result) {
          if (result.errorCode != 0) {
            deferred.resolve(result);
            return deferred.promise;
          }
          var res = toFraction('0');
          var primeTaxi = toFraction('3000');
          var prixBase = toFraction(scope.page.demande.contenu.donnee.prixbase);
          var remises = toFraction(
            scope.page.demande.contenu.donnee.simulation.remises
          );
          var batteries = toFraction(
            scope.page.demande.contenu.donnee.locationbatterie
          );
          var tva = toFraction(scope.page.demande.contenu.donnee.simulationtva);
          var max = 0;
          var percent = 0;
          var cat = scope.page.demande.contenu.donnee.vehiculetypetous;
          var emiVoit =
            scope.page.demande.contenu.donnee.vehicule.emissionvoiture;
          var emi2Roues =
            scope.page.demande.contenu.donnee.vehicule.emissiondeuxroues;
          var emiVelos =
            scope.page.demande.contenu.donnee.vehicule.emissionvelo;

          if (cat === 'CAT1') {
            if (emiVoit === 'EMI4') {
              max = 10000;
              percent = 30;
            } else if (emiVoit === 'EMI3') {
              res = toFraction('6000');
            } else if (emiVoit === 'EMI2') {
              res = toFraction('5000');
            } else if (emiVoit === 'EMI1') {
              res = toFraction('3000');
            } else {
              return goToError(scope, deferred, 'erreur', 12);
            }
          } else if (cat === 'CAT2') {
            if (emi2Roues === 'EMI4') {
              max = 3000;
              percent = 30;
            } else if (emi2Roues === 'EMI3') {
              res = toFraction('800');
            } else {
              return goToError(scope, deferred, 'erreur', 12);
            }
          } else if (cat === 'CAT3') {
            if (emiVelos === 'EMI4') {
              max = 400;
              percent = 30;
            } else {
              return goToError(scope, deferred, 'erreur', 12);
            }
          } else {
            return goToError(scope, deferred, 'erreur', 12);
          }

          if (max) {
            if (
              scope.page.demande.contenu.donnee.vehicule.locationbatterie !=
              'YES'
            ) {
              batteries = toFraction('0');
            }
            var prixTotal = frAdd(
              frAdd(frSub(prixBase, remises), batteries),
              tva
            );
            scope.page.demande.contenu.donnee.simulationprixtotalvehicule = frToString(
              prixTotal
            );
            res = frPercent(prixTotal, percent);
            scope.page.demande.contenu.donnee.simulationprixapplication30 = frToString(
              res
            );
            res = frMin(toFraction(max), res);
          }
          if (scope.page.demande.contenu.donnee.vehicule.taxi === 'YES') {
            res = frAdd(res, primeTaxi);
            scope.page.demande.contenu.donnee.simulation.primetaxi = frToString(
              primeTaxi
            );
          }
          if (res.neg) {
            res.base = 0;
            res.frac = 0;
            res.neg = false;
          }
          scope.page.demande.contenu.simulation.montant = frToString(res);
          // on initialise à nouveau les éventuels recaps qui ont été demandés par des
          // composants de la page
          var initRes = mcCharteTshiPageRecap.initRecaps(
            scope,
            scope.page.demande
          );
          if (initRes != 0) {
            return goToError(scope, deferred, 'erreur', 11);
          }
          deferred.resolve({ errorCode: 0 });
        })
        .catch(function() {
          return goToError(scope, deferred, 'erreur', 6);
        });
      return deferred.promise;
    };
  }
]);
