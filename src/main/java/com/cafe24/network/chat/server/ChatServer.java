package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer{
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Socket socket = null;
		List<Writer> listWriters = null;
		
		
		try {
			//1.서버소켓 생성
			serverSocket = new ServerSocket();
			
			//1-1. Time-Wait 시간에 소켓에 포트번호 할당을 가능하게 하기 위해
			serverSocket.setReuseAddress( true );
			
			//2.바인딩(binding)
			//	: Socket에 SocketAddress(IPAddress + Port)를 바인딩 한다.
			serverSocket.bind(new InetSocketAddress("0.0.0.0", 7000));
			
			listWriters = new ArrayList<Writer>();
			log("대기중...");
			
			
			while(true) {
				//	: 클라이언트의 연결요청을 기다린다.
				socket = serverSocket.accept(); //blocking 
				
				new ChatServerThread(socket, listWriters).start();
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				//두번 닫히면 에러남
				if(serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static void log( String log ) {
		System.out.println( "[server] :" + log );

	}
}
