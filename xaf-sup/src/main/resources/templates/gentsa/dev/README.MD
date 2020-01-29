# Template-tscode

Ce projet est un template permettant d'instancier plus rapidement les properties d'un téléservices issue de GENTS sur l'environnement de DEV

## Utilisation

Remplacer dans les noms des fichiers et leur contenu les variables ci-après. Copier les fichiers résultants dans l'inventory gents.

Varaibles à remplacer:
- tscode : *code du téléservice en lowercase*
- TSCODE : *code du téléservice en uppercase*
- tsport : *Deux premiers chiffre du port du téléservice (ex. 79 pour 790)*
- tscontexte : *contexte du téléservice sans le slash (ex. embauche-personnel-maison)*
- TSPWKB : *Code du tracking piwik back / Code suivant à supprimer car ce sont ceux de DERJFER*
- TSPWKF : *Code du tracking piwik front / Code suivant à supprimer car ce sont ceux de DERJFER*
- TSFILEJWT : *Token JWT File*
- TSMAILJWT : *Token JWT Mail* 
- TSAPISECRET : *Secret pour les Token JWT Api*
- TSAPIJWT : *Token JWT Api pour le front*
- TSPGPWD : *Password postgres*
- TSFRONTKEY : *Clef partagée entre le front et le back (35 cars.)*
- TSAPPLZABBIX : *nom du serveur appl dans zabbix*
- TSFRNTZABBIX : *nom du serveur frnt dans zabbix*
- TGFAPIURL : *URL du service de vérification des IBAN pour l'accès à TGF*
- TGFAPIJWTKEY : *Token JWT pour l'accès à TGF*
- xms : *valeur xms par defaut pour BACK et API*
- xmx : *valeur xmx par defaut pour BACK et API*
- xmsfrnt : *valeur 'xms' par defaut pour le FRONT*
- xmxfrnt : *valeur 'xmx' par defaut pour le FRONT*
- URLFICHEDEMFR : *URL Lien vers la fiche demarche (FR)*
- URLSONDAGEFR : *URL Lien sondage (FR)*
- URLABDSONDAGEFR : *URL Lien sondage (FR)*
- URLJODEMFR : *URL lien Journal Officiel pour la demarche (FR)*
- URLJOCCINFR : *URL lien Journal Officiel CCIN (FR)*
- URLFICHEDEMEN : *URL Lien vers la fiche demarche (EN)*
- URLSONDAGEEN : *URL Lien sondage (EN)*
- URLABDSONDAGEEN : URL Lien sondage (EN)*
- URLJODEMEN : *URL lien Journal Officiel pour la demarche (EN)*
- URLJOCCINEN : *URL lien Journal Officiel CCIN (EN)*
- BACKSRVFRNTFORMSTPAGE : *URL redirection après création de demande coté BO. Valeurs possible etape_usager.html (pour les TS destinés aux particuliers), etape_entreprise.html (pour les TS destinés aux entreprise)*
- CGUURLFR : *URL CGU en Français*
- CGUURLEN : *URL CGU en Anglais*
