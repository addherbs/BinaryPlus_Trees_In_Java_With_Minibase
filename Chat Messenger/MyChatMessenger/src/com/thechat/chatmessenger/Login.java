package com.thechat.chatmessenger;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField nameField;
	private JTextField addField;
	private JTextField portField;

	/*
	 * This Login function helps us develop the GUI for the Login Form It has 3
	 * JTextField and 1 button
	 */
	public Login() {

		try {
			/*
			 * Sets the UI of the application to the running Operaing System
			 */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/*
		 * This creates the name JTextField
		 */
		nameField = new JTextField();
		nameField.setBounds(79, 81, 135, 20);
		contentPane.add(nameField);
		nameField.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(128, 61, 38, 14);
		contentPane.add(lblName);

		JLabel lblIpAddress = new JLabel("IP Address:");
		lblIpAddress.setBounds(117, 141, 60, 14);
		contentPane.add(lblIpAddress);

		/*
		 * This creates the address JTextField
		 */
		addField = new JTextField();
		addField.setColumns(10);
		addField.setBounds(79, 160, 135, 20);
		contentPane.add(addField);

		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(128, 208, 38, 14);
		contentPane.add(lblPort);

		/*
		 * This creates the port JTextField
		 */
		portField = new JTextField();
		portField.setColumns(10);
		portField.setBounds(79, 228, 135, 20);
		contentPane.add(portField);

		/*
		 * This creates the submit JButton
		 */
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String name = nameField.getText(); // Gets the input name form
													// the Login Form
				String address = addField.getText(); // Gets the input address
														// form the Login Form
				int port = Integer.parseInt(portField.getText());// Gets the
																	// input
																	// port
																	// form the
																	// Login
																	// Form and
																	// wraps it
																	// with
																	// Integer

				login(name, address, port); // This calls the private void login
											// function
											// And passes the inpput name,
											// address, and port

			}
		});
		btnLogin.setBounds(102, 303, 89, 23);
		contentPane.add(btnLogin);
	}

	/*
	 * This function accepts 3 paraments name, address, port It calls the
	 * ClientWindow Class and creates a address space for that It then passes
	 * the three paraments to the ClientWindow class
	 */
	private void login(String name, String address, int port) {
		dispose();
		new ClientWindow(name, address, port);
	}

	/*
	 * This is the main method which calls the Login constructor and develops
	 * the GUI
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login(); // create frame object of Login
												// and calls the constructor
					frame.setVisible(true); // //Sets visible to the frame
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}