package org.eclipse.leshan.server.demo.servlet;


import java.net.*;
import java.io.*;

import org.eclipse.leshan.core.response.ReadResponse;

public class WebSocketClient {
    String endpoint;
    Socket socket;
    InetAddress ip;



    public WebSocketClient() throws IOException {
//        this.endpoint = endpoint;
        this.socket = new Socket("localhost",1234);
    }


    public void sendData() throws IOException {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
//            String clientinfo = this.endpoint + " " + data;
            ip = InetAddress.getLocalHost();
            writer.println(ip);
        }

    public String getInput() throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }
}



