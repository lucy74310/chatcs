package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ChatServerThread extends Thread {
	
	private static Socket socket = null;
	private static String nickname = null;
	private static List<Writer> listWriters = null;
	
	
	public ChatServerThread(Socket socket,List<Writer> listWriters) {
		this.socket = socket;
		this.listWriters = listWriters;
	}

	@Override
	public void run() {
		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

		// inetRemoteSocketAddress.getAddress() => inetAddress
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
		int remoteHostPort = inetRemoteSocketAddress.getPort();

		System.out.println("[server] connected by client[" + remoteHostAddress + ":" + remoteHostPort + "]");
		try {

			// 1. IOStream 받아오기
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));

			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);

			while (true) {
				// 2. 데이터 읽기
				String data = br.readLine();
				if (data == null) {
					log("클라이언트로 부터 연결 끊김");
					doQuit( pr );
					break;
				}
				System.out.println("[server] received:" + data);

				// 3.프로토콜 분석
				String[] tokens = data.split(":");
				if ("join".equals(tokens[0])) {
					doJoin(tokens[1], pr);
				} else if ("message".equals(tokens[0])) {
					doMessage(tokens[1]);
				} else if ("quit".equals(tokens[0])) {
					doQuit(pr);
				} else {
					log("알 수 없는 요청 (" + tokens[0] + ")");
				}

				// 6. 데이터 쓰기
				pr.println(data);

			}

		} catch (SocketException e) {
			System.out.println("[server] sudden cloased by client");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 두번 닫히면 에러남
				if (socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {

			}
		}
	}

	private void doJoin(String nickname, PrintWriter pr) {
		this.nickname = nickname;
		
		String data = nickname + "님이 참여하였습니다.";
		broadcast(data);
		
		/* writer pool 에 저장 */
		addWriter( pr );
		
		
		//ack
		pr.println("join:ok");
		pr.flush();
	}
	private void addWriter(Writer pr) {
		synchronized( listWriters ) {
			listWriters.add( pr );
		}
	}
	private void broadcast ( String data ) {
		synchronized( listWriters ) {
			for( Writer writer : listWriters ) {
				PrintWriter pr = (PrintWriter) writer ;
				pr.println( data );
				pr.flush();
			}
			
		}
	}
	private void doMessage(String token) {
		broadcast( nickname + ":" + token );
	}

	private void doQuit( Writer writer ) {
		removeWriter( writer );
		
		String data = nickname + "님이 퇴장 하였습니다.";
		
		broadcast( data );

	}
	
	private void removeWriter( Writer writer ) {
		int i = 0;
		synchronized( listWriters ) {
			for( Writer wr : listWriters ) {
				if ( writer == wr ) {
					listWriters.remove( i );
				}
				i++;
			}
		}
	}
	private void log( String log ) {
		System.out.println( "[server] :" + log );

	}
}
