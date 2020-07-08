Ce projet de generer les scripts de RUN (ansible, propeerties etc...) qui seront utilises par Jenkins pendant le processus de deploiement d'un teleservice
Comment ça marche ?

0. Un dossier 'template' est fourni pour chaque environnement DEV, REC et PROD
Les exemples sont stockes dans <b>"\src\main\resources\templates\${tscode}-dev"</b> et <b>"\src\main\resources\templates\${tscode}-rec"</b>
L'idee est de partir de ces fichiers, pour generer un ensemble de fichiers de la meme arboressence en remplaçant partout ou nous avons ${var} par sa valeur qui sera definie dans un fichier "properties".

Les template sont differents selon les environnements DEV, REC et PROD. 

1. Pour faciliter le travail, un dossier "nom-de-l'environnement" est cree est contient le template de fichier pour cet environnement. Par exemple, pour le DEV, les fichiers sont stocke dans  
"src\main\resources\templates\dev", pour la REC, "src\main\resources\templates\rec" et pour la PROD, "src\main\resources\templates\prod"


2. Pour chaque teleservice, il faut definir un certain nombre de variables pour completer le fichier des templates. Il sont stocke dans "src\main\resources\vars\${tscode}\${env}.properties". 
Par exemple pour ${tscode}=derjfer, ${env}=dev, le fichier "src\main\resources\vars\derjfer\dev.properties" contiendra les varaibles a  utiliser pour generer les scripts pour l'environnement de DEV.

3. Commande a  lancer : 
<pre><code>mvn clean package -DskipTests -Dtscode=-${tscode} -Pgenerate-${env} -Dvars-path="${Le chemin se trouve le fochier properties contenant les config du teleservice}"</code></pre>

Par exemple, si on veut generer les scripts de DEV pour le teleservice "derjfer", il faut lancer la commande suivante :
<pre><code>mvn clean package -DskipTests -Dtscode=derjfer -Pgenerate-dev -Dvars-path='C:\Users\adiallo.ext\Documents\docs\ts\DERJFER\run-vars\dev.properties'</code></pre>

Pour la REC <pre><code>mvn package -Dtscode=derjfer -Pgenerate-rec -Dvars-path='C:\Users\adiallo.ext\Documents\docs\ts\DERJFER\run-vars\rec.properties'</code></pre> et 
pour la PROD <pre><code>mvn clean package -DskipTests -Dtscode=derjfer -Pgenerate-prod -Dvars-path='C:\Users\adiallo.ext\Documents\docs\ts\DERJFER\run-vars\prod.properties'</code></pre>

Template des fichiers properties :

DEV : <pre>src\main\resources\vars\samples\derjfer\dev.properties</pre>
REC : <pre>src\main\resources\vars\samples\derjfer\rec.properties</pre>
 
 Les 3 parametres sont obligatoires 
 
4. - Si cette commande s'execute avec succe, nous aurons a  la fin un dossier "target\generated-run\${tscode}-${env}" comme par exemple "target\generated-run\derjfer-dev". 
 Avec dedans, l'ensembles des fichiers ".yml", "properties" etc... contenant les bonnes valeurs selon l'environnement.