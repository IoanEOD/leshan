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
	
	public void WebsSocketServer() throws IOException {
		serverSocket = new ServerSocket(1234);
		Socket socket = serverSocket.accept();
		
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
		
		System.out.println(input.read());
	}
	
	public void close() throws IOException {
		serverSocket.close();
	}

}