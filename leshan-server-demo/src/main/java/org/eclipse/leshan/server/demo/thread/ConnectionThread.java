package org.eclipse.leshan.server.demo.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.demo.websocket.WebSocketServer;
import org.eclipse.leshan.server.registration.RegistrationHandler;

public class ConnectionThread extends Thread {

    private WebSocketServer webSocketServer;

    private RegistrationHandler registrationHandler;

    private LeshanServer server;


    public ConnectionThread(WebSocketServer webSocketServer, RegistrationHandler registrationHandler, LeshanServer server) {
        this.webSocketServer = webSocketServer;
        this.registrationHandler = registrationHandler;
        this.server = server;
    }


    @Override
    public void run() {
        try {
            Socket socket = null;
            ServerSocket serverSocket=null;
            System.out.println("Server Listening..");
            serverSocket = new ServerSocket(4999);
            //TODO: Close socket
            while(true){
                try{
                    socket = serverSocket.accept();
                    System.out.println("connection Established");
                    ServerThread serverThread = new ServerThread(socket, registrationHandler, server);
                    webSocketServer.addServerThread(serverThread);
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
}