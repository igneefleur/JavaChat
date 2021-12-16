import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.BorderLayout;

////// COLOR
import java.awt.Color;


import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


@SuppressWarnings("unused")
public class Client {
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	AES aes = new AES();
	DiffieHellman securiteInitial = new DiffieHellman();



	//verification du message de FERMETURE DE CONNEXION
	Boolean conditionArret(String message) {
		return message.toLowerCase().equals("bye");
	};

	//Cle pour DiffiHellman
	String clePrimaire = "1867";
	String clePrimaireRacine = "934";

	//recuperation des cles AES et InitVector
	Boolean recupKey = false;
	Boolean recupInitVector = true;

	//variable pour femer la discussion
	Boolean baliseFin = true;

	//les Threads qui vont être utiliser
	Thread connexion = null;
	Thread recevoir = null;
	Thread envoyer = null;
	Socket echoSocket = null;
	PrintWriter out  = null;
	BufferedReader in = null;

	////// WINDOW
	Window window = new Window(this);

	//la lecture des messages qui vont être envoyé
	final Scanner scanner = new Scanner(System.in);


	public String getActualTime() {


		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalTime time = LocalTime.now();

		return time.format(formatter);


	}

	//fermeture du client - fermeture de l'application
	public void Quit() {
		System.out.println("#############################\n"
				+ "Chat : [Disconnected] - Au revoir.");
		System.exit(0) ;
	}

	public int avoirLesSecondes(LocalTime temps) {
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH/mm:ss");
		String instant = time.format(formatter);
		String reference = temps.format(formatter);


		String[] secondeInstant = instant.split(":");
		String[] secondeReference = reference.split(":");
		Integer reponse = Integer.parseInt(secondeInstant[1]) - Integer.parseInt(secondeReference[1]);

		if (reponse < 0 ) {
			reponse +=60;
		}

		return reponse;

	}

	////////////////////////////////////////////////////////////////////////////////
	//// WAIT KEY //////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	private boolean waitKey(String message) {


		DocumentBuilder document_builder = null;
		Document document = null;

		DocumentBuilderFactory document_builder_factory = DocumentBuilderFactory.newInstance();
		try { document_builder = document_builder_factory.newDocumentBuilder(); } catch (ParserConfigurationException error) { return false; }
		try { document = document_builder.parse(new InputSource(new StringReader(message))); } catch (SAXException | IOException error) { return false; }

		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		String root_name = root.getNodeName();

		if(root_name.equals("diffiehellman")) {
			String key = root.getElementsByTagName("key").item(0).getTextContent();


			securiteInitial.aliceCalculationOfKey(new BigInteger(key), securiteInitial.cleSecrete);
			aes.generateKey(securiteInitial.cleFinale);

			return true;
		}
		/*
		if(root_name.equals("aes")) {
			String key = root.getElementsByTagName("key").item(0).getTextContent();
			String vector = root.getElementsByTagName("vector").item(0).getTextContent();

			aes.setKey(key);
			aes.setIv(vector);

			return true;
		}*/

