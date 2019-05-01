package com.cafe24.network.chat.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.cafe24.network.chat.server.ChatWindow;

public class ChatClientApp {
	private static final String SERVER_IP = "192.168.1.24";
	private static final int SERVER_PORT = 7000;
	private static ChatWindow chat = null;
	public static void main(String[] args) {
		
		String name = null;
		
		Scanner scanner = new Scanner(System.in);
		
		Socket socket = null;
		
		while (true) {

			System.out.println("대화명을 입력하세요.");
			
			System.out.print(">>> ");
			
			name = scanner.nextLine();

			if (name.isEmpty() == false) {
				break;
			}

			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}

		scanner.close();
		
		try {
			
			//1. 소켓 만들고
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			System.out.println("[client] connected");
			
			//2. IOSream 받아오기
//			InputStream is = socket.getInputStream();
//			OutputStream os = socket.getOutputStream();
			
			
			new ChatWindow(name, socket).show();
			
			
			// 2. IOStream 받아오기
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);

			
			// 3. join 성공 
			String data = "join:" + name + "\r\n";
			pr.println(data);
			pr.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 2. iostream
		// 3. join 성공
		// 4.

		
	}
	
	private static void log(String log) {
		System.out.println("[client] :" + log);
	}
	
	

}