////////////////////////////////////////////////////////////////////////////////
//// IMPORTS ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//////
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
////// MUTEX
import java.util.concurrent.locks.ReentrantLock;

import java.util.Base64;
import java.util.Date;

////// XML
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
////// AES
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

public final class Server {

////////////////////////////////////////////////////////////////////////////////
//// AES ///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

private final class AES {

	private KeyGenerator cleGen;
	private SecretKey cleSecrete;
	private byte[] initialVector;
	private String algo= "AES/CBC/PKCS5Padding";
	private ArrayList<Byte> table;

	public void generateKey(BigInteger cleFinale) {

		/*
		System.out.println(cleFinale);

        try { cleGen = KeyGenerator.getInstance("AES"); } catch (NoSuchAlgorithmException error) { error.printStackTrace(); }
        SecureRandom random = new SecureRandom();
        random.setSeed(cleFinale.toByteArray());
        cleGen.init(256, random);
        cleSecrete = cleGen.generateKey();

        table = new byte[16];
        random.nextBytes(table);
        this.initialVector = new IvParameterSpec(table);

        System.out.println("Les informations :");
        System.out.println(Arrays.toString(table));
        System.out.println(Base64.getEncoder().encodeToString(cleSecrete.getEncoded()));
        */

		Integer a = cleFinale.intValue();
		String b = a.toString();
		char[] password = new char[b.length()];
		for (int i = 0; i < password.length; i++) {
			password[i] = b.charAt(i);
		}

		Random random = new Random(a);



		byte[] salt = new byte[8];
		random.nextBytes(salt);

		/* Derive the key, given password and salt. */
		SecretKeyFactory factory = null;
		try {factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
		SecretKey tmp = null;;
		try {tmp = factory.generateSecret(spec);} catch (InvalidKeySpecException e) {e.printStackTrace();}
		this.cleSecrete = new SecretKeySpec(tmp.getEncoded(), "AES");

		Cipher cipher = null;
		try {cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");} catch (NoSuchAlgorithmException e1) {e1.printStackTrace();} catch (NoSuchPaddingException e1) {e1.printStackTrace();}
		AlgorithmParameters params = cipher.getParameters();
		try {this.initialVector = params.getParameterSpec(IvParameterSpec.class).getIV();} catch (InvalidParameterSpecException e) {e.printStackTrace();}

		/*
		//TODO erase
		System.out.println("Les informations :");
		System.out.println(cleFinale);
		//System.out.println(this.table.toString());
		System.out.println(Base64.getEncoder().encodeToString(cleSecrete.getEncoded()));
		*/

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
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, cleSecrete);
		AlgorithmParameters params = cipher.getParameters();
		try {this.initialVector = params.getParameterSpec(IvParameterSpec.class).getIV();} catch (InvalidParameterSpecException e) {e.printStackTrace();}
		byte[] cipherText =  cipher.doFinal(mes.getBytes(StandardCharsets.UTF_8));
		return Arrays.toString(cipherText);
	}


	public String decrypatage(String mesCode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
		setIv(mesCode);
		byte[] ciphertext = new byte[this.table.size()];

		for (int i = 0; i < ciphertext.length; i++)
			ciphertext[i] = this.table.get(i);

		Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher2.init(Cipher.DECRYPT_MODE, this.cleSecrete, new IvParameterSpec(this.initialVector));
		String plaintext = new String(cipher2.doFinal(ciphertext), StandardCharsets.UTF_8);
		return plaintext;
	}

    public void setKey(String cleSecrete) {
		byte[] decodedKey = Base64.getDecoder().decode(cleSecrete);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		this.cleSecrete= originalKey;
	}
    public void setIv(String vecteurInitial) {

    	ArrayList<Byte> vector = new ArrayList<Byte>();
    	ArrayList<Integer> entier = new ArrayList<Integer>();
        //byte[] vector = new byte[16];
        //int[] entier = new int[16];
        //decoupage du string
        vecteurInitial = vecteurInitial.substring(1, vecteurInitial.length()-1);
        String[] liste = vecteurInitial.split(", ");
        //passage String to int
        for (int i = 0; i < liste.length; i++) {
            entier.add(Integer.parseInt(liste[i]));
        }
        //passage int to byte
        for (int i = 0; i < liste.length; i++) {
            vector.add(entier.get(i).byteValue());
        }

        this.table = vector;
        //this.vector = new IvParameterSpec(vector);
    }

    public String getKey() { return Base64.getEncoder().encodeToString(cleSecrete.getEncoded()); }
    public String getIntialVector() { return table.toString(); }

}

////////////////////////////////////////////////////////////////////////////////
////Diffie-Hellmann ////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

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
	BigInteger cleSecrete;
	BigInteger cleFinale;

