================================================================================
TP3 - Jonathan Pelletier (1245014) Karim Badrudin (1466559)
================================================================================

Question1
================================================================================
Il est possible de modifier le design de l'application de telle sorte que 
chaque client est responsable d'envoyer 2 messages pour chaque message envoye.

L'idee est d'utiliser le tableau de client en le considerant comme la 
representation d'un arbre binaire. A partir de l'indice d'un client
dans l'arbre, on peut calculer l'indice de ces deux fils de la maniere 
suivante:

    indice_fils_gauche = 2 * index + 1
    indice_fils_droit = 2 * index + 2

Suffit de passer dans le message que l'on envoit au client son propre indice
dans le tableau. Le client qui recoit ensuite le message sera responsable 
d'acheminer celui-ci a ces fils jusqu'a ce que le client qui recoive un 
message soit un noeud feuille dans notre arbre binaire.

Cette simple modification permet de liberer la charge d'envoit de message
d'un client et de la faire passer de N envoit a 2 envoit.

Question2
================================================================================
pistes: HMAC ?  public-key something?

Question3
================================================================================
Il est mieux d'ejecter les plus vieux clients pour deux raisons:

1. Les clients du reseau sont libre de se connecter et de se deconnecter comme
   bon leur semble. Un client plus recent est donc plus succesptible d'etre
   toujours dans le reseau qu'un ancient client.

2. On est plus susceptible de communiquer avec un client qui vient tout juste 
   de communiquer avec nous. Il s'agit en fait du principe (TODO: trouver le nom
   du principe). On peut faire une analogie avec le fonctionnement d'une 
   cache dans un processeur en disant que l'acceptation d'un nouveau client
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
mettre en ligne sur un site web une liste des noeud du reseau. Ceci est 
cependant un probleme car on depend maintenant encore d'un entite centrale
pour assurer le bon fonctionnement du reseau. Cette solution doit etre couple
a un autre processus de dissusion des noeuds qui sont en lignes dans le reseau.


