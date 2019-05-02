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
	
	private Socket socket = null;
	private String nickname = null;
	private List<Writer> listWriters = null;
	private BufferedReader br = null;
	private PrintWriter pw = null;
	
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
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));

			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);

			while (true) {
				// 2. 데이터 읽기
				String data = br.readLine();
				if (data == null) {
					log("클라이언트로 부터 연결 끊김");
					doQuit();
					break;
				}
				log(" reveived : " + data);

				// 3.프로토콜 분석
				String[] tokens = data.split(":");
				
				
				if ("join".equals(tokens[0])) {
					doJoin(tokens[1]);
				} else if ("message".equals(tokens[0])) {
					
					int lng = tokens.length-1;
					String[] message = new String[lng];
					System.arraycopy( tokens, 1, message, 0, lng );
					String msgWithoutProtocol = String.join( ":" , message );		
					doMessage(msgWithoutProtocol);
					
				} else if ("quit".equals(tokens[0])) {
					doQuit();
					break;
				} else {
				}

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

	private void doJoin(String nickname) {
		this.nickname = nickname;
		
		String data = "message:" + nickname + " 님이 참여하였습니다.";
		
		/* writer pool 에 저장 */
		addWriter( pw );
		
		broadcast(data);
		
		//ack
		pw.println("join:ok");
		pw.flush();
		
	}
	private void addWriter(Writer pr) {
		synchronized( listWriters ) {
			listWriters.add( pr );
		}
	}
	private void broadcast ( String data) {
		synchronized( listWriters ) {
			for( Writer writer : listWriters ) {
				PrintWriter otherPw = (PrintWriter) writer ;
				if( otherPw != pw) {
					otherPw.println( data );
				}
			}
		}
	}
	
	private void doMessage(String token) {
		broadcast("message:" + nickname + " : " + token );
	}

	private void doQuit() {
		removeWriter();
		
		String data = "message:" + nickname + " 님이 퇴장 하였습니다.";
		
		broadcast( data );
		
	}
	
	private void removeWriter() {
		//System.out.println(listWriters.indexOf((Writer)pw));
		synchronized( listWriters ) {
			listWriters.removeIf( wr -> ( PrintWriter ) wr == pw );
		}
		//System.out.println(listWriters.indexOf((Writer)pw));
	}
	private void log( String log ) {
		System.out.println( "[server] " + log );
	}
}