		return false;


	}
	////////////////////////////////////////////////////////////////////////////////
	//// SEND MESSAGE //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public final void sendMessage(String message) {
		String xml_message = "";

		if(message.length()==0)
			return;

		if(message.charAt(0) == '/') {
			////// COMMAND
			String[] command = message.split(" ");

			switch (command[0]) {
			case "/help":
				xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<command><help>"
						+ "</help></command>";

				break;
			case "/rename":
				xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<command><rename>"
						+ 		"<name>" + command[1] + "</name>"
						+ "</rename></command>";

				break;
			case "/recolor":
				xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<command><recolor>"
						+ 		"<red>" + command[1] + "</red>"
						+ 		"<green>" + command[2] + "</green>"
						+ 		"<blue>" + command[3] + "</blue>"
						+ "</recolor></command>";

				break;
			case "/private":
				xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<command><private>"
						+ 		"<receiver>" + command[1] + "</receiver>"
						+ 		"<content>" + message.substring(10 + command[1].length()) + "</content>"
						+ "</private></command>";

				break;
			case "/getKey":
				xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<command><getKey>"
						+ "</getKey></command>";

				break;
			default: return;
			}

		} else {

			////// PUBLIC MESSAGE
			xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<public>"
					+		"<content>" + message + "</content>"
					+ "</public>";
		}
		try {

			String encrypted_message = aes.encrypt(xml_message);

			out.println(encrypted_message);
			out.flush();
		} catch (Exception error) { error.printStackTrace(); }

	}
	////////////////////////////////////////////////////////////////////////////////
	//// READ MESSAGE //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	private final void readMessage(String message, String encrypted_message) {

		String sender = "";

		String sender_red = "";
		String sender_green = "";
		String sender_blue = "";
		Color sender_color;

		String receiver = "";

		String receiver_red = "";
		String receiver_green = "";
		String receiver_blue = "";
		Color receiver_color;

		String content = "";
		String time = "";

		DocumentBuilder document_builder = null;
		Document document = null;

		DocumentBuilderFactory document_builder_factory = DocumentBuilderFactory.newInstance();
		try { document_builder = document_builder_factory.newDocumentBuilder(); } catch (ParserConfigurationException error) { return; }
		try { document = document_builder.parse(new InputSource(new StringReader(message))); } catch (SAXException | IOException error) { return; }

		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		String root_name = root.getNodeName();

		switch (root_name) {
		case "public":
			////// PUBLIC MESSAGE
			sender = root.getElementsByTagName("sender").item(0).getTextContent();

			sender_red = root.getElementsByTagName("sender-red").item(0).getTextContent();
			sender_green = root.getElementsByTagName("sender-green").item(0).getTextContent();
			sender_blue = root.getElementsByTagName("sender-blue").item(0).getTextContent();

			time = root.getElementsByTagName("time").item(0).getTextContent();
			content = root.getElementsByTagName("content").item(0).getTextContent();

			sender_color = new Color(Integer.parseInt(sender_red), Integer.parseInt(sender_green), Integer.parseInt(sender_blue));

			System.out.println(time + " | " + sender + " -> " + "everyone" + " : " + content);
			window.addPublicMessage(sender, sender_color, time, content, encrypted_message);
			break;
		case "private":
			////// PRIVATE MESSAGE
			sender = root.getElementsByTagName("sender").item(0).getTextContent();

			sender_red = root.getElementsByTagName("sender-red").item(0).getTextContent();
			sender_green = root.getElementsByTagName("sender-green").item(0).getTextContent();
			sender_blue = root.getElementsByTagName("sender-blue").item(0).getTextContent();

			receiver = root.getElementsByTagName("receiver").item(0).getTextContent();

			receiver_red = root.getElementsByTagName("receiver-red").item(0).getTextContent();
			receiver_green = root.getElementsByTagName("receiver-green").item(0).getTextContent();
			receiver_blue = root.getElementsByTagName("receiver-blue").item(0).getTextContent();

			time = root.getElementsByTagName("time").item(0).getTextContent();
			content = root.getElementsByTagName("content").item(0).getTextContent();

			sender_color = new Color(Integer.parseInt(sender_red), Integer.parseInt(sender_green), Integer.parseInt(sender_blue));
			receiver_color = new Color(Integer.parseInt(receiver_red), Integer.parseInt(receiver_green), Integer.parseInt(receiver_blue));

			System.out.println(time + " | " + sender + " -> " + receiver + " : " + content);
			window.addPrivateMessage(sender, sender_color, receiver, receiver_color, time, content, encrypted_message);
			break;
		default:
			break;
		}
	}
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	@SuppressWarnings("deprecation")
	public Client(String lien,	int port) {

		try {
			//info serv
			System.out.println("Chat : [Connecting] - Recherche du serveur.");

			String textInitiation = "[Connecting] - La relation au serveur est en cours d'etablissement...";
			window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), textInitiation, textInitiation);

			connexion = new Thread(new Runnable() {
				@Override
				public void run() {
					LocalTime temps = LocalTime.now();
					Integer memo = 1;
					while(echoSocket == null) {
						Integer moment = avoirLesSecondes(temps);
						if ( moment != memo) {
							try {echoSocket = new Socket(lien,port);}
							catch (UnknownHostException e) {System.out.println("Chat : [Error] - L'hôte est inconnu : "+e); echoSocket =null;/*e.printStackTrace();*/}
							catch (IOException e) {System.out.println("Chat : [Error] - La connexion ne peut pas être établi : "+e); echoSocket =null;/*e.printStackTrace();*/}
							memo = moment;
						}

					}

				}
			});
			connexion.start();
			String connexionEnCours = "[Connecting] - Recherche en cours...";
			window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), connexionEnCours, connexionEnCours);
			//affichage du temps de recherche
			LocalTime temps = LocalTime.now();
			Integer memo = 1;
			while (echoSocket == null) {
				Integer moment = avoirLesSecondes(temps);
				if ( moment != memo) {
					System.out.println("Chat : [Connecting] - Recherche en cours... " +moment.toString()+"s.");
					memo = moment;
				}
				if (memo == 15) {
					connexion.stop();
					echoSocket = new Socket();
					System.out.println("Chat : [Error] - La connexion ne peut pas être établi : veuillez vérifier le port et l'adresse saisie");
					window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), "[Error] - La connexion ne peut pas être établi : veuillez vérifier le port et l'adresse saisie", "");
					System.out.println("Chat : [Error] - La connexion ne peut pas être établi : êtes-vous sur le même réseau que le serveur ?");
					window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), "[Error] - La connexion ne peut pas être établi : êtes-vous sur le même réseau que le serveur ?", "");
					Quit();
				}
			}
			if (echoSocket != null) {
				connexion.stop();
				String textRelationServeur = "[Connected] - La relation au serveur est effectuee.";

				window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), textRelationServeur, textRelationServeur);
				System.out.println("Chat : [Connecting] - Serveur trouvé.");

				//connexion
				System.out.println("Chat : [Connecting] - Lancement socket.");
			}
			out = new PrintWriter(echoSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			System.out.println("Chat : [Connecting] - Socket établi");
			System.out.println("Chat : [Connected]");

			////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////
			//Construction des clés pour Diffi-Hellman
			securiteInitial.setClePrimaire(new BigInteger(clePrimaire));
			securiteInitial.setClePrimaireRacine(new BigInteger(clePrimaireRacine));
			securiteInitial.genCleSecrete();

			BigInteger toServeur = securiteInitial.toServeur(securiteInitial.cleSecrete);
			String xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<diffiehellman>"
					+		"<key>" + toServeur.toString() + "</key>"
					+ "</diffiehellman>";



			out.println(xml_message);
			out.flush();
			////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////
			//discussion
			System.out.println(
					"#############################"
							+ "\n -- Bienvenue dans le Chat --"
							+ "\n############quit with 'bye'##");
			//envoyer des messages
			envoyer = new Thread(new Runnable() {
				String message;
				@Override
				public void run() {
					while(baliseFin){
						message = scanner.nextLine();
						//############################################
						// Décommenter pour avoir les messages cryptés
						//############################################
						/*
						try {System.out.println(aes.crypatage(message));} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| InvalidAlgorithmParameterException | BadPaddingException
								| IllegalBlockSizeException e1) {e1.printStackTrace();}
						try {System.out.println(aes.decrypatage(aes.crypatage(message)));} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
								| InvalidAlgorithmParameterException | BadPaddingException
								| IllegalBlockSizeException e1) {e1.printStackTrace();}
						 */
						//############################################


						sendMessage(message);

						//try {out.println(aes.crypatage(message));} catch (Exception e) {e.printStackTrace();}
						//out.flush();
						if (conditionArret(message)) {
							Quit();}
					}

				}
			});
			envoyer.start();

			recevoir = new Thread(new Runnable() {
				String message;
				@Override
				public void run() {

					//recuperation des informations pour le fonctionnement de la l'AES
					while(true) {
						try {
							String message = in.readLine();
							if(message != null && waitKey(message)) break;
						}catch (Exception e) {System.out.println("Chat : [Error] - "+e);}
					}

					//discussion classique
					try {
						message = in.readLine();

						while(message!=null){
							String decrypted_message = null;
							try {decrypted_message = aes.decrypt(message);} catch (Exception e) {System.out.println("Chat : [Error] - "+e);}
							//decrypted_message=message;
							readMessage(decrypted_message, message);

							if(conditionArret(message)) {
								Quit();
							}

							//pourquoi cette ligne
							message = in.readLine();
						}
						System.out.println("Chat : [Error] - Le serveur a rencontré une erreur. Fin de connexion");
						//recevoir.close();
						Quit();
					} catch (IOException e) {System.out.println("Chat : [Error] - "+e);}
				}
			});
			recevoir.start();
			/*
	        envoyer.join();
	        recevoir.join();
			 */

		}
		catch(UnknownHostException e){
			System.out.println("Chat : [Error] - L'hôte est inconnu : "+e) ;
			System.exit(-1) ;
		}
		catch(SocketException e) {
			System.out.println("Chat : [Error] - La socket émise est invalide : "+e);
		}
		catch(IOException e){
			System.out.println("Chat : [Error] - La connexion ne peut pas être établi : "+e) ;

		}

	}

	//TODO
	/*
	 *
	 *
	 * faire un bouton pour avoir la cle sur un fichier
	 *
	 *
	 *
	 * gestion de la perte de serveur
	 * gestion de la fermeture de la connexion
	 *
	 * fermeture de l'application
	 *
	 *
	 * faire le compte-rendue !
	 *
	 */

	public static void main(String[] args) {
		// GET SERVER ADDRESS via sh command :
		//$ ip -br -c a
		
		AddressWindow address_window = new AddressWindow();
		AddressWindow.Address address = address_window.waitAddress(); // blocks program
		
		Client client = new Client(address.ip, address.port);
	}
}
