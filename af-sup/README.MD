Ce projet de générer les scripts de RUN (ansible, propeerties etc...) qui seront utilisés par Jenkins pendant le processus de déploiement d'un téléservice
Comment ça marche ?

0- Un dossier 'template' est fourni pour chaque environnement DEV, REC et PROD
Les exemples sont stockés dans "\src\main\resources\templates\${tscode}-dev" et "\src\main\resources\templates\${tscode}-rec"
L'idée est de partir de ces fichiers, pour générer un ensemble de fichiers de la même arboressence en remplaçant partout où nous avons ${var} par sa valeur qui sera définie dans un fichier "properties".
Les template sont différents selon les environnements DEV, REC et PROD. 

1- Pour faciliter le travail, un dossier "nom-de-l'environnement" est créé est contient le template de fichier pour cet environnement. Par exemple, pour le DEV, les fichiers sont stocké dans  
"src\main\resources\templates\dev", pour la REC, "src\main\resources\templates\rec" et pour la PROD, "src\main\resources\templates\prod"


2- Pour chaque téléservicen, il faut définir un certain nombre de variables pour compléter le fichier des templates. Il sont stocké dans "src\main\resources\vars\${tscode}\${env}.properties". 
Par exemple pour ${tscode}=derjfer, ${env}=dev, le fichier "src\main\resources\vars\derjfer\dev.properties" contiendra les varaibles à utiliser pour générer les scripts pour l'environnement de DEV.

3- Commande à lancer :   mvn clean package -DskipTests -Dtscode=${tscode} -Pgenerate-${env} -Dvars-path='${Le chemin où se trouve le fochier properties contenant les config du téléservice}"

Par exemple, si on veut générer les scripts de DEV pour le téléservice "derjfer", il faut lancer la commande suivante :
 
mvn clean package -DskipTests -Dtscode=derjfer -Pgenerate-dev -Dvars-path='C:\Users\adiallo.ext\Documents\docs\ts\DERJFER\run-vars\dev.properties'
 
Pour la REC  mvn package -Dtscode=derjfer -Pgenerate-rec -Dvars-path='C:\Users\adiallo.ext\Documents\docs\ts\DERJFER\run-vars\rec.properties' et 
pour la PROD   mvn clean package -DskipTests -Dtscode=derjfer -Pgenerate-prod -Dvars-path='C:\Users\adiallo.ext\Documents\docs\ts\DERJFER\run-vars\prod.properties'
 
 Les 3 paramètres sont obligatoires 
 
 4- Si cette commande s'éxécute avec succès, nous aurons à la fin un dossier "target\generated-run\${tscode}-${env}" comme par exemple "target\generated-run\derjfer-dev". 
 Avec dedans, l'ensembles des fichiers ".yml", "properties" etc... contenant les bonnes valeurs selon l'environnement.