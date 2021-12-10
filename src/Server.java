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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.StringReader;

////// AES
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

public final class Server {
	
////////////////////////////////////////////////////////////////////////////////
//// AES ///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

private final class AES {
    
    private KeyGenerator generator; 
    private SecretKey key;
    private IvParameterSpec vector;
    final private String type = "AES/CBC/PKCS5Padding";
    
    private byte[] table;
    
    public void generateKey() {
        try { generator = KeyGenerator.getInstance("AES"); } catch (NoSuchAlgorithmException error) { error.printStackTrace(); }
        SecureRandom random = new SecureRandom();
        generator.init(256, random);
        key = generator.generateKey();
        
        table = new byte[16];
        new SecureRandom().nextBytes(table);
        this.vector = new IvParameterSpec(table);
    }
    
    public String getKey() { return Base64.getEncoder().encodeToString(key.getEncoded()); }
    public String getIntialVector() { return Arrays.toString(table); }
    
    public String encrypt(String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(this.type);
        cipher.init(Cipher.ENCRYPT_MODE, this.key, this.vector);
        byte[] cipher_text = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(cipher_text);
    }

    public String decrypt(String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    	Cipher cipher = Cipher.getInstance(this.type);
        cipher.init(Cipher.DECRYPT_MODE, this.key, this.vector);
        byte[] decoded = cipher.doFinal(Base64.getDecoder().decode(message));
        return new String(decoded);
    }
    
    public void setKey(String cleSecrete) {
		byte[] decodedKey = Base64.getDecoder().decode(cleSecrete);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		this.key = originalKey;
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
        
        this.table = vector;
        this.vector = new IvParameterSpec(vector);
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
	@Override public void run() { while(true) { try {


		encrypted_message = in.readLine();
		decrypted_message = "";
		
		if(encrypted_message != null) {
			try { decrypted_message = aes.decrypt(encrypted_message); } catch (Exception error) { error.printStackTrace(); }
			
			read(decrypted_message);
		} else {
			System.out.println(name + " leave the chat");
			clients.remove(name);
			// il manque des objets a detruire
			return;
		}

	
} catch (IOException error) { error.printStackTrace(); return; }
}}}); thread_receive.start(); }

/*
out.println("===========================================================");
out.println("All commands :");
out.println();
out.println("/help                          show this help");
out.println("/rename [NAME]                 change your name");
out.println("/color [RED] [GREEN] [BLUE]    change your colorname");
out.println("/private [NAME] [TEXT]         send private message to user");
out.println("===========================================================");
out.flush();
*/
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
					"HELP"
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
	private final String server_name = "Server";
	
	private final String server_red = "255";
	private final String server_green = "0";
	private final String server_blue = "0";
	
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

	String xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<diffiehellman>"
			+		"<key>" + "KEY" + "</key>"
			+ "/diffiehellman";
	
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
		String encrypted_message = client.aes.encrypt(xml_message);
		client.out.println(encrypted_message);
		client.out.flush();
		
		if(!sender.equals(server_name)) {
			client = clients.get(sender);
			encrypted_message = client.aes.encrypt(xml_message);
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
			String encrypted_message = client.aes.encrypt(xml_message);
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
	Client client = new Client(socket, "user" + String.valueOf(clientCounter++));
	clients.put(client.name, client);
	
	client.aes.generateKey();
	System.out.println(client.name + " enter the chat");
	mutex.unlock();
	sendPublicMessage(server_name, server_red, server_green, server_blue, client.name + " enter the chat");
	
	sendAesKey(client.name);

	
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
