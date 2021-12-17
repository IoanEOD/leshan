package org.eclipse.leshan.mtserver.demo.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.leshan.mtserver.demo.websocket.WebSocketCloud;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.RegistrationHandler;

public class ConnectionThread extends Thread {

    private WebSocketCloud webSocketServer;

    private RegistrationHandler registrationHandler;

    private LeshanServer server;


    public ConnectionThread(WebSocketCloud webSocketServer, RegistrationHandler registrationHandler, LeshanServer server) {
        this.webSocketServer = webSocketServer;
        this.registrationHandler = registrationHandler;
        this.server = server;
    }


    @Override
    public void run() {
        try {
            Socket socket = null;
            ServerSocket serverSocket=null;
            serverSocket = new ServerSocket(4999);
            //TODO: Close socket
            while(true){
                try{
                    // Wait for new connection to an Edge server 
                    socket = serverSocket.accept();
                    // Create and start new server thread to handle communication to new edge server
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