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
        "paysoriginedetachement": "mapping paysOrigineDetachement: FR|IT",
        "datesdetachement": "texte",
        "estchantier": "mapping ouinon: YES|NO",
        "nomchantier": "chaine",
        "nomentreprise": "chaine",
        "nautorisationchantier": "chaine",
        "datedebutchantier": "date",
        "datefinchantier": "date",
        "adressechantier": {
          "ligne1": "chaine",
          "ligne2": "chaine",
          "ligne3": "chaine",
          "codePostal": "chaine",
          "ville": "chaine",
          "pays": "mapping pays"
        },
        "adresseentreprise": {
          "ligne1": "chaine",
          "ligne2": "chaine",
          "ligne3": "chaine",
          "codePostal": "chaine",
          "ville": "chaine",
          "pays": "mapping pays"
        }
      },
      "salaries": {
        "tabsalariesitalie": [
          {
            "donneeSalariesTabsalariesitalieChampnom": "chaine",
            "donneeSalariesTabsalariesitalieChampprenom": "chaine",
            "donneeSalariesTabsalariesitalieChampqualif": "chaine",
            "donneeSalariesTabsalariesitalieChampdebutdetach": "texte"
          },
          "..."
        ],
        "tabsalariesfrance": [
          {
            "donneeSalariesTabsalariesfranceChampnom": "chaine",
            "donneeSalariesTabsalariesfranceChampprenom": "chaine",
            "donneeSalariesTabsalariesfranceChampnumsecu": "chaine",
            "donneeSalariesTabsalariesfranceChampqualif": "chaine",
            "donneeSalariesTabsalariesfranceChampdebutdetach": "texte"
          },
          "..."
        ],
        "joindrefichier": "mapping saisirOuFichier: SAISIR|JOINDRE"
      },
      "demandeur": {
        "titre": "mapping titre: 0|1|2",
        "nom": "chaine",
        "prenom": "chaine",
        "email": "chaine"
      },
      "entrepriseorigine": {
        "raisonsociale": "chaine",
        "adresse": {
          "ligne1": "chaine",
          "ligne2": "chaine",
          "ligne3": "chaine",
          "codePostal": "chaine",
          "ville": "chaine",
          "pays": "mapping pays"
        }
      }
    },
    "champvide": "chaine"
  }
}
```
