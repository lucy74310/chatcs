package com.cafe24.network.chat.server;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	
	private String name;
	private Socket socket;
	private PrintWriter pw;
	public ChatWindow(String name, Socket socket) {
		this.name = name;
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);

		this.socket = socket;
		try {
			this.pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
		} catch (IOException e) {
			System.out.println("[Error]pw exception : "+ e);
		}

		new ChatClientReceiveThread(socket).start();

	}

	public void finish() {
		// socket 정리
		String request = "quit\r\n";
		pw.println(request);
		
		System.exit(0);

	}

	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}

		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// System.exit(0);
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();

		// thread 생성
	}

	private void updateTextArea(String message) {
		textArea.append(message);
	}

	private void sendMessage() {
		String message = name + ">>" + textField.getText();
		
		// pw.println("MSG "+ message);

		textField.setText("");
		textField.requestFocus();

		// test
		updateTextArea(message);
		
		pw.println("message:" + message);
		
	}

	private class ChatClientReceiveThread extends Thread {
		Socket socket = null;

		ChatClientReceiveThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				while (true) {
					String msg = br.readLine();
					if(msg == null) {
						break;
					}
					updateTextArea(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}