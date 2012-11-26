package ca.polymtl.inf4402.tp3;

import il.technion.ewolf.dht.DHT;
import il.technion.ewolf.dht.DHTStorage;
import il.technion.ewolf.dht.SimpleDHTModule;
import il.technion.ewolf.dht.storage.SimpleDHTStorage;
import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.KadNetModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ChatClient {
	int localPort;
	String bootstrapAddr;
	private int bootstrapPort;

	KeybasedRouting kbr;
	DHTStorage storage;
	DHT dht;

	public static void main(String args[]) throws IOException,
			URISyntaxException {
		int bootstrapPort = -1;
		String bootstrapAddr = null;
		int localPort = -1;
		switch (args.length) {
		case 3:
			bootstrapAddr = args[1];
			try {
				bootstrapPort = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				usage();
			}

		case 1:
			try {
				localPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				usage();
			}
			break;
		default:
			usage();
			break;

		}

		ChatClient cc = new ChatClient(localPort, bootstrapAddr, bootstrapPort);
		cc.connect();

		menu(cc);
	}

	private static void menu(ChatClient cc) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {
			System.out.println("Menu (" + cc.kbr.getLocalNode() + ") :");
			System.out.println("[1] Liste des salles.");
			System.out.println("[2] Rejoindre une salle.");
			System.out.println("[3] Liste des membres d'une salle.");
			System.out.println("[4] Message aux membres d'une salle.");

			String room;
			String message;
			int choice = Utils.getIntInput(input);
			switch (choice) {
			case 1:
				List<String> rooms = cc.getRooms();
				System.out.println("Salles:");
				for (String r : rooms) {
					System.out.println(" " + r);
				}
				System.out.println();
				break;

			case 2:
				System.out.print("Nom de la salle: ");
				room = input.readLine();
				cc.joinRoom(room);
				break;

			case 3:
				System.out.print("Nom de la salle: ");
				room = input.readLine();
				List<Node> members = cc.getMembers(room);
				System.out.println("Membres de " + room + ":");
				for (Node n : members) {
					System.out.println(" " + n);
				}
				System.out.println();
				break;

			case 4:
				System.out.print("Nom de la salle: ");
				room = input.readLine();
				System.out.print("Message: ");
				message = input.readLine();
				cc.sendMessage(room, message);
				break;

			default:
				break;
			}
		}

	}

	public static void usage() {
		System.out
				.println("Usage: ChatClient localPort [bootstrapIp boostrapPort]");
		System.exit(1);
	}

	/**
	 * Crée un ChatClient attaché au port spécifié. Le client d'initialisation
	 * est également spécifié.
	 * 
	 * @param localPort
	 *            Port local auquel s'attacher.
	 * @param bootstrapAddr
	 *            Adresse à laquelle se connecter, soruce la forme
	 *            "adresse:port".
	 * @param bootstrapPort
	 * @throws IOException
	 */
	public ChatClient(int localPort, String bootstrapAddr, int bootstrapPort)
			throws IOException {
		this.localPort = localPort;
		this.bootstrapAddr = bootstrapAddr;
		this.bootstrapPort = bootstrapPort;

		Injector injector;

		KadNetModule kadNetModule = new KadNetModule();
		kadNetModule.setProperty("openkad.net.udp.port",
				Integer.toString(localPort));
		SimpleDHTModule simpleDHTModule = new SimpleDHTModule();

		injector = Guice.createInjector(kadNetModule, simpleDHTModule);

		kbr = injector.getInstance(KeybasedRouting.class);

		// Créer un réseau singleton.
		kbr.create();

		// Créer un stockage simple.
		storage = injector.getInstance(SimpleDHTStorage.class);
		storage.setNode(kbr.getLocalNode());

		// Créer l'objet DHT et lier l'objet de stockage.
		dht = injector.getInstance(DHT.class);
		dht.setStorage(storage);
		dht.setName("dht");
		dht.create();
	}

	/**
	 * Crée un ChatClient attaché au port spécifié.
	 * 
	 * @param localPort
	 *            Port local auquel s'attacher.
	 * @throws IOException
	 */
	public ChatClient(int localPort) throws IOException {
		this(localPort, null, -1);
	}

	/**
	 * Connecte le client au réseau à l'aide du bootstrap (si un tel client a
	 * été passé au constructeur).
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void connect() throws URISyntaxException {

		// Connecter notre réseau simple à un client du réseau existant.
		if (bootstrapAddr != null && bootstrapPort > 0) {
            String url_format = "oprnlad.udp://%s:%d/";
            String url = String.format(url_format, bootstrapAddr, bootstrapPort);
            
            System.out.println("the uri i connect to is:"+url);
            
            ArrayList<URI> addresses = new ArrayList<URI>();
            addresses.add(new URI(url));
            kbr.join(addresses);

		}
		
		// Enregistrement du callback de message.
		kbr.register("message_handler", new MessageHandler() {

		    @Override
			public void onIncomingMessage(Node from, String tag,
					Serializable content) {
				System.out.println("I got a message from: "+from.getURI("openkad.udp").toString());
				
			}
			@Override
			public Serializable onIncomingRequest(Node from, String tag,
					Serializable content) {
				// TODO Auto-generated method stub
				System.out.println("I got a request !: "+from.getURI("openkad.udp").toString());
		        return "Hello";
			}
		});


		
	}

	/**
	 * Va chercher la liste des salles en obtenant la valeur associée à la clé
	 * générée à partir de la chaîne "rooms".
	 * 
	 * @return Liste de noms de salles.
	 */
	public List<String> getRooms() {
		ArrayList<String> rooms = new ArrayList<String>();
		
		List<Serializable> vals = dht.get("rooms");
		
		for(Serializable val: vals)
		{
			rooms.add((String) val);
		}
		 
		return rooms;
	}

	/**
	 * Ajoute son nom aux membres d'une salle. La salle ne peut pas être un mot
	 * réservé (comme "rooms").
	 * 
	 * @param room
	 *            Nom de la salle à rejoindre.
	 */
	public void joinRoom(String room) {
		// À remplir
	}

	/**
	 * Obtient la liste des membres inscrits à une salle.
	 * 
	 * @param room
	 *            Le nom de la salle.
	 * @return
	 */
	public List<Node> getMembers(String room) {
		ArrayList<Node> members = new ArrayList<Node>();

		// À remplir

		return members;
	}

	/**
	 * Envoie un message aux membres d'une salle.
	 * 
	 * @param room
	 *            Le nom de la salle.
	 * @param message
	 *            Le message.
	 */
	public void sendMessage(String room, String message) {
		// À remplir
	}

	@Override
	public String toString() {
		return "Chatclient[" + localPort + ", " + kbr.getLocalNode().getKey()
				+ "]";
	}
}
