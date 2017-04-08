package com.thechat.chatmessenger.server;

/*
 * The main class which starts the Server
 */
public class ServerMain {

	private int port;
	private Server server;

	/*
	 * The constructor which initializes the port and the address of the server
	 */
	ServerMain(int port) {
		this.port = port;
		server = new Server(port); // Creates Server object at the specified
									// port
	}

	public static void main(String[] args) { // gets the input argument from the
												// User in the eclipse compiler
		int port;
		if (args.length != 1) { // If no argument specified, returns server
								// error
			System.out.println("Error, insert port");
			return;
		}
		port = Integer.parseInt(args[0]); // initializes the port variable with
											// the argument
		new ServerMain(port);// Calls the constructor of the ServerMain class to
								// initialize the server
	}

}