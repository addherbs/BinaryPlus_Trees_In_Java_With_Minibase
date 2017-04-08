package com.thechat.chatmessenger.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {

	private List<ServerClient> clients = new ArrayList<ServerClient>();
	private List<Integer> clientResponse = new ArrayList<Integer>();

	private DatagramSocket socket;
	private int port;
	private Thread run, manage, send, receive;
	private boolean running = false;
	private int MAX_ATTEMPTS = 5;
	private boolean raw = false;

	/*
	 * The server constructor which initializes the socket at the respective
	 * port from the argument from the ServerMain
	 */
	public Server(int port) {
		this.port = port; // sets the current port to the input port
		try {
			socket = new DatagramSocket(port); // Sets the socket connection
		} catch (SocketException e) {
			e.printStackTrace(); // Might have error setting th socket. Like
									// already server is UP
			return;
		}

		run = new Thread(this, "Server"); // Creates the Thread of the server,
											// This is a multi-threaded sever
		run.start(); // Starts the thread
	}

	/*
	 * Overrides the run method from interface Runnable
	 */
	public void run() {
		System.out.println("Server is ready: " + port);
		System.out.println("Clients can connect now");
		running = true; // Runs the infinite loop with initializing the running
						// variable to true
		manageClients(); // Calls the manage Client method to send the
							// informational polling messages to each of the
							// clients
		receive(); // Receives the packet from the clients using this method
		Scanner sc = new Scanner(System.in);
		while (running) { // While True
			String text = sc.nextLine(); // Stores the message in the text
											// variable for further processing

			if (!text.startsWith("/")) { // If the message doesn't start with \
											// That means the message is from
											// the server console
				/*
				 * Sends the message to all of the clients
				 */
				sendMessageToAll("/m/Server Message: " + text + "/e/");
				continue;
			}
			/*
			 * this is when the message is from the server console and starts
			 * with /. That is this is a command
			 */
			text = text.substring(1); // Discards the \ from the server console

			/*
			 * Raw mode is when the server displays all the incoming traffic
			 * messages into the server console.Including the polling messages
			 */
			if (text.equals("raw")) {
				if (raw)
					System.out.println("Raw mode off");
				else
					System.out.println("Raw mode on");
				raw = !raw;
			} else

			/*
			 * This is when the server asks to displays the list of the clients
			 * that are avaible onilne
			 */
			if (text.equals("clients")) {
				System.out.println("Clients:");
				System.out.println("============");
				for (int i = 0; i < clients.size(); i++) { // Iterates through
															// all the clients
															// in the ArrayList
					ServerClient c = clients.get(i); // Stores the current
														// client in it
					System.out.println(c.name + "(" + c.getID() + "): "
							+ c.address.toString() + ":" + c.port);
				}
				System.out.println("============");
			} else

			/*
			 * If the server gives a kick command
			 */
			if (text.startsWith("kick")) {
				String name = text.split(" ")[1]; // Gets the name or the id
													// which server gave
				int id = -1; // sets initial id to -1
				boolean number = true; // this variable checks if the user gave
										// the ID or the Name
				try { // this try checks if the user gave
						// the ID or the Name
					id = Integer.parseInt(name); // stores the value of the 2ndd
													// unit in the int file
				} catch (NumberFormatException e) {
					number = false; // This is false if the 2nd argument is name
									// and it was not able to store in int file
				}
				if (number) { // If the server gave ID and not number
					boolean exists = false;
					for (int i = 0; i < clients.size(); i++) { // Iterates
																// through
						// all the clients
						// in the ArrayList
						if (clients.get(i).getID() == id) { // compares ID with
															// client ID
							exists = true;
							break;
						}
					}
					if (exists)
						disconnect(id, true); // Disconnects if the ID is
												// present
					else
						// Doesn't exits
						System.out.println("Client " + id
								+ " doesn't exist! Check ID number.");
				} else { // If the server gave the name as the input
					for (int i = 0; i < clients.size(); i++) {
						ServerClient currentClient = clients.get(i);
						if (name.equals(currentClient.name)) {
							disconnect(currentClient.getID(), true);
							break;
						}
					}
				}
			} else if (text.equals("help")) { // if the command was help
				help(); // Calls the help method
			} else if (text.equals("quit")) {// if the commad was quit

				quit(); // calls the quit method
			} else {
				System.out.println("Unknown Command:"); // any of the unknown
														// command
				help(); // calls the help method
			}
		}

	}

	/*
	 * Help method displays all the commands for server
	 */
	private void help() {
		System.out.println("Here is a list of all available commands:");
		System.out.println("=========================================");
		System.out.println("/raw - enables raw mode.");
		System.out.println("/clients - shows all connected clients.");
		System.out.println("/kick [users ID or username] - kicks a user.");
		System.out.println("/help - shows this help message.");
		System.out.println("/quit - shuts down the server.");
	}

	/*
	 * this is the method which sends the polling messages to all the clients
	 */
	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					sendMessageToAll("/i/server");
					sendStatus();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < clients.size(); i++) {// Iterates
						// through
						// all the clients
						// in the ArrayList

						ServerClient c = clients.get(i); // gets client
						if (!clientResponse.contains(c.getID())) { // Checks if
																	// the
																	// client
																	// responded
																	// to the
																	// polling
																	// message
							if (c.attempt >= MAX_ATTEMPTS) { // Tries the max
																// number of
																// times to run
								disconnect(c.getID(), false); // Disconnects the
																// client from
																// the
																// application
																// denoting the
																// client died
																// of the
																// Time-out
																// error
							} else {
								c.attempt++; // increase client attempt
							}
						} else {
							clientResponse.remove(new Integer(c.getID())); // Remove
																			// the
																			// client
							c.attempt = 0; // Set the attempt to 0
						}
					}
				}
			}
		};
		manage.start();// Starts the thread

	}

	/*
	 * Method quit is responsible to disconnect a client by calling the
	 * disconnect function This is invoked when server is shut and it has to
	 * disconnect all the clients that are active
	 */
	private void quit() {

		try {
			for (int i = 0; i < clients.size(); i++) { // gets all the clients
														// in a loop
				disconnect(clients.get(i).getID(), true); // disconnects them
															// saying it was a
															// proper
															// disconnection
			}
			running = false; // stops the thread
			socket.close(); // closes the socket
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * This is a method which is responsible for checking all the online clients
	 */
	private void sendStatus() {
		if (clients.size() <= 0) // no client present
			return;
		String users = "/u/";
		for (int i = 0; i < clients.size() - 1; i++) {
			users += clients.get(i).name + "/n/"; // gets all clients name
		}
		users += clients.get(clients.size() - 1).name + "/e/"; // gets the last
																// user
		sendMessageToAll(users); // Sends messages to all the users for the
									// online user list
	}

	/*
	 * Receives the datagram packet and sends it to the process method
	 */
	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {

					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data,
							data.length);
					try {
						socket.receive(packet);
					} catch (SocketException e1) {

					} catch (IOException e) {
						e.printStackTrace();
					}
					process(packet); // calls the process method and sends the
										// packet
				}
			}
		};
		receive.start(); // Starts the thread
	}

	/*
	 * Processes each incoming packet
	 */
	private void process(DatagramPacket packet) {
		try {
			String message = new String(packet.getData(), 0, packet.getLength()); // stores
																					// the
																					// data
																					// in
																					// string
																					// from
																					// the
																					// packet
			if (raw) // If server has raw enabled, displayes all the packet that
						// are coming
				System.out.println(message);
			if (message.startsWith("/c/")) { // if the packet is a connection
												// packet
				int id = UniqueID.getID(); // gets unique ID for the client
				// System.out.println("Identifier: " + id);
				// System.out.println(message.substring(3, message.length()));

				String name = message.split("/c/|/e/")[1];
				System.out.println(name + "(" + id + ") connected!");
				clients.add(new ServerClient(name, packet.getAddress(), packet
						.getPort(), id)); // Adds the client in the clients
											// array list
				String ID = "/c/" + id;
				send(ID, packet.getAddress(), packet.getPort()); // calls the
																	// send
																	// method to
																	// send the
																	// packet
			} else if (message.startsWith("/m/")) {
				// If it is a user message it will send it to sendmessagesto all
				// method
				sendMessageToAll(message);
			} else if (message.startsWith("/d/")) {
				// if it is a disconnect message, will call disconnect method
				String id = message.split("/d/|/e/")[1];
				disconnect(Integer.parseInt(id), true);
			} else if (message.startsWith("/i/")) {
				// if it is a info message it will set the client response with
				// the message
				clientResponse
						.add(Integer.parseInt(message.split("/i/|/e/")[1]));
			} else {
				System.out.println(message);
			}
		} catch (Exception e) {
			System.out.println("");
		}

	}

	/*
	 * Disconnects the client status says if it was a proper disconnection or a
	 * timeout one
	 */
	private void disconnect(int id, boolean status) {
		ServerClient removeClient = null;
		try {

			boolean existed = false;
			for (int i = 0; i < clients.size(); i++) { // gets all the clients
														// in loop
				if (clients.get(i).getID() == id) { // if the clcient matches
					removeClient = clients.get(i); // gets the details of the
													// client
					clients.remove(i); // removes the client from the arrayList
					existed = true; // sets existed to true
					break;
				}
			}

			if (!existed) { // if the user was there
				String message = null;
				if (status)
					message = "Client: " + removeClient.name + " ("
							+ removeClient.getID() + ") Address: "
							+ removeClient.address.toString() + ":"
							+ removeClient.port + " disconnected!";

				else
					// If the user was not there and the disconnect request was
					// sent
					message = "Client: " + removeClient.name + " ("
							+ removeClient.getID() + ") Address: "
							+ removeClient.address.toString() + ":"
							+ removeClient.port + " timed out!";

				System.out.println(message); // Prints the message
			}
		} catch (Exception e) {
			System.out.println("Client: " + removeClient.name + "Kicked");
		}
	}

	/*
	 * Sends the message as a packet to the client
	 */
	private void send(final byte[] data, final InetAddress address,
			final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length,
						address, port); // initializes the packet
				try {
					socket.send(packet); // sends the packet
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start(); // starts the thread
	}

	/*
	 * Initializes the the message with /e/
	 */
	private void send(String message, InetAddress address, int port) {
		message += "/e/";
		send(message.getBytes(), address, port);
	}

	/*
	 * This method is responsible to send data to all the clients or the
	 * selected client this is the main functional method of the application
	 */
	private void sendMessageToAll(String message) {

		String messageToSend = null;
		int flag;
		flag = 0;
		byte[] utf8Bytes = null;
		ServerClient client;
		String pqr, toSend, sendersName, finalMessage;
		pqr = null;
		boolean userFlag = message.contains("-");
		String newMessage[] = null;
		String date = null;
		int superFlag = 0;

		// if (message.startsWith("/m/Server")) {
		// superFlag = 1;
		// for (int i = 0; i < clients.size(); i++) {
		// client = clients.get(i);
		// send(message.getBytes(), client.address, client.port);
		// }
		// } else {

		try {
			if (message.startsWith("/m/")) { // If it is a user message
				try {
					utf8Bytes = message.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				newMessage = message.split("="); // separates the HTTP header
													// form the message
				message = "/m/" + newMessage[1]; // prepends /m/ to the message
				date = newMessage[0].substring(3); // gets the date
				// System.out.println(date + " ===== " + message);
			}
			if (message.startsWith("/m/")) { // if it was a server message

				/*
				 * Normalizes the message to the working format of the
				 * application
				 */
				pqr = message.substring(3);
				pqr = pqr.split("/e/")[0];

				/*
				 * If it was a private message, the processing is done here
				 * Userflag == true for private message
				 */

				if (userFlag) {

					sendersName = message.split(":")[0].trim().substring(3)
							.trim();
					toSend = pqr.split("-")[0];
					toSend = toSend.split(":")[1].trim();
					finalMessage = message.split("-")[1].trim().split("/e/")[0]
							.trim();
					// System.out.println(sendersName + "(space)" + toSend);

					for (int i = 0; i < clients.size(); i++) { // gets all
																// clients
						client = clients.get(i);
						if (toSend.equals(client.name)) // checks if the
														// recipient is not
														// present
							flag = 1;

					}

					if (flag == 1) { // If recipient is present

						message = sendersName + "--->" + toSend + ": "
								+ finalMessage;

						System.out.println(date + "  Message'" + message + "' Size: "
								+ utf8Bytes.length); // Prints on the console

						message = "/m/(Private Chat)" + sendersName + "--->"
								+ toSend + ": " + finalMessage + "/e/";
						messageToSend = "/m/" + date + "=" + message;
						for (int i = 0; i < clients.size(); i++) { // gets all
																	// clients
							client = clients.get(i); // if client matches
							if (toSend.equals(client.name) // if recipient
															// matches a client
															// or sender matches
															// a client,
									// This is done to send messages to the
									// sender and the receiver
									|| sendersName.equals(client.name)) {
								send(messageToSend.getBytes(), client.address,
										client.port); // sends the messages
							}
						}
					} else {
						// No such receiver found, error
						System.out.println("\n===========================");
						System.out.println("No such user");
						System.out.println(sendersName
								+ " must be a smurf user");
						System.out
								.println("to know how to kick him, type: /help");
						System.out.println("===========================");
					}

				} else {
					/*
					 * It is a broadcast message it will send to all the client
					 */

					System.out.println(date + "  Message'" + pqr + "' Size: "
							+ utf8Bytes.length); // prints the message on the
													// server console
					messageToSend = "/m/" + date + "=" + message; // inits the
																	// sending
																	// message
					// System.out.println(message);
					for (int i = 0; i < clients.size(); i++) { // gets all
																// clients
						client = clients.get(i); // gets current client
						send(messageToSend.getBytes(), client.address,
								client.port); // sends message to the client
					}
				}
			} else { // if not a user message, send message to all the client
				for (int i = 0; i < clients.size(); i++) {
					client = clients.get(i);
					// System.out.println(client.name);
					send(message.getBytes(), client.address, client.port);
				}
			}
		} catch (Exception e) {
			System.out.print("");
		}

	}
}
