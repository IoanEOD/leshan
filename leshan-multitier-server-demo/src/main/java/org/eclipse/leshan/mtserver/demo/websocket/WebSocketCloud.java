package org.eclipse.leshan.mtserver.demo.websocket;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.leshan.mtserver.demo.thread.ConnectionThread;
import org.eclipse.leshan.mtserver.demo.thread.SocketThread;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.RandomStringRegistrationIdProvider;
import org.eclipse.leshan.server.registration.RegistrationHandler;
import org.eclipse.leshan.server.registration.RegistrationIdProvider;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;
import org.eclipse.leshan.server.security.Authorizer;
import org.eclipse.leshan.server.security.DefaultAuthorizer;

public class WebSocketCloud {

	private ServerSocket serverSocket;

	// Array to keep track of all the server threads associated with unique edge
	// servers
	private ArrayList<SocketThread> serverThreads = new ArrayList<>();

	public WebSocketCloud(LeshanServer server) throws IOException {

		// Set up a registration handler to be passed into connection thread
		// Will be used to create, update and delete registrations in cloud
		Authorizer authorizer = new DefaultAuthorizer(server.getSecurityStore());
		RegistrationIdProvider registrationIdProvider = new RandomStringRegistrationIdProvider();
		RegistrationServiceImpl registrationService = (RegistrationServiceImpl) server.getRegistrationService();
		final RegistrationHandler registrationHandler = new RegistrationHandler(registrationService, authorizer,
				registrationIdProvider);

		ConnectionThread connectionThread = new ConnectionThread(this, registrationHandler, server);
		connectionThread.start();
	}

	public void close() throws IOException {
		serverSocket.close();
	}

	public void addServerThread(SocketThread serverThread) {
		serverThreads.add(serverThread);
	}

	public SocketThread getServerThreadWithEdgeName(String edgeName){
		SocketThread res = null;
		for(int i=0; i<serverThreads.size();i++) {
			if(serverThreads.get(i).getEdgeName().equals(edgeName)) {
				res = serverThreads.get(i);
			}
		}
		return res;
	}
}
