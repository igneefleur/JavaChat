
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.net.InetAddress;

public class Pouet {
    
    public static void main(String[] args) throws IOException {
    /*
     * Configuration des informations sur le serveur de communication
     */
        
    System.out.println("Lancement socket");
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
    try{
        echoSocket = new Socket(InetAddress.getByName("6.tcp.ngrok.io"), 15318) ;
        System.out.println("hey");
        out = new PrintWriter(echoSocket.getOutputStream(),true) ;
        in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())) ;
        System.out.println("Socket lancé");
    }
    catch(UnknownHostException e){
        System.out.println("« Destination unknown »") ;
        System.exit(-1) ;
            }
    catch(IOException u){
        System.out.println("« now to investigate this IO issue »") ;
        System.exit(-1) ;
            }
    
    BufferedReader stdn = new BufferedReader(
            new InputStreamReader(System.in)
            );
    String userInput;
    
    while ((userInput = stdn.readLine()) != null){
        out.println(userInput);
        System.out.println("echo :" + in.readLine());
    }

    out.close();
    in.close();
    stdn.close();
    echoSocket.close();
    
    
    }
}
