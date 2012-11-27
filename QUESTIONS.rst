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
Mentionnons tout d'abord qu'il s'agit d'un problème très difficile à résoudre
à cause de la nature "décentralisé" des systèmes P2P.
Pour les systèmes que l'on utilise généralement dans notre vie quotidienne 
(site web de la banque, paiement en ligne etc.) l'authenticité des acteurs
est assuré grâce à une authorité tierce; l'authorité de certification. Cette
authorité s'occupe d'émettre des certificats qui permettent de faire l'échange
de clefs publiques sur un réseau qui peut contenir des agents malicieux. Ce 
modèle fonctionne "bien" pour les communications point à point mais, généralement,
les applications P2P ne peuvent avoir confiance en une telle authorité tierce.
Souvent, le but d'une application P2P est d'échapper au contrôle central afin
de permettre la diffusion libre de contenu. De plus, on peut considérer l'ajout
d'une authorité centrale comme étant une violation du principe P2P.

Il faut donc une autre manière de partager des clefs publiques sans avoir 
recours à une authorité tierce. En supposant que nous avons un budget 
illimité afin d'implémenter une solution, voici la structure de partage de clef
que l'on propose: le partage de clef quantique.

Avec le partage de clef quantique, il est possible à deux agents qui communiquent
sur le réseau de partager un secret de manière totalement sécuritaire.
Ce secret pourrait être une clef symétrique qui sera ensuite utilisé pour
encrypter le traffic du partage d'une clef publique. On peut donc authentifier
les requêtes des clients par signatures puisqu'il est possible de connaitre le
propriétaire d'une clef publique.


Question3
================================================================================
Il est mieux d'ejecter les plus vieux clients pour deux raisons:

1. Les clients du reseau sont libre de se connecter et de se deconnecter comme
   bon leur semble. Un client plus recent est donc plus succesptible d'etre
   toujours dans le reseau qu'un ancient client.

2. On est plus susceptible de communiquer avec un client qui vient tout juste 
   de communiquer avec nous. Il s'agit en fait du principe localite temporelle.
   SOn peut faire une analogie avec le fonctionnement d'une 
   cache dans un processeur en considerant que l'acceptation d'un nouveau client
   correspond a une politique de cache FIRST IN FIRST OUT.

Question4
================================================================================
Dans le monde reel, Le bootstrap Kademlia correspond a la procedure par laquelle
un noeud propage son identite a travers le reseau. Le noeud qui desire joindre 
le reseau prend l'adresse d'un noeud initialement connu (le noeud de bootstrap) 
et ajoute son identite a ce noeud. Le noeud effectue ensuite une demande de sa
propre identite au noeud de bootstrap. Ceci initie la recherche de la ressource 
et chacun des noeuds qui participent au relai de l'information va ajouter 
l'identite du nouveau client sur sa cache locale.

Afin d'obtenir l'adresse initiale du noeud bootstrap, on doit absolument 
connaitre l'identite initiale d'un noeud present dans le reseau. Ce probleme
est a la fois simple et difficile a resoudre. De maniere simple, on pourrait 
mettre en ligne sur un site web une liste des noeuds du reseau. Ceci est 
cependant un probleme car on depend maintenant encore d'un entite centrale
pour assurer le bon fonctionnement du reseau. Comme autre solution, on pourrait
considerer le partage d'adresse de noeuds qui sont extremenent fiable 
(presque toujours connecte) a meme le code source du client.


