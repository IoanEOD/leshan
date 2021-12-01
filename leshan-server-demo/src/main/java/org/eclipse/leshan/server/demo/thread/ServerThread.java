package org.eclipse.leshan.server.demo.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

enum RecieveModes {
	ip, idle
}

public class ServerThread extends Thread{  

	public String clientIP;

	String line = null;
	BufferedReader inputStream = null;
	PrintWriter outputStream = null;
	Socket socket = null;

	RecieveModes recieveMode = RecieveModes.ip;

	public ServerThread(Socket socket){
		this.socket = socket;
	}

	public void run() {
		try{
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream =new PrintWriter(socket.getOutputStream());

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
					switch(recieveMode) {
						case idle:
						  break;
						case ip:
							clientIP = line;
						  break;
						default:
					  }
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}


