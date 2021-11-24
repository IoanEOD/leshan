package org.eclipse.leshan.server.demo.servlet;


import java.net.*;
import java.io.*;


public class WebSocketClient {
    Socket socket;

    public WebSocketClient(String address, int port) throws IOException {
        this.socket = new Socket(address,port);
    }


    public void sendIPAddress() throws IOException {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            String localHost = InetAddress.getLocalHost().toString();
            writer.println(localHost.split("/")[1]);
        }

    public String getInput() throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }
}



