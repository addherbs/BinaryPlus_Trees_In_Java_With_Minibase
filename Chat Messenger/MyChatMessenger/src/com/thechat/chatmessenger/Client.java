package com.thechat.chatmessenger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	private String name, address;
	private int port;
	private DatagramSocket socket;
	private InetAddress ip;
	private int ID = -1;

	private Thread send;

	/*
	 * It creates a unique client for every login Sets the name, address and
	 * port which was input for the login field
	 */
	public Client(String name, String address, int port) {
		this.name = name;
		this.address = address;
		this.port = port;
	}

	/*
	 * Returns name of the current user
	 */
	public String getName() {
		return name;
	}

	/*
	 * Returns address of the current user
	 */
	public String getAddress() {
		return address;
	}

	/*
	 * Returns port of the current user
	 */
	public int getPort() {
		return port;
	}

	/*
	 * Sets the ID for the current user
	 */
	public void setID(int ID) {
		this.ID = ID;
	}

	/*
	 * Returns ID of the current user
	 */
	public int getID() {
		return ID;

	}

	/*
	 * This method tries to connect with the respective InetAddress And creates
	 * a new DattagramSocket address space
	 */
	public boolean tryConnection(String address) {
		try {
			socket = new DatagramSocket(); // Creates a socket of the class
											// DatagramSocket
			ip = InetAddress.getByName(address); // Initializes the ip variable
													// with the current ip
													// address
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false; // Returns false if the socket didn't create
		} catch (SocketException e) {
			e.printStackTrace();
			return false; // Returns false if the socket didn't create
		}
		return true; // Returns true when the connection is placed
	}

	/*
	 * this method receives the packet which is of the form DatagramSocket for
	 * every client
	 */
	public String receive() {

		byte data[] = new byte[1024]; // Creates byte array to get the input
										// data in bytes and store in the packet
										// of DatagramSocket type
		DatagramPacket packet = new DatagramPacket(data, data.length); // Creates
																		// the
																		// packet
																		// of
																		// the
																		// byte
																		// array
		try {
			socket.receive(packet); // Receive the Actual data from the socket
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData()); // Converts the packet
														// data into string
		return message; // Returns the message to the caller method
	}

	/*
	 * This closes the socket connection for a respective user
	 */
	public void close() {
		try {
			new Thread() {
				public void run() {
					synchronized (socket) {
						socket.close(); // Closes the socket

					}
				}
			}.start(); // This starts the thread
		} catch (Exception e) {
			System.out.println("");
		}
	}

	/*
	 * This method creates a new Thread as Send to send whenever we send a data
	 * to a client It sends the data to the respective socket IP and Port
	 */
	public void send(final byte[] data) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length,
						ip, port); // This creates a datagram packet object and
									// stuffs the message, its length, ip and
									// port into the packet
				try {
					socket.send(packet); // This sends the packet frrom the
											// socket
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		send.start(); // This starts the thread
	}

}
