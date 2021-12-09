import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

// AES
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Server {
	
	
	private class AES {
	    
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
	private class Client {
		Socket socket;
		PrintWriter out;
		BufferedReader in;
		
		AES aes = new AES();
		
		String name;
		
		Thread receive;
				
		Client(Socket s, String n){
			socket = s;
			try {
				out = new PrintWriter(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException error) { error.printStackTrace(); }
			name = n;
			
////////////////////////////////////////////////////////////////////////////////
//// RECEIVE ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
			receive = new Thread(new Runnable() {
				String message = "";
				@Override
				public void run() {
					while(true) {
						try {
							message = in.readLine();
							if(message != null) {
								try {
									message = aes.decrypt(message);
								} catch (Exception error) { error.printStackTrace(); }
								
								if(message.charAt(0) == '/') {
									message = message.substring(1);
									
									String[] command = message.split(" ");
									switch (command[0]) {
									case "help" :
										out.println("===========================================================");
										out.println("All commands :");
										out.println();
										out.println("/help                          show this help");
										out.println("/rename [NAME]                 change your name");
										out.println("/private [NAME] [TEXT]         send private message to user");
										out.println("===========================================================");
										out.flush();
										break;
									case "rename":
										command[1] = command[1];
										if(clients.containsKey(command[1])) {
											out.println("The name \"" + command[1] + "\" is already used");
											out.flush();
										} else {
											Client self = clients.get(name); // j'arrive pas a utiliser this dans ce cas
											clients.remove(name);
											name = command[1];
											clients.put(name, self);
											out.println("Name changed");
											out.flush(); // manque la securite du mutex
										}
										break;
									case "private" :
										if(clients.containsKey(command[1])) {
											Client other_client = clients.get(command[1]);
											message = command[2];
											for (int i = 3; i < command.length; i++) {
												message += " " + command[i];
											}
											other_client.out.println(name + " to " + other_client.name + " : " + message);
											other_client.out.flush();
										} else {
											out.println("Unknow user + " + command[1]);
											out.flush();
										}
										
										
										break;
									default :
										out.println("Unknow command : " + command[0]);
										out.flush();
										break;
									}
									
									System.out.println(name + " : /" + message);
								} else { // message publique :
									sendPublicMessage(name, message);
								}
							} else {
								// supprimer le client!
								System.out.println(name + " leave the chat");
								clients.remove(name);
								// il manque des objets a detruire
								return;
							}
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			});
			receive.start();
		}
	}

	// SOCKETS
	ServerSocket server = null;
	HashMap<String, Client> clients = new HashMap<String, Client>();
	
	int numberOfClients = 0;
	int clientCounter = 0;
	
	// TIME
	Date date = new Date();
	
	// MUTEX
	ReentrantLock mutex = new ReentrantLock();
	
	// THREADS
	Thread thread_connect = null;
	Thread thread_send = null;
	
	public String getActualTime() {
		return null;
	}
	
	public void sendKey(String name) {
		mutex.lock();
		Client client = clients.get(name);
		
		client.out.println("k" + client.aes.getKey());
		client.out.println("i" + client.aes.getIntialVector());
		client.out.flush();
		mutex.unlock();
	}
	
	public void sendPrivateMessage(String name, String receiver, String message) {
		String full_message = "<#private>" + "<@" + name + ">" + "<&" + this.getActualTime() + ">" + message;
		
		mutex.lock();
		try {
			Client client = clients.get(receiver);
			String encrypted_message = client.aes.encrypt(full_message);
			client.out.println(encrypted_message);
			client.out.flush();
		} catch (Exception error) { error.printStackTrace(); }
		mutex.unlock();
	}
	
	public void sendPublicMessage(String name, String message){
		String full_message = "<#public>" + "<@" + name + ">" + "<&" + this.getActualTime() + ">" + message;
		System.out.println(full_message);
		
		mutex.lock();
		for (Client client : clients.values()) {
			try {
				String encrypted_message = client.aes.encrypt(full_message);
				client.out.println(encrypted_message);
				client.out.flush();
			} catch (Exception error) { error.printStackTrace(); }			
		}
		mutex.unlock();
	}
	
	// SCANNER
	final Scanner scanner = new Scanner(System.in);
	
	public Server() {		
////////////////////////////////////////////////////////////////////////////////
//// CREATE SERVER /////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
		
		try { server = new ServerSocket(55555); }
		catch(IOException error) { error.printStackTrace(); System.exit(-1); }
		System.out.println("Lancement du serveur!");
		
////////////////////////////////////////////////////////////////////////////////
//// CONNECT CLIENTS ///////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
		
		thread_connect = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = server.accept();
						
						mutex.lock();
						Client client = new Client(socket, "user" + String.valueOf(clientCounter++));
						clients.put(client.name, client);
						
						client.aes.generateKey();
						System.out.println("Nouveau client connecte au nom de : " + client.name);
						mutex.unlock();
						
						sendKey(client.name);
					} catch (IOException e) { e.printStackTrace(); }
				}
			}
		});
		thread_connect.start();
		
////////////////////////////////////////////////////////////////////////////////
//// SEND MESSAGES /////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
		
		thread_send = new Thread(new Runnable() {
			String message = "";
			@Override
			public void run() {
				while(true) {
					message = scanner.nextLine();
					sendPublicMessage("Server", message);
				}
			}
		});
		thread_send.start();
		
	}
////////////////////////////////////////////////////////////////////////////////
//// MAIN //////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) { new Server(); }
	
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
}
