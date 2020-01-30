Génération fichiers .yml et .properties pour le déploiement des téléservices
-

Ce projet de générer les scripts de RUN (ansible, properties etc...) qui seront utilises par Jenkins pendant le processus de deploiement d'un teleservice

### Comment ça marche ? #

1. Un dossier `templates` est fourni pour chaque environnement DEV, REC et PROD. Par environnement, les templates sont différents. En effet, nous pouvons avoir besoin de certaines informations en PROD mais pas en REC et en DEV. Par exemple, le monitoring n'est pas géré par les mêmes serveurs zabbix en DEV, REC et PROD. 
L'idée est de partir de ces fichiers, pour générer un ensemble de fichiers de la même arborescence en remplaçant partout ou nous avons ${variable} par sa valeur qui sera définie dans un fichier `properties`.
Le fichier `properties` sera donne en argument à la commande `maven` qui lance le processus de génération.


2. Un dossier "nom-de-l'environnement" est créé est contient le template de fichier pour cet environnement.
* DEV : `src/main/resources/templates/${codePlateforme}/dev`, 
* REC : `src/main/resources/templates/${codePlateforme}/rec`
* PROD : `src/main/resources/templates/${codePlateforme}/prod`


3. Pour chaque téléservice, il faut définir un certain nombre de variables pour compléter le fichier des templates. Il sont stocke dans `src/main/resources/template/${codePlateforme}/vars/samples/${codeEnv}.properties`.

4. Commande à lancer : 
>mvn clean package -DskipTests -Dtscode=-${tscode} -P${codePlateforme}-${codeEnv} -Dvars-path="${Le chemin se trouve le fichier properties contenant les config du téléservice}

Par exemple, si on veut générer les scripts de DEV pour le téléservice "derjfer", il faut lancer la commande suivante :
>mvn clean package -DskipTests -Dtscode=derjfer -Pgents-dev -Dvars-path='C:/Users/adiallo.ext/Documents/docs/ts/DERJFER/run-vars/dev.properties'

Pour la REC 
>mvn package -Dtscode=derjfer -Pgents-rec -Dvars-path='C:/Users/adiallo.ext/Documents/docs/ts/DERJFER/run-vars/rec.properties' 

et pour la PROD 
>mvn clean package -DskipTests -Dtscode=derjfer -Pgents-prod -Dvars-path='C:/Users/adiallo.ext/Documents/docs/ts/DERJFER/run-vars/prod.properties

Template des fichiers properties :

* DEV : src/main/resources/templates/${codePlateforme}/vars/samples/dev.properties
* REC : src/main/resources/templates/${codePlateforme}/vars/samples/rec.properties
* PROD : src/main/resources/templates/${codePlateforme}/vars/samples/prod.properties
 
 Les 3 parametres sont obligatoires 
 
5. Si cette commande s’exécute avec succès, un dossier sera créé dans `target/generated-run/${numEnv}-${codeEnv}/${tscode}` contenant l'ensemble des fichiers classés dans les répertoires attendus par les scripts Ansible appelés par Jenkins lors du déploiement des composants.

### Annexes #

${tscode} : exemple, dertrav

${codePlateforme} : `gents` (plateforme dédiée), `gentsa` (plateforme mutualisée en HA)

|   Variables      | DEV           |     REC      |   PROD     |
|------------------|---------------|--------------|------------|
|${codeEnv}        | dev           |rec           | prod       |
|${numEnv}         | 1             |3             | 0          |

