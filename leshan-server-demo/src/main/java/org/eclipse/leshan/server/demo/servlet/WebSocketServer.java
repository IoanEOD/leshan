package org.eclipse.leshan.server.demo.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WebSocketServer {

	private ServerSocket serverSocket;
	private Socket socket;

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


class ServerThread extends Thread{  

	String line = null;
	BufferedReader inputStream = null;
	PrintWriter outputStream = null;
	Socket socket = null;

	public ServerThread(Socket socket){
		this.socket = socket;
	}

	public void run() {
		try{
			inputStream= new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream=new PrintWriter(socket.getOutputStream());

		}catch(IOException e){
			System.out.println("IO error in server thread");
		}
		while (true) {
			try {
				line = inputStream.readLine();
				if ((line == null) || line.equalsIgnoreCase("QUIT")) {
					socket.close();
					return;
				} else {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}


