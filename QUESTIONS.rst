================================================================================
TP3 - Jonathan Pelletier (1245014) Karim Badrudin (1466559)
================================================================================

Question1
================================================================================
Il est possible de modifier le design de l'application de telle sorte que 
chaque client est responsable d'envoyer 2 messages pour chaque message envoyé.

L'idée est d'utiliser le tableau de client membre d'une salle de chat
en le considerant comme la représentation d'un arbre binaire. A partir de 
l'indice d'un client dans l'arbre, on peut calculer l'indice de ces deux fils 
de la manière suivante:

1. indice_fils_gauche = 2 * index + 1

2. indice_fils_droit = 2 * index + 2

Suffit de passer dans le message que l'on envoit au client son indice
dans le tableau. Le client qui reçoit ensuite le message sera responsable 
d'acheminer celui-ci à ces fils jusqu'à ce que le client qui reçoive un 
message soit un noeud feuille dans notre arbre binaire.

Cette simple modification permet de libérer la charge d'envoit de message
d'un client et de la faire passer de N messages à 2 messages.

Question2
================================================================================
Afin de résoudre le problème d'integrité et d'authenticité sur le réseau P2P,
une solution possible est l'utilisation d'une entité tierce qui serait 
responsable d'émettre des certificats aux clients du reseau. Les clients qui 
communiquent ensembles pourrait le faire en utilisant une encryption asymétrique
(RSA par exemple). Pour toute requête qui implique la modification d'un contenu,
le client responsable d'une clef pourrait exiger que la communication soit 
encrypté ce qui empêche un client malicieux de modifier des données qui ne lui 
appartiennent pas. Plus précisément, une clef publique et la preuve que cette 
clef publique appartient au client permet un échange d'une clef symétrique 
(clef AES par exemple) temporaire à l'aide de laquelle tous les autres messages
de la communication sont encryptés, assurant ainsi l'authenticité des requêtes.

Cette solution a pour avantage d'être simple à implémenter. Cependant, on 
peut considerer qu'il s'agit d'une violation du principe P2P car
le fonctionnement du réseau dépend maintenant d'une entité centrale. Dans le 
cadre d'une application de communication d'urgence, la solution par certificat
nous semble justifié. Dans un cas ou le réseau P2P est utilisé parce qu'on ne
peut avoir confiance en une authorité tierce (censure par un gouvernement par
exemple), il faut considérer une autre solution. À ce sujet, on peut consulter
le document RFC 5765: Security Issues and Solutions in Peer-to-Peer Systems for
Realtime Communications qui fait état des problèmes de sécurités particuliés aux
réseaux P2P et des solutions existantes afin d'y remédier.

Question3
================================================================================
Il est mieux d'éjecter les plus vieux clients pour deux raisons:

1. Les clients du réseau sont libres de se connecter et de se déconnecter comme
   bon leur semble. Un client plus récent est donc plus succesptible d'être
   toujours dans le réseau qu'un ancient client.

2. On est plus susceptible de communiquer avec un client qui vient tout juste 
   de communiquer avec nous. Il s'agit en fait du principe de localité 
   temporelle. On peut faire une analogie avec le fonctionnement d'une 
   cache dans un processeur en considérant que l'acceptation d'un nouveau client
   correspond à une politique de cache FIRST IN FIRST OUT.

Question4
================================================================================
Dans le monde reel, Le bootstrap Kademlia correspond à la procédure par laquelle
un noeud propage son identité à travers le réseau. Le noeud qui désire joindre 
le réseau prend l'adresse d'un noeud initialement connu (le noeud de bootstrap) 
et ajoute son identité à ce noeud. Le noeud effectue ensuite une demande de sa
propre identité au noeud de bootstrap. Ceci initie la recherche de la ressource 
et chacun des noeuds qui participent au relai de l'information va ajouter 
l'identité du nouveau client sur sa cache locale.

Afin d'obtenir l'adresse initiale du noeud bootstrap, on doit absolument 
connaitre l'identité initiale d'un noeud présent dans le réseau. Ce probleme
est à la fois simple et difficile à résoudre. De manière simple, on pourrait 
mettre en ligne sur un site web une liste des noeuds du réseau. Ceci est 
cependant un problème car on depend maintenant encore d'un entité centrale
pour assurer le bon fonctionnement du réseau. Comme autre solution, on pourrait
considérer le partage d'adresse de noeuds qui sont extrêmenent fiable 
(presque toujours connecté) à même le code source du client.


