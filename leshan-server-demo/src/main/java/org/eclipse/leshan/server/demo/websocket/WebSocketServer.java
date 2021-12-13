package org.eclipse.leshan.server.demo.websocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.demo.thread.ConnectionThread;
import org.eclipse.leshan.server.demo.thread.ServerThread;
import org.eclipse.leshan.server.registration.RandomStringRegistrationIdProvider;
import org.eclipse.leshan.server.registration.RegistrationHandler;
import org.eclipse.leshan.server.registration.RegistrationIdProvider;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;
import org.eclipse.leshan.server.security.Authorizer;
import org.eclipse.leshan.server.security.DefaultAuthorizer;

public class WebSocketServer {

	private ServerSocket serverSocket;
	private Socket socket;
	private LeshanServer server;
	private ArrayList<ServerThread> serverThreads = new ArrayList<>();

	public WebSocketServer(LeshanServer server) throws IOException {
		this.server = server;
		
		Authorizer authorizer = new DefaultAuthorizer(server.getSecurityStore());
        RegistrationIdProvider registrationIdProvider = new RandomStringRegistrationIdProvider();
        RegistrationServiceImpl registrationService = (RegistrationServiceImpl) server.getRegistrationService();
        final RegistrationHandler registrationHandler = new RegistrationHandler(registrationService, authorizer, registrationIdProvider);

		ConnectionThread connectionThread = new ConnectionThread(this, registrationHandler, server);
		connectionThread.start();
	}

	public void close() throws IOException {
		serverSocket.close();
	}

	public void addServerThread(ServerThread serverThread) {
		serverThreads.add(serverThread);
	}
}
