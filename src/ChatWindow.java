import java.util.ArrayList;
import java.util.Iterator;

// EVENTS
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// AWT
import java.awt.Frame;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Label;

import java.awt.Color;
import java.awt.Dimension;

import java.awt.BorderLayout;

// SWING
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;



public class ChatWindow extends Frame implements WindowListener {
	private static final long serialVersionUID = 1L;

////////////////////////////////////////////////////////////////////////////////
//// COLORS ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

	private static final Color background_color = new Color(14, 15, 15);
	
	private static final Color date_color = new Color(130, 130, 130);
	private static final Color message_color = new Color(230, 230, 230);

	private class Message extends JPanel {
////////////////////////////////////////////////////////////////////////////////
//// MESSAGE ///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
		
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
		
		private final Panel head = new Panel();
		private final JLabel label_name;
		private final JLabel label_date;
		private final JLabel space = new JLabel("  ");
		
		private final JTextArea textarea_message;
		
////////////////////////////////////////////////////////////////////
//// ATTRIBUTES ////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
		
		private final String name;
		private final Color name_color;
		
		private final String date;
		
		private final String message;
		private final String encrypted_message;
		
		private boolean is_encrypted = false;
		
		public Message(String name, String date, Color name_color, String message, String encrypted_message){
////////////////////////////////////////////////////////////////////
//// CONSTRUCTOR ///////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
			
			this.name = name;
			this.date = date;
			this.name_color = name_color;
			this.message = message;
			this.encrypted_message = encrypted_message;
			
			this.setLayout(new BorderLayout());
			
			label_name = new JLabel(name);
			label_name.setAlignmentX(LEFT_ALIGNMENT);
			label_name.setAlignmentY(CENTER_ALIGNMENT);
			
			label_date = new JLabel(date);
			label_date.setAlignmentX(LEFT_ALIGNMENT);
			label_date.setAlignmentY(CENTER_ALIGNMENT);
			
			space.setAlignmentX(LEFT_ALIGNMENT);
			space.setAlignmentY(CENTER_ALIGNMENT);
			
			head.setLayout(new BoxLayout(head, BoxLayout.X_AXIS));
			head.add(label_name);
			head.add(space);
			head.add(label_date);
			
			this.add(head, BorderLayout.NORTH);
			
			// TEXT AREA
			textarea_message = new JTextArea(message, 1, 1);
			textarea_message.setEditable(false);
			textarea_message.setColumns(60);

			textarea_message.setLineWrap(true);
			textarea_message.setWrapStyleWord(true);
			
			this.add(textarea_message, BorderLayout.SOUTH);
			this.setMaximumSize(new Dimension(450, 100));
			
			// COLORS
			this.setBackground(background_color);
			
			label_name.setForeground(name_color);
			label_date.setForeground(date_color);

			textarea_message.setBackground(background_color);
			textarea_message.setForeground(message_color);
		}
////////////////////////////////////////////////////////////////////
//// SWITCH ////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////

		public void setIsEncrypted(boolean is_encrypted) {
			this.is_encrypted = is_encrypted;
			
			if(this.is_encrypted) textarea_message.setText(encrypted_message);
			else textarea_message.setText(message);
		}
		
		
	}
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
	
	private boolean is_encrypted;
	
	private final Panel body;
	
	private final ScrollPane scroll_pane;
	private final Panel main;
	private final Label space = new Label();
	
	private final ArrayList<Message> messages = new ArrayList<Message>();
	
	public void addMessage(String name, String date, Color name_color, String text, String encrypted_text){
		Message message = new Message(name, date, name_color, text, encrypted_text);
		message.setAlignmentX(LEFT_ALIGNMENT);
		message.setAlignmentY(BOTTOM_ALIGNMENT);
		
		messages.add(message);
		main.add(message);
		//this.pack();
	}
	
	public void setIsEncrypted(boolean is_encrypted){
		this.is_encrypted = is_encrypted;
		
		for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();)
			((Message) iterator.next()).setIsEncrypted(is_encrypted);
	}
	
	public ChatWindow(){
		super();
		
		body = new Panel();
		
		scroll_pane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		main = new Panel();
		main.setBackground(background_color);
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));		
		
		main.add(space);
		
		scroll_pane.add(main);
		this.add(scroll_pane);

		this.setUndecorated(true);
		this.pack();
		
		this.setSize(500, 500);
		this.setResizable(true);
		this.addWindowListener(this);
				
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		ChatWindow chat = new ChatWindow();

		Color test = new Color(230, 130, 130);
		
		// TESTS
		chat.addMessage("Florian", "09:46", test, "Dispo a partir de 22h", "");
		chat.addMessage("Theo", "09:46", test, "t'es dispo demain?", "");
		chat.addMessage("Florian", "09:47", test, "Oui", "");
		chat.addMessage("Florian", "09:47", test, "16h->17h puis a partir de 21h45", "");
		chat.addMessage("Florian++", "09:47", test, "go le faire demain du coup", "");
		chat.addMessage("Theo", "09:47", test, "vous preferez quelle heure?", "");
		chat.addMessage("Florian++", "09:48", test, "22h", "");
		chat.addMessage("Florian++", "09:49", test, "comme ca y a flo", "");
		chat.addMessage("Florian", "09:49", test, "minutes", "");

		
		chat.setIsEncrypted(true);
		chat.setIsEncrypted(false);
	}

////////////////////////////////////////////////////////////////////////////////
//// EVENTS ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
	
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

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
}
