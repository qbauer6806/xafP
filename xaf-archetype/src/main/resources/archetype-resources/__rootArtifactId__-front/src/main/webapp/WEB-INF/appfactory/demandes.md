# Structure des demandes de la démarche
## Structure de projectDemande
```
{
  "fichiers": [
    {
      "name": "chaine",
      "url": "chaine",
      "meta": "chaine"
    },
    {
      "name": "chaine",
      "url": "chaine",
      "meta": "chaine"
    },
    {
      "name": "chaine",
      "url": "chaine",
      "meta": "chaine"
    },
    {
      "name": "chaine",
      "url": "chaine",
      "meta": "chaine"
    },
    {
      "name": "chaine",
      "url": "chaine",
      "meta": "chaine"
    }
  ],
  "contenu": {
    "donnee": {
      "entreprise": {
        "numerocar": "chaine",
        "raisonsociale": "chaine",
        "nom": "chaine",
        "adresse": {
          "ligne1": "chaine",
          "ligne2": "chaine",
          "ligne3": "chaine",
          "codePostal": "chaine",
          "ville": "chaine",
          "pays": "mapping pays"
        },
        "telephone": "chaine"
      },
      "demandeur": {
        "titre": "mapping titre: 0|1|2",
        "prenom": "chaine",
        "nom": "chaine",
        "mail": "chaine"
      },
      "derogation": {
        "joursferies": {
          "jourDeL_An": "booléen: true|false",
          "sainteDevote": "booléen: true|false",
          "lundiDePaques": "booléen: true|false",
          "le1erMai": "booléen: true|false",
          "ascension": "booléen: true|false",
          "lundiDePentecote": "booléen: true|false",
          "feteDieu": "booléen: true|false",
          "assomption": "booléen: true|false",
          "toussaint": "booléen: true|false",
          "feteDuPrince": "booléen: true|false",
          "immaculeeConception": "booléen: true|false",
          "noel": "booléen: true|false"
        },
        "employe": {
          "concerne": "texte"
        },
        "motivationdemande": "texte",
        "presencedeleguespersonnel": "mapping ouinon: YES|NO",
        "nombresalarie": "chaine"
      }
    }
  }
}
```
