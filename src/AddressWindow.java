import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public final class AddressWindow extends JFrame implements WindowListener {
	private static final long serialVersionUID = 1L;

	private class Address {
		public final String ip;
		public final int port;
		public Address(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}
	}
	
	////// BODY
	private final JPanel body = new JPanel();
		
		////// HEADER
		private final JPanel header = new JPanel();
			private final JLabel explanation = new JLabel("Please enter your server's address.");
		
		////// MAIN
		private final JPanel main = new JPanel();
			
			private final JPanel top = new JPanel();
				private final JLabel ip_label = new JLabel("IP : ");
				private final JTextArea ip_textarea = new JTextArea("");
		
			private final JPanel bottom = new JPanel();
				private final JLabel port_label = new JLabel("Port : ");
				private final JTextArea port_textarea = new JTextArea("");
		
		////// FOOTER
		private final JPanel footer = new JPanel();
			private final JButton button = new JButton("OK");
	
	////// WAIT ADDRESS
	private boolean address_given = false;
	private boolean isAdressGiven() { return this.address_given; }
	private String ip;
	private int port;
	public Address getAddress() { return new Address(this.ip, this.port); }
	public final ReentrantLock mutex = new ReentrantLock();
	
	public AddressWindow() {
		AddressWindow self = this;
		this.addWindowListener(this);
		
		////// BODY
		body.setLayout(new BorderLayout());
		
		////// MAIN
		main.setLayout(new BorderLayout());
		ip_textarea.setRows(1); ip_textarea.setColumns(20);
		port_textarea.setRows(1); port_textarea.setColumns(20);
		
		////// FOOTER
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				self.mutex.lock();
				self.ip = ip_textarea.getText();
				self.port = Integer.parseInt(port_textarea.getText());
				self.address_given = true;
				self.mutex.unlock();
			}
		});
		
		//// ADD TO WINDOW
		header.add(explanation);
		
		top.add(ip_label, BorderLayout.EAST);
		top.add(ip_textarea, BorderLayout.WEST);
		bottom.add(port_label, BorderLayout.EAST);
		bottom.add(port_textarea, BorderLayout.WEST);
		main.add(top, BorderLayout.NORTH);
		main.add(bottom, BorderLayout.SOUTH);
		
		footer.add(button);
		
		body.add(header, BorderLayout.NORTH);
		body.add(main, BorderLayout.CENTER);
		body.add(footer, BorderLayout.SOUTH);
		this.add(body);
		
		////// VISUAL
		this.setSize(new Dimension(300, 150));
		this.setPreferredSize(new Dimension(300, 150));
		this.setResizable(false);
		this.setTitle("ADDRESS");
		
		this.pack();
		
		////// SHOW
		this.setVisible(true);
	}
	
	public Address waitAddress() {
		AddressWindow self = this;
		
		Thread thread_address_window = new Thread() {
			@Override public void run() { while(true) {
				self.mutex.lock();
				if(self.isAdressGiven()) {
					self.mutex.unlock();
					
					break;
				} else
					self.mutex.unlock();
			}}};
		thread_address_window.start();
		
		try {
			thread_address_window.join();
			
			this.mutex.lock();
			Address address = this.getAddress();
			this.mutex.unlock();
			
			this.setVisible(false);
			return address;
		
		} catch (InterruptedException error) { error.printStackTrace(); System.exit(1); }
		return null;
	}
	
	public static void main(String[] args) {
		AddressWindow address_window = new AddressWindow();
		Address address = address_window.waitAddress(); // blocks program
		
		System.out.println(address.ip);
		System.out.println(address.port);
	}

	////// WINDOW LISTENER
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

}
