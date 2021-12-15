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

	private final class AES {

		private SecretKey key;
		private byte[] iv = new byte[16];

		public void generateKey(BigInteger diffieHellman_key) {

			char[] password = diffieHellman_key.toString().toCharArray();
			SecretKeyFactory factory;
			try {
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(password, new byte[8], 65536, 256);
				SecretKey tmp = factory.generateSecret(spec);
				this.key = new SecretKeySpec(tmp.getEncoded(), "AES");
			} catch (NoSuchAlgorithmException | InvalidKeySpecException error) { error.printStackTrace(); }
			
		}
		
		public String encrypt(String decrypted_message) {
			try {
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(this.iv));
				AlgorithmParameters params = cipher.getParameters();
				byte[] ciphertext = cipher.doFinal(decrypted_message.getBytes(StandardCharsets.UTF_8));
				String encrypted_message = Base64.getEncoder().encodeToString(ciphertext);
				return encrypted_message;
			} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
				e.printStackTrace();
				return null;
			}
		}


		public String decrypt(String encrypted_message) {
			try {
				byte[] ciphertext = Base64.getDecoder().decode(encrypted_message);
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(this.iv));
				String decrypted_message = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
				return decrypted_message;
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
				return null;
			}
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

	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	public class Window extends JFrame implements WindowListener, KeyListener {
		private static final long serialVersionUID = 1L;

		////////////////////////////////////////////////////////////////////
		//// COLORS ////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////

		private final Color background_color = new Color(14, 15, 15);

		private final Color date_color = new Color(130, 130, 130);
		private final Color message_color = new Color(230, 230, 230);

		////////////////////////////////////////////////////////////////////////////////
		//// MESSAGE ///////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////
		private final class Message extends JPanel {

			/*-----------------------------------------------------------------------------|
			||----------------|----------------|------------------------------------------||
			||      NAME      |      DATE      |                   SPACE                  ||
			||----HEAD--------|----HEAD--------|----HEAD----------------------------------||
			|----THIS----------------------------------------------------------------------|
			|                                                                              |
			|                                  MESSAGE                                     |
			|                                                                              |
			|----THIS---------------------------------------------------------------------*/

			////////////////////////////////////////////////////////////////////
			//// VISUAL ELEMENTS ///////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;
			private final Panel head = new Panel();
			private final JLabel sender_label;
			private final JLabel arrow_label = new JLabel("  ->  ");
			private final JLabel receiver_label;
			private final JLabel date_label;
			private final JLabel space = new JLabel("  ");

			private final JTextArea textarea_message;

			////////////////////////////////////////////////////////////////////
			//// ATTRIBUTES ////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////

			private final boolean is_public;

			private final String sender_name;
			private final Color sender_color;

			private final String receiver_name;
			private final Color receiver_color;

			private final String date;

			private final String message;
			private final String encrypted_message;

			private boolean is_encrypted = false;

			////////////////////////////////////////////////////////////////////
			//// CONSTRUCTOR ///////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////
			public Message(boolean is_public, String sender_name, Color sender_color, String receiver_name, Color receiver_color, String date, String message, String encrypted_message){


				this.is_public = is_public;

				this.sender_name = sender_name;
				this.sender_color = sender_color;

				this.receiver_name = receiver_name;
				this.receiver_color = receiver_color;

				this.date = date;

				this.message = message;
				this.encrypted_message = encrypted_message;

				this.setLayout(new BorderLayout());

				////// HEAD
				head.setLayout(new BoxLayout(head, BoxLayout.X_AXIS));

				sender_label = new JLabel(sender_name);
				sender_label.setAlignmentX(LEFT_ALIGNMENT);
				sender_label.setAlignmentY(CENTER_ALIGNMENT);
				sender_label.setForeground(sender_color);
				head.add(sender_label);


				receiver_label = new JLabel();
				if(!is_public) {
					arrow_label.setAlignmentX(LEFT_ALIGNMENT);
					arrow_label.setAlignmentY(CENTER_ALIGNMENT);
					arrow_label.setForeground(message_color);
					head.add(arrow_label);

					receiver_label.setText(receiver_name);
					receiver_label.setAlignmentX(LEFT_ALIGNMENT);
					receiver_label.setAlignmentY(CENTER_ALIGNMENT);
					receiver_label.setForeground(receiver_color);
					head.add(receiver_label);
				}

				space.setAlignmentX(LEFT_ALIGNMENT);
				space.setAlignmentY(CENTER_ALIGNMENT);
				head.add(space);

				date_label = new JLabel(date);
				date_label.setAlignmentX(LEFT_ALIGNMENT);
				date_label.setAlignmentY(CENTER_ALIGNMENT);
				date_label.setForeground(date_color);
				head.add(date_label);

				this.add(head, BorderLayout.NORTH);

				////// TEXT AREA
				textarea_message = new JTextArea(message, 1, 1);
				textarea_message.setEditable(false);
				textarea_message.setColumns(60);

				textarea_message.setLineWrap(true);
				textarea_message.setWrapStyleWord(true);

				this.add(textarea_message, BorderLayout.SOUTH);
				this.setMaximumSize(new Dimension(450, 100));

				////// COLORS
				this.setBackground(background_color);

				textarea_message.setBackground(background_color);
				textarea_message.setForeground(message_color);


			}
			////////////////////////////////////////////////////////////////////
			//// SWITCH ////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////
			public final void setIsEncrypted(boolean is_encrypted) {


				this.is_encrypted = is_encrypted;

				if(this.is_encrypted) textarea_message.setText(encrypted_message);
				else textarea_message.setText(message);


			}}
		////////////////////////////////////////////////////////////////////////////////
		//// WINDOW ////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////

		/*-----------------------------------------------------------------------------|
		||----------------------------------------------------------------------------||
		|||--------------------------------------------------------------------------|||
		||||------------------------------------------------------------------------||||
		|||||----------------------------------------------------------------------|||||
		|||||                               SPACE                                  |||||
		|||||----------------------------------------------------------------------|||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                              ALL MESSAGES                              ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||                                                                        ||||
		||||----MAIN----------------------------------------------------------------||||
		|||----BODY------------------------------------------------------------------|||
		|||                                 INPUT                                    |||
		|||----BODY------------------------------------------------------------------|||
		||----SCROLLPANE---------------------------------------------------------------|
		|----WINDOWCHAT---------------------------------------------------------------*/

		private final Client client;

		private boolean is_encrypted = false;
		private boolean is_cleaned = true;

		private final Panel body = new Panel();

		//private final ScrollPane scroll_pane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		private final JPanel main = new JPanel();
		private final JScrollPane scrollpane = new JScrollPane(main);
		private int main_height = 0;
		private final Label space = new Label();

		private final JTextArea input = new JTextArea();

		private final ArrayList<Message> messages = new ArrayList<Message>();


		////////////////////////////////////////////////////////////////////
		//// ADD PUBLIC MESSAGE ////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////
		public final void addPublicMessage(String sender_name, Color sender_color, String date, String text, String encrypted_text){


			Message message = new Message(true, sender_name, sender_color, null, null, date, text, encrypted_text);
			message.setAlignmentX(LEFT_ALIGNMENT);
			message.setAlignmentY(BOTTOM_ALIGNMENT);

			messages.add(message);
			main.add(message);
			this.pack();


		}
		////////////////////////////////////////////////////////////////////
		//// ADD PRIVATE MESSAGE ///////////////////////////////////////////
		////////////////////////////////////////////////////////////////////
		public final void addPrivateMessage(String sender_name, Color sender_color, String receiver_name, Color receiver_color, String date, String text, String encrypted_text){


			Message message = new Message(false, sender_name, sender_color, receiver_name, receiver_color, date, text, encrypted_text);
			message.setAlignmentX(LEFT_ALIGNMENT);
			message.setAlignmentY(BOTTOM_ALIGNMENT);

			messages.add(message);
			main.add(message);
			this.pack();


		}
		////////////////////////////////////////////////////////////////////
		//// SWITCH ENCRYPTED\DECRYPTED ////////////////////////////////////
		////////////////////////////////////////////////////////////////////
		public final void setIsEncrypted(boolean is_encrypted){


			this.is_encrypted = is_encrypted;
			for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();)
				((Message) iterator.next()).setIsEncrypted(is_encrypted);


		}
		////////////////////////////////////////////////////////////////////
		//// CONSTRUCTOR ///////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////
		public Window(Client client){
			super();
			Window self = this;

			this.client = client;

			body.setLayout(new BorderLayout());

			JButton button = new JButton("switch");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setIsEncrypted(!is_encrypted);
					self.getContentPane().repaint();
				}
			});
			body.add(button, BorderLayout.NORTH);
			
			main.setBackground(background_color);
			main.setAutoscrolls(true);
			main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
			//main.setPreferredSize(new Dimension(500, 500));
			
			scrollpane.setPreferredSize(new Dimension(500, 500));
			
			////// AUTO AJUST SCROLLBAR
			scrollpane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
		        public void adjustmentValueChanged(AdjustmentEvent e) {
		        	if(main.getSize().height != main_height) { //// SI LA TAILLE DE MAIN CHANGE
		        		main_height = main.getSize().height;
		        		e.getAdjustable().setValue(e.getAdjustable().getMaximum());
		        	}
		            
		        }
		    });

			
			main.add(space);
			//scroll_pane.add(main);

			input.setBackground(background_color);
			input.setForeground(message_color);
			input.setRows(3);

			//body.add(scroll_pane, BorderLayout.CENTER);
			body.add(scrollpane, BorderLayout.CENTER);
			
			body.add(input, BorderLayout.SOUTH);
			this.add(body);

			//this.setUndecorated(true);
			this.pack();

			//this.setSize(500, 500);
			this.setMinimumSize(new Dimension(500, 500));
			this.setMaximumSize(new Dimension(500, 500));
			this.setResizable(false);

			this.addWindowListener(this);
			input.addKeyListener(this);

			this.setTitle("Chat");


			//affichage
			this.setVisible(true);


		}
		////////////////////////////////////////////////////////////////////////////////
		//// EVENTS ////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////

		////// WINDOW
		@Override
		public void windowOpened(WindowEvent e) {}
		@Override
		public void windowClosing(WindowEvent e) { System.exit(1); }
		@Override
		public void windowClosed(WindowEvent e) {}
		@Override
		public void windowIconified(WindowEvent e) {}
		@Override
		public void windowDeiconified(WindowEvent e) {}
		@Override
		public void windowActivated(WindowEvent e) {}
		@Override
		public void windowDeactivated(WindowEvent e) {}

		////// KEY
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				String message = input.getText()+"";

				client.sendMessage(message);

				input.setText("");
				is_cleaned = false;
			}
		}
		@Override
		public void keyReleased(KeyEvent e) {
			if(!is_cleaned) {
				is_cleaned = true;
				input.setText("");
			}
		}

		////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////
	}

	AES aes = new AES();
	DiffieHellMan securiteInitial = new DiffieHellMan();



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
	private final void sendMessage(String message) {
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
			case "/color":
				xml_message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<command><color>"
						+ 		"<red>" + command[1] + "</red>"
						+ 		"<green>" + command[2] + "</green>"
						+ 		"<blue>" + command[3] + "</blue>"
						+ "</color></command>";

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

			String encrypted_message = aes.encrypt(xml_message);

			/*
			//TODO erase
			System.out.println("message : "+xml_message);

			System.out.println(xml_message.length());
			System.out.println(encrypted_message.length());

			System.out.println("crypatage : "+encrypted_message);
			System.out.println("decrypatage :"+aes.decrypatage(encrypted_message));
			*/
			//encrypted_message = aes.encrypt(xml_message);
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
			window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), textInitiation, "");

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
			window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), "[Connecting] - Recherche en cours...","");
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

				window.addPrivateMessage("Chat", new Color(140,46,12), "You", new Color(0,230,230), getActualTime(), textRelationServeur, "");
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

						/*
						//TODO erase
						System.out.println("reception : "+ message);
						*/

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
	 * ajouter un Bouton
	 *
	 * faire le compte-rendue !
	 *
	 */

	public static void main(String[] args) {
		//$ ip -br -c a (sur le pc serveur)
		Client client = new Client("127.0.0.1",55555);
	}
}
