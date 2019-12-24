# Template-tscode

Ce projet est un template permettant d'instancier plus rapidement les properties d'un téléservices issue de GENTS sur l'environnement de REC

## Utilisation

Remplacer dans les noms des fichiers et leur contenu les variables ci-après. Copier les fichiers résultants dans l'inventory gents.

Varaibles à remplacer:
- tscode ${tscode} : code du téléservice en lowercase
- TSCODE ${TSCODE}: code du téléservice en uppercase
- TSPORT ${TSPORT}: Deux premiers chiffre du port du téléservice (ex. 79 pour 790)
- TSCONTEXTE ${TSCONTEXTE}: Contexte du téléservice sans le slash (ex. subvention-vehicule-ecologique)
- TSPWKB ${TSPWKB}: Code du tracking piwik back
- TSPWKF ${TSPWKF}: Code du tracking piwik front
- TSFILEJWT ${TSFILEJWT}: Token JWT File
- TSMAILJWT ${TSMAILJWT}: Token JWT Mail
- TSAPISECRET ${TSAPISECRET}: Secret pour les Token JWT Api
- TSAPIJWT ${TSAPIJWT}: Token JWT Api pour le front
- TSPGPWD ${TSPGPWD}: Password postgres
- TSFRONTKEY ${TSFRONTKEY}: Clef partagé entre le front et le back
- TSAPPLZABBIX ${TSAPPLZABBIX}: nom du serveur appl dans zabbix
- TSFRNTZABBIX ${TSFRNTZABBIX}: nom du serveur frnt dans zabbix