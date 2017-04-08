package com.thechat.chatmessenger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

public class ClientWindow extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textMessage;
	private JTextArea history;
	private DefaultCaret caret;
	private Thread listen, run;
	private Client client;

	private boolean running = false;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;

	private OnlineUsers users;

	/*
	 * This constructor accepts 3 parameters name, address and port It creates
	 * Client Window for each of the client. Every client has a unique and
	 * different Client Window
	 */
	public ClientWindow(String name, String address, int port) {
		setTitle("Chat Application"); // It sets the window name to the current
										// string

		client = new Client(name, address, port); // It creates a Client Class
													// object for each client
													// Which passes 3 parameters
													// for unique identfication

		boolean connectionFlag = client.tryConnection(address); // It tries to
																// connect with
																// the socket
		/*
		 * If the connecionFlag returns False, It says conn failed
		 */
		if (!connectionFlag) {
			System.err.println("Connection Failed");
			console("Connection Failed");
		}

		createWindow(); // This generates the Client Window for the respective
						// client
		console("Attempting a connection to " + address + ":" + port
				+ ", User: " + name); // Prints the message to the console
		String connection = "/c/" + name + "/e/"; // It prepends and appends the
													// respective message data.

		client.send(connection.getBytes()); // It sends connection
											// packet to the sever for
											// connection
		users = new OnlineUsers(); // Creates Online users object

		running = true; // Sets running equals to true, so that our Infinite
						// loop will start running and will send a accept
						// packets
		run = new Thread(this, "Running"); // Creates current classes thread
		run.start(); // Starts the thread

	}

	/*
	 * This method is used to call the listen function which in turn will start
	 * listening for any packets at the port from the socket
	 */
	public void run() {
		listen();
	}

	/*
	 * It appends the incoming message into the history field of the respective
	 * client. It keeps a track of the history messages for user
	 */
	public void console(String message) {
		history.append(message + "\n\r");
	}

	/*
	 * This function calls the current clients respective send method to send
	 * the data from the socket This method basically initializes the user
	 * messages with the application message format
	 */
	private void send(String message, boolean text) {

		if (message.equals("")) // It returns if the user sends empty message
			return;
		if (text) { // If the message is not empty
			message = client.getName() + ": " + message; // This message
															// prepends name to
															// the message, this
															// is the sends name
															// which it
															// prepends

			// console(message);
			textMessage.setText(""); // It sets the textMessage which is the
										// history field to empty after the text
										// has been delivered

			/*
			 * It stores the date from the HTTP Field in the RFC 1123
			 * international format
			 */
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss z");
			String currentTime = dateFormat.format(calendar.getTime());

			message = currentTime + "=" + message; // It appends the HTTP header
													// to the message, and
													// Separates them with =
													// sign so that the sever
													// can differentiate if tom
													// the message
			/*
			 * It appends and prepends the application logic message to the
			 * string message. /m/ is the initializes for user message which
			 * stands for message. /e/ is the end for ever message which stands
			 * for End of the message.
			 */
			message = "/m/" + message + "/e/";

		}
		client.send(message.getBytes()); // It sends the message to the Client
											// class which actually sends the
											// message form the socket to the
											// server.

	}

	/*
	 * It is the method which accepts packet from the sever in the
	 * DatagramSocket object and the processes it
	 */
	private void listen() {
		listen = new Thread("Listen") { // Creates Thread named Listen

			public void run() {

				while (running) { // It checks for every client at every instant
									// that if the socket has received a packet
									// from the server
					String message = client.receive(); // Stores the message
														// into a string and the
														// processes it
					// console(message);

					/*
					 * /c/ stands for connection messages, which is from the
					 * server. It sets a respective ID for every client and
					 * prints the respective into the clients console
					 */
					if (message.startsWith("/c/")) {
						client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
						console("Successfully connected to server!! ID: "
								+ client.getID());
					} else

					/*
					 * /m/ stands for message. This is when the server redirects
					 * a packet which it received from a client and then
					 * processes it
					 */
					if (message.startsWith("/m/")) {

						String newMessage[] = null;
						// String date = null;

						if (message.startsWith("/m/Server")) { // This is when
																// server sends
																// a message to
																// all the user
							String text = message.substring(3);
							text = text.split("/e/")[0];
							console(text);
						} else { // It is when a user sends a message packet to
									// the server and then server redirects it
							newMessage = message.split("="); // Splits the HTTP
																// header format
																// with the
																// String
																// message

							/*
							 * Processes the message. removes additional headers
							 * from it and extract the proper message
							 */
							message = newMessage[1];
							// date = newMessage[0].substring(3);
							String text = message.substring(3);
							text = text.split("/e/")[0];

							console(text); // Prints the message to the console
						}
					} else

					/*
					 * /i/ stands for informational messages. These are the
					 * polling messages that the server sends to each client at
					 * every moment, to check if the client has been
					 * disconnected of timed out.
					 */
					if (message.startsWith("/i/")) {
						String text = "/i/" + client.getID() + "/e/";
						send(text, false);
					} else

					/*
					 * /u/ stands for User message. This keeps the online user
					 * list updated at every moment
					 */
					if (message.startsWith("/u/")) {
						String[] u = message.split("/u/|/n/|/e/");
						users.update(Arrays.copyOfRange(u, 1, u.length - 1)); // Updates
																				// the
																				// online
																				// users
																				// list
					}
				}

			}
		};
		listen.start(); // Starts the listen thread for ever client
	}

	/*
	 * This creats the GUI for the Client Window
	 */
	private void createWindow() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(880, 550);
			// setBounds(100, 100, 450, 300);
			setLocationRelativeTo(null);

			menuBar = new JMenuBar(); // Sets Menu Bar
			setJMenuBar(menuBar);

			mnFile = new JMenu("File"); // Names Menu Bar as File
			menuBar.add(mnFile);

			mntmOnlineUsers = new JMenuItem("Online Users"); // Creates a menu
																// item named
																// online user.
																// Adds action
																// listener to
																// it which
																// displays list
																// of online
																// users
			mntmOnlineUsers.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					users.setVisible(true);
				}
			});
			mnFile.add(mntmOnlineUsers);

			mntmExit = new JMenuItem("Exit"); // Creates a menu item named Exit
												// which Exits the Client Window
												// on Exit
			mnFile.add(mntmExit);

			/*
			 * Creates JPanel which is the scroll plane for the client windwow
			 */
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);

			GridBagLayout gbl_contentPane = new GridBagLayout();
			gbl_contentPane.columnWidths = new int[] { 28, 815, 30, 7 };
			gbl_contentPane.rowHeights = new int[] { 25, 485, 40 };

			contentPane.setLayout(gbl_contentPane);

			/*
			 * Creats a JTextArea for storing the History messages of the Client
			 */
			history = new JTextArea();
			history.setEditable(false);
			JScrollPane scroll = new JScrollPane(history);
			caret = (DefaultCaret) history.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			GridBagConstraints scrollConstraints = new GridBagConstraints();
			scrollConstraints.insets = new Insets(0, 0, 5, 5);
			scrollConstraints.fill = GridBagConstraints.BOTH;
			scrollConstraints.gridx = 0;
			scrollConstraints.gridy = 0;
			scrollConstraints.weightx = 1;
			scrollConstraints.weighty = 1;
			scrollConstraints.gridwidth = 3;
			scrollConstraints.gridheight = 2;
			scrollConstraints.insets = new Insets(0, 4, 0, 0);
			contentPane.add(scroll, scrollConstraints);

			/*
			 * Creates TextField for typing message for the client
			 */
			textMessage = new JTextField();
			textMessage.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
						send(textMessage.getText(), true);
				}
			});

			GridBagConstraints gbc_textMessage = new GridBagConstraints();
			gbc_textMessage.insets = new Insets(0, 0, 0, 5);
			gbc_textMessage.fill = GridBagConstraints.HORIZONTAL;
			gbc_textMessage.gridx = 0;
			gbc_textMessage.gridy = 2;
			gbc_textMessage.weightx = 1;
			gbc_textMessage.weighty = 0;
			gbc_textMessage.gridwidth = 2;
			contentPane.add(textMessage, gbc_textMessage);
			textMessage.setColumns(10);

			/*
			 * Creates a button to send message
			 */
			JButton btnSend = new JButton("Send");
			btnSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					send(textMessage.getText(), true);
				}
			});

			GridBagConstraints gbc_btnSend = new GridBagConstraints();
			gbc_btnSend.insets = new Insets(0, 0, 0, 5);
			gbc_btnSend.gridx = 2;
			gbc_btnSend.gridy = 2;
			gbc_btnSend.weightx = 0;
			gbc_btnSend.weighty = 0;
			contentPane.add(btnSend, gbc_btnSend);

			/*
			 * This is the Window Listener It gets activated if a user presses
			 * the 'X' button to end the client session and closes the client
			 * window
			 */
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					String disconnect = "/d/" + client.getID() + "/e/";
					send(disconnect, false);
					running = false;
					client.close();
				}
			});
			setVisible(true);
			textMessage.requestFocusInWindow();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
