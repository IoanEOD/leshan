package org.eclipse.leshan.server.demo.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.eclipse.leshan.core.link.Link;
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Deregistration;
import org.eclipse.leshan.server.registration.RandomStringRegistrationIdProvider;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationHandler;
import org.eclipse.leshan.server.registration.RegistrationIdProvider;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;
import org.eclipse.leshan.server.security.Authorizer;
import org.eclipse.leshan.server.security.DefaultAuthorizer;
import org.eclipse.leshan.server.demo.model.RegistrationRequestObject;
import org.eclipse.leshan.core.request.BindingMode;

enum RecieveModes {
	ip, request
}

public class ServerThread extends Thread{  

	private String clientIP;

	private LeshanServer server;

	private String line;

	private BufferedReader inputStream;

	private PrintWriter outputStream;

	private Socket socket;

	private RegistrationHandler registrationHandler;

	private RecieveModes recieveMode = RecieveModes.ip;

    private Gson gson;

	private RegistrationServiceImpl registrationService;

	public ServerThread(Socket socket, RegistrationHandler registrationHandler, LeshanServer server){
		this.socket = socket;
		this.registrationHandler = registrationHandler;
		this.server = server;

		this.gson = new Gson();
		this.registrationService = (RegistrationServiceImpl) server.getRegistrationService();
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
						case ip:
							clientIP = line;
							recieveMode = RecieveModes.request;
						  break;
						case request:
							RegistrationRequestObject r = gson.fromJson(line, RegistrationRequestObject.class);
							Registration oldRegistration = r.getRegistration();

							// Link[] linkObject = oldRegistration.getObjectLinks();
							// long lifeTimeInSec = oldRegistration.getLifeTimeInSec();
							// EnumSet<BindingMode> bindingMode = oldRegistration.getBindingMode();
							// String smsNumber = oldRegistration.getSmsNumber();
							// Map<String, String> additionalAttributes = oldRegistration.getAdditionalRegistrationAttributes();
							// Date lastUpdate = new Date();

							InetAddress inet4Address = Inet4Address.getByAddress(r.getHostName(), r.getAddr());
							InetSocketAddress inetSocketAddress = new InetSocketAddress(inet4Address, r.getPort());
							Identity identity = Identity.unsecure(inet4Address, r.getPort());
							Registration.Builder builder = new Registration.Builder(oldRegistration.getId(), oldRegistration.getEndpoint(), identity);

							builder.lwM2mVersion(oldRegistration.getLwM2mVersion()).rootPath(oldRegistration.getRootPath())
							.supportedContentFormats(oldRegistration.getSupportedContentFormats())
							.supportedObjects(oldRegistration.getSupportedObject())
							.availableInstances(oldRegistration.getAvailableInstances()).objectLinks(oldRegistration.getObjectLinks()).lifeTimeInSec(oldRegistration.getLifeTimeInSec()).bindingMode(oldRegistration.getBindingMode()).smsNumber(oldRegistration.getSmsNumber()).additionalRegistrationAttributes(oldRegistration.getAdditionalRegistrationAttributes());

							Registration newRegistration = builder.build();						
							final Deregistration deregistration = registrationService.getStore().addRegistration(newRegistration);

							// RegistrationRequestObject regReqObj = gson.fromJson(line, RegistrationRequestObject.class);
							// System.out.println(regReqObj);
							// registrationHandler.register(regReqObj.getSender(), regReqObj.getRegisterRequest());
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


