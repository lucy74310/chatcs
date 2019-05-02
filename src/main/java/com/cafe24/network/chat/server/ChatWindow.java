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
	
	private String nickname;
	private BufferedReader br;
	private PrintWriter pw;
	private Thread receiveThread;
	
	public ChatWindow(String nickname, Socket socket) {
		this.nickname = nickname;
		frame = new Frame(nickname);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);

		try {
			
			//2. IOStream 받아오기
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			this.pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
			
			// 3. join 보내기 
			String data = "join:" + nickname + "\r\n";
			pw.println(data);
			
		} catch (IOException e) {
			System.out.println("[Error]pw exception : "+ e);
		}

		receiveThread = new ChatClientReceiveThread();
		receiveThread.start();

	}

	public void finish() {
		// socket 정리
		pw.println("quit");
		try {
			receiveThread.join();
		} catch (InterruptedException e) {
			log("receiveThread 종료 중 error " + e);
		}
		
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
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
		
	}

	private void updateTextArea(String message) {
		textArea.append(message + "\r\n");
	}

	private void sendMessage() {
		String message =  textField.getText();
		
		pw.println( "message:" + message );

		textField.setText("");
		textField.requestFocus();

		updateTextArea(nickname + " (나) : " + message);
	}

	private class ChatClientReceiveThread extends Thread {

		public void run() {
			try {
				while (true) {
					String msg = br.readLine();
					if(msg == null) {
						break;
					}
					
					// 프로토콜 분석
					String[] tokens = msg.split(":");
					if( "join".equals( tokens[0] ) ) {
						if( "ok".equals( tokens[1] ) ) {
							updateTextArea( "채팅방에 입장하셨습니다. ");
						}
					} else if ("message".equals(tokens[0])) {
						int lng = tokens.length-1;
						String[] message = new String[lng];
						System.arraycopy( tokens, 1, message, 0, lng );
						String msgWithoutProtocol = String.join( ":" , message );		
						updateTextArea(msgWithoutProtocol);
					} else {
						log("알 수 없는 요청 (" + msg + ")");
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log ( String log ) {
		System.out.println("[client] : " + log);
	}

}