	public void genClePrimaireEtRacine(){
		this.clePrimaire = BigInteger.valueOf(new PrimeNumberGen().getPrimeNumber());
		this.clePrimaireRacine = BigInteger.valueOf(new PrimitiveRootGen(this.clePrimaire.intValue()).getPr());
	}

	public void genCleSecrete(){
		this.cleSecrete = BigInteger.valueOf(new PrimeNumberGen().getPrimeNumber());
	}

	public BigInteger getClePrimaire() {
		return clePrimaire;
	}

	public void setClePrimaireRacine(BigInteger a) {
		this.clePrimaireRacine = a;
	}

	public void setClePrimaire(BigInteger a) {
		this.clePrimaire = a;
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

	public void aliceCalculationOfKey (BigInteger toClient, BigInteger cleSecreteClient){
		cleFinale =  toClient.modPow(cleSecreteClient, this.clePrimaire);
	}

	public void bobCalculationOfKey(BigInteger toServeur, BigInteger cleSecreteServeur){
		cleFinale =  toServeur.modPow(cleSecreteServeur, this.clePrimaire);
	}
}

////////////////////////////////////////////////////////////////////////////////
//// CLIENT ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
private class Client {
////////////////////////////////////////////////////////////////////
//// ATTRIBUTES ////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////

////// SOCKET
	Socket socket;
	PrintWriter out;
	BufferedReader in;

////// AES
	AES aes = new AES();

////// ID
	String name;

////// THREAD
	private Thread thread_receive;

////// COLOR
	String red = "0";
	String green = "230";
	String blue = "230";
	private final boolean isBetween0And255(int value) { return 0 <= value && value <= 255; }



////////////////////////////////////////////////////////////////////
//// INITIALISATION ////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
	Client(Socket socket, String n){

		this.socket = socket;
		try {
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception error) { error.printStackTrace(); }
		this.name = n;

////////////////////////////////////////////////////////////////////
//// THREAD ////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
thread_receive = new Thread(new Runnable() {

	String encrypted_message = "";
	String decrypted_message = "";
	@Override public void run() {



	//Construction des clés pour Diffi-Hellman
	securiteInitial.setClePrimaire(new BigInteger(clePrimaire));
	securiteInitial.setClePrimaireRacine(new BigInteger(clePrimaireRacine));
	securiteInitial.genCleSecrete();

	sendDiffieHellmanKey(n);




	String message = "";
	try {message = in.readLine();} catch (IOException e) {}
	while(!waitKey(name, message)) {

		}

	Client client = clients.get(name);
	client.aes.generateKey(securiteInitial.cleFinale);

	while(true) { try {


		encrypted_message = in.readLine();
		/*
		//TODO erase
		System.out.println("reception : "+ encrypted_message);
		*/

		decrypted_message = "";

		if(encrypted_message != null) {
			try { decrypted_message = aes.decrypatage(encrypted_message); } catch (Exception error) { error.printStackTrace(); }

			/*
			//TODO erase
			System.out.println("decrypatage :"+decrypted_message);
			*/
			decrypted_message=encrypted_message;
			read(decrypted_message);
		} else {
			System.out.println(name + " leave the chat");
			clients.remove(name);
			// il manque des objets a detruire
			return;
		}


} catch (IOException error) { error.printStackTrace(); return; }
}}}); thread_receive.start(); }


////////////////////////////////////////////////////////////////////
//// READ MESSAGE //////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
private final void read(String message) {


	Client sender = clients.get(name);
	Client receiver;
	String receiver_name = "";
	String content = "";

	DocumentBuilder document_builder = null;
	Document document = null;

	DocumentBuilderFactory document_builder_factory = DocumentBuilderFactory.newInstance();
	try { document_builder = document_builder_factory.newDocumentBuilder(); } catch (ParserConfigurationException error) { error.printStackTrace(); return; }
	try { document = document_builder.parse(new InputSource(new StringReader(message))); } catch (SAXException | IOException error) { error.printStackTrace(); return; }

	document.getDocumentElement().normalize();
	Element root = document.getDocumentElement();
	String root_name = root.getNodeName();

	switch (root_name) {
	case "public":
////// PUBLIC MESSAGE
		content = root.getElementsByTagName("content").item(0).getTextContent();

		sendPublicMessage(sender.name, sender.red, sender.green, sender.blue, content);
		break;
	case "private":
////// PRIVATE MESSAGE
		receiver_name = root.getElementsByTagName("receiver").item(0).getTextContent();
		content = root.getElementsByTagName("content").item(0).getTextContent();

		if(clients.containsKey(receiver_name)) {
			receiver = clients.get(receiver_name);
			sendPrivateMessage(sender.name, sender.red, sender.green, sender.blue, receiver.name, receiver.red, receiver.green, receiver.blue, content);
		} else
			sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Unknow user " + receiver_name);
		break;
	case "command":
////// COMMAND
		String command = root.getFirstChild().getNodeName();
		switch (command) {
		case "help":
			sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue,
					         "==========================================================="
					+ "\n" + "All commands :"
					+ "\n"
					+ "\n" + "/help                          show this help"
					+ "\n" + "/rename [NAME]                 change your name"
					+ "\n" + "/color [RED] [GREEN] [BLUE]    change your colorname"
					+ "\n" + "/private [NAME] [TEXT]         send private message to user"
					+ "\n" + "==========================================================="
			);
			break;
		case "rename":
			String new_name = root.getElementsByTagName("name").item(0).getTextContent();

			if(clients.containsKey(new_name) || new_name.equals(server_name))
				sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Name " + new_name + " is already used");
			else {
				Client self = clients.get(name);
				clients.remove(name);
				name = new_name;
				clients.put(name, self);
				sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Your name changes to " + name);
			}
			break;
		case "color":
			// TODO

			String sender_red = root.getElementsByTagName("red").item(0).getTextContent();
			String sender_green = root.getElementsByTagName("green").item(0).getTextContent();
			String sender_blue = root.getElementsByTagName("blue").item(0).getTextContent();
			try {
				if(isBetween0And255(Integer.parseInt(sender_red)) && isBetween0And255(Integer.parseInt(sender_green)) && isBetween0And255(Integer.parseInt(sender_blue))) {
					sender.red = sender_red;
					sender.green = sender_green;
					sender.blue = sender_blue;

					sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Your color changes to " + sender.red + " " + sender.green + " " + sender.red);
				} else
					sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Cannot change your color to " + sender_red + " " + sender_green + " " + sender_red);
			} catch (NumberFormatException error) {
				sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Cannot change your color to " + sender_red + " " + sender_green + " " + sender_red);
			}
			break;
		case "private":
			receiver_name = root.getElementsByTagName("receiver").item(0).getTextContent();
			content = root.getElementsByTagName("content").item(0).getTextContent();

			if(clients.containsKey(receiver_name)) {
				receiver = clients.get(receiver_name);

				sendPrivateMessage(sender.name, sender.red, sender.green, sender.blue, receiver.name, receiver.red, receiver.green, receiver.blue, content);
			} else
				sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Unknow user " + receiver_name);
			break;
		default:
			sendPrivateMessage(server_name, server_red, server_green, server_blue, sender.name, sender.red, sender.green, sender.blue, "Unknow command " + command);

break; } break; default: break; }}}
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////////////////////
//// SERVER ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////
//// ATTRIBUTES ////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////// CLIENTS
	private ServerSocket server = null;
	private final HashMap<String, Client> clients = new HashMap<String, Client>();
	private int clientCounter = 0;

////// TIME
	private Date date = new Date();

////// THREADS
	private final ReentrantLock mutex = new ReentrantLock();

