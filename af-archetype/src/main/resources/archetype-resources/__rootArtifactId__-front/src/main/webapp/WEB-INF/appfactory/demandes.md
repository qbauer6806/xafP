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
    },
    {
      "name": "chaine",
      "url": "chaine",
      "meta": "chaine"
    }
  ],
  "contenu": {
    "usager": {
      "titre": "mapping titre: 0|1|2",
      "nom": "chaine",
      "nomjeunefille": "chaine",
      "prenom": "chaine",
      "raisonsociale": "chaine",
      "mail": "chaine",
      "telephone": "chaine",
      "adresse": {
        "ligne1": "chaine",
        "ligne2": "chaine",
        "ligne3": "chaine",
        "codePostal": "chaine",
        "ville": "chaine",
        "pays": "mapping pays"
      }
    },
    "donnee": {
      "vehiculetypeusager": "mapping typeUsager: PARTICULIER|ENTREPRISE",
      "vehiculetypetous": "mapping vehiculeTypetous: CAT1|CAT2|CAT3",
      "vehicule": {
        "taxi": "mapping ouinon: YES|NO",
        "emissionvoiture": "mapping emissionVoiture: EMI1|EMI2|EMI3|EMI4",
        "emissiondeuxroues": "mapping emission2Roues: EMI3|EMI4",
        "emissionvelo": "mapping emissionVelo: EMI4",
        "nomproprietaire": "chaine",
        "prenomproprietaire": "chaine",
        "locationbatterie": "mapping ouinon: YES|NO"
      },
      "declaration": {
        "recap": "mapping declarationRecap: TEXTE"
      },
      "simulation": {
        "reglecalculmoins20quatresroues": "chaine",
        "reglecalculmoins20deuxroues": "chaine",
        "reglecalculmoins20velo": "chaine",
        "montantaide4roues61": "chaine",
        "montantaide4roues51": "chaine",
        "montantaide4roues21": "chaine",
        "montantaide2roues21": "chaine",
        "remises": "chaine",
        "primetaxi": "chaine"
      },
      "prixbase": "chaine",
      "locationbatterie": "chaine",
      "simulationtva": "chaine",
      "simulationprixtotalvehicule": "chaine",
      "simulationprixapplication30": "chaine",
      "bancaire": {
        "nometsbancaire": "chaine",
        "rib": {
          "titulaire": "chaine",
          "bic": "chaine",
          "iban": "chaine"
        }
      }
    },
    "vehicule": {
      "nombrekm": "chaine",
      "datefacture": "date",
      "numeroimmat": "chaine",
      "marque": "chaine",
      "genre": "chaine",
      "datemiseencirculation": "date",
      "type": "mapping vehiculeTypetous: CAT1|CAT2|CAT3",
      "typesansimmat": "mapping vehiculeTypesansimmat: TRICYLE|VELO_ELECTRIQUE_AVEC_MOTEUR_SUPERIEUR_A_250_W|VELO_ELECTRIQUE_AVEC_MOTEUR_INFERIEUR_A_250_WATT"
    },
    "declaration": {
      "acceptation": "mapping declarationTexte: TEXTE"
    },
    "simulation": {
      "montant": "chaine"
    }
  }
}
```
