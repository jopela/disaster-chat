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

1. Les clients du reseau sont libre de se connecter et de se deconnecter

Question4
================================================================================