	private final Thread thread_connect;
	private final Thread thread_send;

////// SCANNER
	private final Scanner scanner = new Scanner(System.in);

////// OTHERS
	private DiffieHellMan securiteInitial = new DiffieHellMan();
	private final String server_name = "Server";

	private final String server_red = "255";
	private final String server_green = "0";
	private final String server_blue = "0";

	//Cle pour DiffiHellman
	String clePrimaire = "1867";
	String clePrimaireRacine = "934";


////////////////////////////////////////////////////////////////////////////////
////WAIT KEY //////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
private boolean waitKey(String name, String message) {


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

		securiteInitial.bobCalculationOfKey(new BigInteger(key), securiteInitial.cleSecrete);

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
////////////////////////////////////////////////////////////////////
//// GET TIME //////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
public String getActualTime() {


	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	LocalTime time = LocalTime.now();

	return time.format(formatter);

 
}
////////////////////////////////////////////////////////////////////
////// SEND NUMBER /////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
public void sendDiffieHellmanKey(String name) {

	mutex.lock();
	Client client = clients.get(name);

	BigInteger toClient = securiteInitial.toClient(securiteInitial.cleSecrete);
	String xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<diffiehellman>"
			+		"<key>" + toClient.toString() + "</key>"
			+ "</diffiehellman>";


	client.out.println(xml_message);

	client.out.flush();
	mutex.unlock();

}
////////////////////////////////////////////////////////////////////
//// SEND KEY //////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
public void sendAesKey(String name) {


	mutex.lock();
	Client client = clients.get(name);

	String xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<aes>"
			+		"<key>" + client.aes.getKey() + "</key>"
			+		"<vector>" + client.aes.getIntialVector() + "</vector>"
			+ "</aes>";

	client.out.println(xml_message);
	client.out.flush();
	mutex.unlock();


}
////////////////////////////////////////////////////////////////////
//// SEND PRIVATE MESSAGE //////////////////////////////////////////
////////////////////////////////////////////////////////////////////
public void sendPrivateMessage(String sender, String sender_red, String sender_green, String sender_blue, String receiver, String receiver_red, String receiver_green, String receiver_blue, String content) {


	String time = this.getActualTime();

	System.out.println(time + " | " + sender + " -> " + receiver + " : " + content);
	String xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	+ "<private>"
	+		"<sender>" + sender + "</sender>"

	+		"<sender-red>" + sender_red + "</sender-red>"
	+		"<sender-green>" + sender_green + "</sender-green>"
	+		"<sender-blue>" + sender_blue + "</sender-blue>"

	+		"<receiver>" + receiver + "</receiver>"

	+		"<receiver-red>" + receiver_red + "</receiver-red>"
	+		"<receiver-green>" + receiver_green + "</receiver-green>"
	+		"<receiver-blue>" + receiver_blue + "</receiver-blue>"

	+		"<time>" +  this.getActualTime() + "</time>"
	+		"<content>" + content + "</content>"
	+ "</private>";


	mutex.lock();
	try {
		Client client = clients.get(receiver);
		String encrypted_message = client.aes.crypatage(xml_message);
		client.out.println(encrypted_message);
		client.out.flush();

		if(!sender.equals(server_name)) {
			client = clients.get(sender);
			encrypted_message = client.aes.crypatage(xml_message);
			client.out.println(encrypted_message);
			client.out.flush();
		}
	} catch (Exception error) { error.printStackTrace(); }
	mutex.unlock();


}
////////////////////////////////////////////////////////////////////
//// SEND PUBLIC MESSAGE ///////////////////////////////////////////
////////////////////////////////////////////////////////////////////
public void sendPublicMessage(String sender, String red, String green, String blue, String content){


	String time = this.getActualTime();

	System.out.println(time + " | " + sender + " -> " + "everyone" + " : " + content);
	String xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	+ "<public>"
	+		"<sender>" + sender + "</sender>"

	+		"<sender-red>" + red + "</sender-red>"
	+		"<sender-green>" + green + "</sender-green>"
	+		"<sender-blue>" + blue + "</sender-blue>"

	+		"<time>" +  this.getActualTime() + "</time>"
	+		"<content>" + content + "</content>"
	+ "</public>";

	mutex.lock();
	for (Client client : clients.values()) {
		try {

			/*
			//TODO erase
			System.out.println("message : "+xml_message);
			System.out.println("crypatage : "+client.aes.crypatage(xml_message));
			*/

			String encrypted_message = client.aes.crypatage(xml_message);
			client.out.println(encrypted_message);
			client.out.flush();
		} catch (Exception error) { error.printStackTrace(); }
	}
	mutex.unlock();


}
////////////////////////////////////////////////////////////////////
//// CREATE SERVER /////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
public Server() {


	try { server = new ServerSocket(55555); }
	catch(IOException error) { error.printStackTrace(); System.exit(-1); }
	System.out.println("Server launch!");




////////////////////////////////////////////////////////////////////
//// THREAD CONNECT CLIENTS ////////////////////////////////////////
////////////////////////////////////////////////////////////////////
thread_connect = new Thread(new Runnable() { @Override public void run() { while(true) { try {


	Socket socket = server.accept();

	mutex.lock();
	Client client = new Client(socket, "User" + String.valueOf(clientCounter++));
	clients.put(client.name, client);

	System.out.println(client.name + " enter the chat");
	mutex.unlock();
	// TODO : ENVOYER CE MESSAGE QUAND TOUS LES CLIENTS AURONT LA BONNE CLEF
	//sendPublicMessage(server_name, server_red, server_green, server_blue, client.name + " enter the chat");

	// TODO : A SUPPRIMER AVEC LA FONCTION
	//sendAesKey(client.name);


} catch (IOException error) { error.printStackTrace(); } }}}); thread_connect.start();
////////////////////////////////////////////////////////////////////////////////
//// THREAD SEND MESSAGES //////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
thread_send = new Thread(new Runnable() {

	String message = "";
	@Override public void run() {while(true) {


		message = scanner.nextLine();
		sendPublicMessage(server_name, server_red, server_green, server_blue, message);


}}}); thread_send.start(); }
////////////////////////////////////////////////////////////////////////////////
//// MAIN //////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

public static void main(String[] args) { new Server(); }

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
}
