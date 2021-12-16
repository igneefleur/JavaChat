# Chat
### L3 RESEAUX - mini projet de groupe

## Rendu final :

Nous avons développé un Chat TCP en java où tous les utilsateurs peuvent communiquer entre eux publiquement de manière sécurisée.<br/>
Pour se connecter, l'utilisateur doit rentrer l'adresse IP et le Port du serveur. Le port du serveur est de base ***55555***.<br/>
Chaque Utilisateur à la possibilité d'envoyer un message publique (à tous les autres utilsateurs), un message privé (à un autre utilisateur) ou d'envoyer une commande pour modifier son status.

Toutes les commandes commençent par le caractère "/". Elles sont :
> ***"/help"*** -> affiche une aide pour toutes les commandes.<br/>
> ***"/rename [NAME]"*** -> modifie le pseudonyme de l'utilsateur par [NAME].<br/>
> ***"/recolor [RED] [GREEN] [BLUE]"*** -> modifie la couleur de l'utilsateur par celle donnée en RGB.<br/>
> ***"/private [NAME] [TEXT]"*** -> envoie un message privé à l'utilisateur [NAME] avec comme contenu [TEXT].

## Réponses aux questions :

***1) Créez un client et un serveur capables de s’échanger des chaînes de caractères. Vous utiliserez pour cela les sockets TCP.***

> Nous avons utilisé les classes Socket et ServerSocket pour créer le Client et le Serveur.<br/>
Deux threads sont utilisés pour la reception et l'envoi de message.

> Sources utilisés :
>> PDF fourni <br/>
>> http://www.codeurjava.com/2014/11/java-socket-clientserveur-mini-chat.html

***2) Modifiez votre application de manière à ce que la réception de la chaîne ‘’bye’’ mette fin à la connexion.***

> Lorsqu'un Client envoie uniquement la chaine "bye", un signal est envoyé au serveur lui disant qu'un Client est parti (afin de le supprimer de la listes des clients) et alors le programme du Client se ferme.<br/>
Lorsqu'un Serveur envoie uniquement la chaine "bye", le serveur se ferme, et tous les Clients qui sont connecté au serveur se ferment aussi.

***3) Créez un module capable de générer une clé AES, de l’exporter dans un fichier et de charger une clé existante depuis un fichier.***

> La clé AES se génère pour le Client et le Serveur à partir d'un nombre secret est et directement utilisé pour crypter et décrypter des messages. Le nombre secret changeant pour chaque Client, charger une clé existante ou l'exporter est inutile.

***4) Intégrez le code responsable du chargement de la clé depuis un fichier au processus de démarrage du client et du serveur.***

> Voir réponse aux questions ***3*** et ***6.1***.

***5) Utilisez cette clé partagée pour chiffrer les messages envoyés et les déchiffrer à la réception. Affichez, pour chaque message, la version chiffrée et la version en clair.***

> Pour obtenir la version chiffrée des messages, il faut appuyer sur le button "switch", alors tous les messages du Client seront modifiés pour afficher la version chiffrée.<br/>
Le Serveur n'a pas cette fonctionnalité car il possède une clé différente pour chaque client, et afficher les messages chiffrés en fonction de chaque clé serait illisible (déjà qu'un message chiffré est difficile à lire...).

***6) Comment peut-on améliorer la sécurité des échanges ?***

> ***1. Implémentez et commentez votre solution. Vous pouvez utiliser d’autres services de java. (classes ...)***

>> TODO : EXPLICATION DIFFIE HELLMAN

> ***2. Commentez votre code et justifiez votre choix (numéro de port, algorithmes, types de cryptographie ...)***

>> TODO : EXPLIQUER QU'ON A BIEN COMMENTE, PLUS EXPLIQUER PORTS

> ***3. Votre code doit pouvoir s’exécuter sur n’importe quelle machine sans demander de créer une arborescence de fichiers particulière ou des IP spécifiques.***

>> Pour éxécuter respectivement le Server et le Client via commande sh :<br/>
***```java -jar exports/server.jar```***<br/>
***```java -jar exports/client.jar```***<br/><br/>
>> Pour lancer les programmes sur Windows, cliquez sur les éxécutables dans le répertoire ***`exports`***.

> ***4. Votre rendu consistera en votre code source et un rapport décrivant ou vous en êtes rendu, les améliorations que vous avez apporté, les ports choisis, etc ...***

>> TODO CE QU'ON A FAIT EN PLUS

***7) Si possible faire du multicast : un serveur et au moins deux clients...***

> Nous utilisons une liste de Clients (HashMap pour être précis) afin de conserver les sockets TCP. Lorsqu'un Client envoie un message au Serveur, celui ci le renvoie à tous les clients.

***8) développer une interface graphique***

> Le Client possède une interface graphique où il peut voir tous les messages depuis qu'il est connecté, et peut écrire des messages aux autres Clients dans une zone de texte en bas de la fenêtre et envoyer ces messages en appuyant sur la touche "Entrée".<br/>
> Le Serveur ne possède pas d'interface graphique, il peut donc être lancé facilement sur un hébergeur.

===
===
===




** 1) **  
En utilisant la commande :  
$ ip -br -c addr  
On peut obtenir l'adresse IP du serveur.

La plage de ports 49152-65535 est réservé à l'usage privé.
Nous avons donc choisis arbitrairement de prendre 55555 (5 cinq) comme port de discussion pour notre application Chat.

En utilisant les ressources du cours et un tutoriel en ligne, on configure sans soucis (sauf problème concernant le réseaux universitaire) un premier chat.

** 2) **  
Après discussion, on a choisi que l'envoi de la chaîne *"bye"* servira à la deconnexion du client côté Serveur et à la fermeture de l'application côté Client.


Les questions 3 et 4 ont été ignorées. On a choisi de d'implémenter directement la possibilité de s'échanger les clés.  
Voici le protocole que l'on utilise :  
> Ouverture du serveur    
> Recuperation de l'adresse IP  
> Mise a jour de l'adresse IP  
> Lancement des Sockets  
> Reception d'un Socket
> envoie d'une clé AES généré par le serveur (clé secrete + InitialVector)  
> Assimilation côté client des informations en fonction des balises initiales pour configurer les AES utilisé par l'AES  
> Lancement du Chat  

** 5) **  
Le cryptage et le decryptage fonctionne. On a commenté les affichages des différents messages pour plus de lisibilité.


--------------------------------------
**Ici s'achève les consignes du projets, voici ce que l'on a apporté**  
Le projet a été amélioré sur ces points :
> Possibilité de recevoir plusieurs Clients  
> Possibilité de communication privés entre deux clients  
> Sécurisation de l'envoie des clés AES par le protocole Diffi-Hellman  
> Construction d'une representation graphique du Chat  


Voici en quoi consiste le protocole Diffi-Hellman:  
/////////////////////////////////////////////////   
1- echange une clePrimaire et une racine clePrimaireRacine  
2- définition d'une cleScreteServeur et cleScreteClient  
toServeur = clePrimaireRacine ^ cleScreteClient  % clePrimaire  
toClient  = clePrimaireRacine ^ cleScreteServeur % clePrimaire  

Serveurkey = toServeur ^ cleScreteServeur % clePrimaire  
Clientkey = toClient ^ cleScreteClient % clePrimaire  
- Ainsi, comme les clés secrètes sont conservés sur les ordinateurs et ne sont pas échangés, il n'est pas possible de déterminer les 'ServeurKey' et 'Clientkey'.   
/////////////////////////////////////////////////   
