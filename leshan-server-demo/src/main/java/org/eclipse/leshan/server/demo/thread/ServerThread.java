package org.eclipse.leshan.server.demo.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;

import com.google.gson.Gson;

import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.eclipse.leshan.core.response.SendableResponse;
import org.eclipse.leshan.core.response.UpdateResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Deregistration;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationHandler;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;
import org.eclipse.leshan.server.demo.model.RegistrationRequestObject;
import org.eclipse.leshan.core.request.DeregisterRequest;

enum RecieveModes {
	ip, request
}

public class ServerThread extends Thread {

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

	public ServerThread(Socket socket, RegistrationHandler registrationHandler, LeshanServer server) {
		this.socket = socket;
		this.registrationHandler = registrationHandler;
		this.server = server;

		this.gson = new Gson();
		this.registrationService = (RegistrationServiceImpl) server.getRegistrationService();
	}

	public void run() {
		try {
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputStream = new PrintWriter(socket.getOutputStream());

		} catch (IOException e) {
			System.out.println("IO error in server thread");
		}
		while (true) {
			try {
				line = inputStream.readLine();
				if ((line == null) || line.equalsIgnoreCase("QUIT")) {
					socket.close();
					return;
				} else {
					switch (recieveMode) {
						case ip:
							clientIP = line;
							recieveMode = RecieveModes.request;
							break;
						case request:
							RegistrationRequestObject requestObject = gson.fromJson(line,
									RegistrationRequestObject.class);
							Registration registration = requestObject.getRegistration();
							Identity identity = Identity
									.unsecure(Inet4Address.getByAddress(requestObject.getHostName(),
											requestObject.getAddr()), requestObject.getPort());

							String requestType = requestObject.getRequestType();
							switch (requestType) {
								case "register":
									Registration.Builder builder = new Registration.Builder(registration.getId(),
											registration.getEndpoint(), identity);

									builder.lwM2mVersion(registration.getLwM2mVersion())
											.rootPath(registration.getRootPath())
											.supportedContentFormats(registration.getSupportedContentFormats())
											.supportedObjects(registration.getSupportedObject())
											.availableInstances(registration.getAvailableInstances())
											.objectLinks(registration.getObjectLinks())
											.lifeTimeInSec(registration.getLifeTimeInSec())
											.bindingMode(registration.getBindingMode())
											.smsNumber(registration.getSmsNumber()).additionalRegistrationAttributes(
													registration.getAdditionalRegistrationAttributes());

									Registration newRegistration = builder.build();
									final Deregistration deregistration = registrationService.getStore()
											.addRegistration(newRegistration);
									break;
								case "update":
									UpdateRequest updateRequest = new UpdateRequest(registration.getId(),registration.getLifeTimeInSec(),registration.getSmsNumber(),registration.getBindingMode(),registration.getObjectLinks(),registration.getAdditionalRegistrationAttributes());
									final SendableResponse<UpdateResponse> updateResponse = registrationHandler.update(identity, updateRequest);
									break;
								case "deregister":
									DeregisterRequest deregisterRequest = new DeregisterRequest(registration.getId());
									registrationHandler.deregister(identity, deregisterRequest);
									break;
							}

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
