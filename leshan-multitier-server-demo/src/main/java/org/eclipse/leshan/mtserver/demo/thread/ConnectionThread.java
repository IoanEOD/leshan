package org.eclipse.leshan.mtserver.demo.thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.eclipse.leshan.mtserver.demo.websocket.WebSocketCloud;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.RegistrationHandler;

public class ConnectionThread extends Thread {

    private WebSocketCloud webSocketCloud;

    private RegistrationHandler registrationHandler;

    private LeshanServer server;

    private ArrayList<String> edgeNames = new ArrayList<String>();

    public ConnectionThread(WebSocketCloud webSocketCloud, RegistrationHandler registrationHandler,
            LeshanServer server) {
        this.webSocketCloud = webSocketCloud;
        this.registrationHandler = registrationHandler;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            Socket socket = null;
            ServerSocket serverSocket = null;
            serverSocket = new ServerSocket(4999);
            // TODO: Close socket
            while (true) {
                try {
                    // Wait for new connection to an Edge server
                    socket = serverSocket.accept();
                    // Create and start new server thread to handle communication to new edge server
                    SocketThread serverThread = new SocketThread(socket, registrationHandler, server, this);
                    webSocketCloud.addSocketThread(serverThread);
                    serverThread.start();
                }

                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEdgeNames(ArrayList<String> edgeNames) {
        this.edgeNames = edgeNames;
    }

    public ArrayList<String> getEdgeNames() {
        return this.edgeNames;
    }
}