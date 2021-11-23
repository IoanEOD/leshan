package org.eclipse.leshan.server.demo.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WebSocketServer {
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	public WebSocketServer() throws IOException {
		serverSocket = new ServerSocket(4999);
		Thread connectionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socket = serverSocket.accept();
			        InputStream input = socket.getInputStream();
			        OutputStream output = socket.getOutputStream();
					
					System.out.println(input.read());
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