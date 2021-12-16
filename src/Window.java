import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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

		@SuppressWarnings("unused")
		private final boolean is_public;

		@SuppressWarnings("unused")
		private final String sender_name;
		@SuppressWarnings("unused")
		private final Color sender_color;

		@SuppressWarnings("unused")
		private final String receiver_name;
		@SuppressWarnings("unused")
		private final Color receiver_color;

		@SuppressWarnings("unused")
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