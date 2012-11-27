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

    indice_fils_gauche = 2 * index + 1
    indice_fils_droit = 2 * index + 2

Suffit de passer dans le message que l'on envoit au client son propre indice
dans le tableau. Le client qui reçoit ensuite le message sera responsable 
d'acheminer celui-ci à ces fils jusqu'a ce que le client qui recoive un 
message soit un noeud feuille dans notre arbre binaire.

Cette simple modification permet de liberer la charge d'envoit de message
d'un client et de la faire passer de N messagesa 2 messages.

Question2
================================================================================
On considère que le premier client qui fait une requête "put" pour une clef de
la table qui n'existe pas devient le propriétaire du contenu associé à cette 
clef. Les clients du réseau auront le droit de lire la valeur de la clef avec 
la requête "get" mais seul le propriétaire doit pouvoir modifier (avec une
requête "put" subséquente) ou effacer, avec une requête "delete", le contenue de
la clef.

Voici le modèle que l'on propose afin de mettre en place, de manière 
sécuritaire, notre concept de propriété. On expose d'abord les détails 
techniques et des explications seront ensuite donnée afin de bien comprendre
comment ce modèle permet d'assurer la sécurité des données. 

Éléments du modèle: MARCHE PASSSSSSSSSSSSS

1. Chaque client du réseau possède une paire de clef publique-privé que l'on
   nomme RSA_s pour la clef privée et RSA_p pour la clef publique.

2. Lors d'une requête "put", le client transmet l'information suivante:

    a. La clef.

    b. La valeur.

    c. RSA_p.

    d. Signature_propriete = RSA_s(sha512(clef || valeur)). Ici, || représente
    la concaténation.

3. Lors d'une requête "delete", le client transmet l'information suivante:

    a. La clef.

    b. RSA_p.

3. Lors des requêtes "put", le client responsable de la clef conserve la clef 
   publique des clients et la signature de la propriété.

4. Lors des requêtes "put" et "delete" subséquentes, le client responsable de la
   clef procède à la vérification suivante avant d'exécuter la commande:

    a. contenu = RSA_p( Signature_propriete )

    b. if shah512( clef || valeur) == contenu:
        executer_commande()
       else:
        refuser_commande()

Voici quelques explications qui permettent de comprendre le mécanisme. Pour 
construire son message de requête "put", un client commence par construire
la signature d'un identifiant du contenu donc il se considère le propriétaire.
Il prend donc le résultat du hachage de la clef et de la valeur puis il utilise
sa clef privé pour signer le résultat. Ceci permet de vérifier que le client
qui possède la clef publique RSA_p est celui qui se considère le propriétaire
d'un contenu qui possède un identifiant unique de sha512(clef || valeur).
Cette valeur de sha512 peut-être utilisé pour valider l'intégrité de la 
combinaison clef valeur.

Maintenant, 



        

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


