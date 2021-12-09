import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

////// COLOR
import java.awt.Color;


@SuppressWarnings("unused")
public class Client {

	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	private class CleAES {

		private KeyGenerator cleGen; 
		private SecretKey cleSecrete;
		private IvParameterSpec initialVector;
		private String algo= "AES/CBC/PKCS5Padding";

		public void setKey(String cleSecrete) {
			byte[] decodedKey = Base64.getDecoder().decode(cleSecrete);
			SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
			this.cleSecrete = originalKey;
		}
		public void setIv(String vecteurInitial) {
			byte[] vector = new byte[16];
			int[] entier = new int[16];
			//decoupage du string
			vecteurInitial = vecteurInitial.substring(1, vecteurInitial.length()-1);
			String[] liste = vecteurInitial.split(", ");
			//passage String to int
			for (int i = 0; i < liste.length; i++) {
				entier[i] = Integer.parseInt(liste[i]);
			}
			//passage int to byte
			for (int i = 0; i < liste.length; i++) {
				vector[i] = ((Integer) entier[i]).byteValue();
			}

			this.initialVector = new IvParameterSpec(vector);
		}

		/*
		public void genererCle() {
			//construction de la clé
			try {cleGen = KeyGenerator.getInstance("AES");} catch (NoSuchAlgorithmException e) {System.out.println("L'AES existe pas"); e.printStackTrace();}
			SecureRandom alea = new SecureRandom();
			cleGen.init(256,alea);
			cleSecrete = cleGen.generateKey();

			//generation de l'initial vector
			byte[] initialVector = new byte[16];
			new SecureRandom().nextBytes(initialVector);
			this.initialVector = new IvParameterSpec(initialVector);


			String key = cleSecrete.getEncoded().toString();
			//System.out.println(key);
		}
		
		
		
		public IvParameterSpec generateIv() {
			byte[] initialVector = new byte[16];
			new SecureRandom().nextBytes(initialVector);
			return new IvParameterSpec(initialVector);

		}
		 */
		public String crypatage(String mes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
			Cipher cipher = Cipher.getInstance(this.algo);
			cipher.init(Cipher.ENCRYPT_MODE, this.cleSecrete, this.initialVector);
			byte[] cipherText = cipher.doFinal(mes.getBytes());
			return Base64.getEncoder().encodeToString(cipherText);
		}


		public String decrypatage(String mesCode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
			Cipher cipher = Cipher.getInstance(this.algo);
			cipher.init(Cipher.DECRYPT_MODE, this.cleSecrete, this.initialVector);
			byte[] mesDecrypte = cipher.doFinal(Base64.getDecoder().decode(mesCode));
			return new String(mesDecrypte);
		}


	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	private class PrimeNumberGen {
		private long n;
		public long getPrimeNumber(){
			this.n = (int)(new Random().nextDouble()*100)+250;
			long l = 0;
			l = (long) ((this.n)*(Math.log(this.n) + (Math.log(Math.log(this.n)) -1) + ((Math.log(Math.log(this.n))-2)/(Math.log(this.n))) - ((Math.log(Math.log(this.n)) -21.0/10.0)/Math.log(this.n)) ));
			for(long i=l;;i++){
				if(isPrime(i)){
					return i;
				}
			}
		}/////////////////////////////////////////////////   
		private boolean isPrime(long n){
			if(n%2 == 0 || n%3 == 0) return false;
			for(int i=5; i*i<=n; i+=6){
				if(n%i == 0 || n%(i+2)==0) return false;
			}
			return true;
		}
	}

	public class PrimitiveRootGen {
		long pr, clePrimaire, phi;
		public PrimitiveRootGen(long clePrimaire){
			this.clePrimaire = clePrimaire;
			this.phi = this.clePrimaire - 1;
			Vector<Long> primitiveRoots =  this.getPrimitiveRoot(this.clePrimaire, this.phi);
			this.pr = primitiveRoots.get(new Random().nextInt(primitiveRoots.size()));
		}

		public long getPr() {
			return pr;
		}

		private Vector<Long> getPrimitiveRoot(long clePrimaire, long phi){
			Vector<Long> primeFactors = this.genPrimesFactorsList(phi);
			Vector<Long> primitiveRoots = new Vector<>();
			for(long i = 2;i<clePrimaire;i++){
				boolean flg = false;
				for(Long l: primeFactors){
					BigInteger iBig = BigInteger.valueOf(i);
					BigInteger phiBig = BigInteger.valueOf(phi/l);
					BigInteger pBig = BigInteger.valueOf(clePrimaire);
					BigInteger pRootBig = iBig.modPow(phiBig, pBig);
					if(pRootBig.compareTo(BigInteger.valueOf(1))==0){
						flg = true;
						break;
					}
				}
				if(!flg)primitiveRoots.add(i);
			}
			return primitiveRoots;
		}

		private Vector<Long> genPrimesFactorsList(long phi){
			Vector<Long> primesFactors = new Vector<>();
			while(phi % 2 == 0){
				primesFactors.add((long) 2);
				phi /= 2;
			}
			for(long i=3;i<=Math.sqrt(phi);i+=2){
				if(phi % i == 0){
					primesFactors.add(i);
					phi /= i;
				}
			}
			if(phi > 2){
				primesFactors.add(phi);
			}
			return primesFactors;
		}
	}

	private class DiffieHellMan{
		BigInteger clePrimaire, clePrimaireRacine;
		
		public void getClePrimaireEtRacine(){
			this.clePrimaire = BigInteger.valueOf(new PrimeNumberGen().getPrimeNumber());
			this.clePrimaireRacine = BigInteger.valueOf(new PrimitiveRootGen(this.clePrimaire.intValue()).getPr());
		}

		public BigInteger getClePrimaire() {
			return clePrimaire;
		}

		public BigInteger getClePrimaireRacine() {
			return clePrimaireRacine;
		}

		public BigInteger toServeur(BigInteger cleSecreteClient){
			return this.clePrimaireRacine.modPow(cleSecreteClient, this.clePrimaire);
		}

		public BigInteger toClient(BigInteger cleSecreteServeur){
			return this.clePrimaireRacine.modPow(cleSecreteServeur, this.clePrimaire);
		}

		public BigInteger aliceCalculationOfKey (BigInteger toClient, BigInteger cleSecreteClient){
			return toClient.modPow(cleSecreteClient, this.clePrimaire);
		}

		public BigInteger bobCalculationOfKey(BigInteger toServeur, BigInteger cleSecreteServeur){
			return toServeur.modPow(cleSecreteServeur, this.clePrimaire);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////

	CleAES aes = new CleAES();
	DiffieHellMan securiteInitial = new DiffieHellMan();

	Boolean conditionArret(String message) {
		return message.toLowerCase().equals("bye");
	};

	//recuperation des cles AES et InitVector
	Boolean recupKey = true;
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

	//la lecture des messages qui vont être envoyé
	final Scanner scanner = new Scanner(System.in);


	// TODO : A SUPPRIMER
	public String[] retireBalise(String message) {
		int fin = 0;
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == '>') {
				fin = i+1;
			}
		}
		String[] retour = new String[2];
		retour[0] = message.substring(0, fin);
		retour[1] = message.substring(fin);
		return retour;
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
	
	if(root_name.equals("aes")) {
		String key = root.getElementsByTagName("key").item(0).getTextContent();
		String vector = root.getElementsByTagName("vector").item(0).getTextContent();
		
		aes.setKey(key);
		aes.setIv(vector);
		
		return true;
	}
	
	return false;
	
	
}	
////////////////////////////////////////////////////////////////////////////////
//// SEND MESSAGE //////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
private final void sendMessage(String message) {
	String xml_message = "";
	
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
		case "/color":
			xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<command><color>"
					+ 		"<red>" + command[1] + "</red>"
					+ 		"<green>" + command[2] + "</green>"
					+ 		"<blue>" + command[3] + "</blue>"
					+ "</rename></color>";
			
			break;
		case "/private":
			xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<command><private>"
					+ 		"<receiver>" + command[1] + "</receiver>"
					+ 		"<content>" + message.substring(10 + command[1].length()) + "</content>"
					+ "</private></command>";
			
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
		out.println(aes.crypatage(xml_message));
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
		// TODO : CONNECTION WITH WINDOW like :
		// window.addPublicMessage(sender, time, sender_color, content, encrypted_message);
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
		// TODO : CONNECTION WITH WINDOW like :
		// window.addPrivateMessage(sender, receiver, time, sender_color, receiver_color, content, encrypted_message);
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
			connexion = new Thread(new Runnable() {
				@Override
				public void run() {

					try {echoSocket = new Socket(lien,port);} 
					catch (UnknownHostException e) {System.out.println("Chat : [Error] - L'hôte est inconnu : "+e);/*e.printStackTrace();*/}
					catch (IOException e) {System.out.println("Chat : [Error] - La connexion ne peut pas être établi : "+e);/*e.printStackTrace();*/}
				}
			});
			connexion.start();

			//affichage du temps de recherche
			LocalTime temps = LocalTime.now();
			Integer memo = 1;
			while (echoSocket == null) {
				Integer moment = avoirLesSecondes(temps);
				if ( moment != memo) {
					System.out.println("Chat : [Connecting] - Recherche en cours... " +moment.toString()+"s.");
					memo = moment;
				}
				
				//a enlever
				if (memo == 5) {
					echoSocket = new Socket();
				}
			}
			if (echoSocket != null) {
				connexion.stop();
			}


			System.out.println("Chat : [Connecting] - Serveur trouvé.");

			//connexion
			System.out.println("Chat : [Connecting] - Lancement socket.");
			out = new PrintWriter(echoSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			System.out.println("Chat : [Connecting] - Socket établi");
			System.out.println("Chat : [Connected]");
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
							try {decrypted_message = aes.decrypatage(message);} catch (Exception e) {System.out.println("Chat : [Error] - "+e);}

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
	 * balise pour InitialVector
	 * balise pour SecretKey
	 * 
	 * Clé + IV unique pour chaque client
	 * Gestion message privé/public
	 * 
	 * gestion de la perte de serveur
	 * gestion de la fermeture de la connexion
	 * 
	 * fermeture de l'application
	 * 
	 * partie graphique
	 * 
	 * faire le compte-rendue
	 * 
	 */

	public static void main(String[] args) {
		//$ ip -br -c a (sur le pc serveur)
		Client client = new Client("127.0.0.1",55555);
	}
}


