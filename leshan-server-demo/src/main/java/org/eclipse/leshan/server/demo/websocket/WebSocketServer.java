package org.eclipse.leshan.server.demo.websocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.eclipse.leshan.server.demo.thread.ServerThread;

public class WebSocketServer {

	private ServerSocket serverSocket;
	private Socket socket;
	private ArrayList<ServerThread> serverThreads = new ArrayList<>();

	public WebSocketServer() throws IOException {
		Thread connectionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Socket socket=null;
					ServerSocket serverSocket=null;
					System.out.println("Server Listening..");
					serverSocket = new ServerSocket(4999);
					//TODO: Close socket
					while(true){
						try{
							socket= serverSocket.accept();
							System.out.println("connection Established");
							ServerThread serverThread = new ServerThread(socket);
							serverThreads.add(serverThread);
							serverThread.start();
						}

						catch(Exception e){
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		connectionThread.start();
	}

	public void close() throws IOException {
		serverSocket.close();
	}
}
