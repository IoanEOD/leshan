package org.eclipse.leshan.mtserver.demo.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.SendableResponse;
import org.eclipse.leshan.core.response.UpdateResponse;
import org.eclipse.leshan.mtserver.demo.model.WriteRequestAttributes;
import org.eclipse.leshan.mtserver.demo.servlet.CloudClientServlet;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonLwM2mNodeDeserializer;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonLwM2mNodeSerializer;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonRegistrationSerializer;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonResponseSerializer;
import org.eclipse.leshan.mtserver.demo.model.RegistrationRequestObject;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Deregistration;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationHandler;
import org.eclipse.leshan.server.registration.RegistrationServiceImpl;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.request.DeregisterRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;

// Enum that represents what data the websocket expects next
enum RecieveModes {
	name, request, response
}

public class SocketThread extends Thread {

	private String edgeName;

	private LeshanServer server;

	private String line;

	private BufferedReader inputStream;

	private PrintWriter outputStream;

	private Socket socket;

	private RegistrationHandler registrationHandler;

	private RecieveModes recieveMode = RecieveModes.name;

	private Gson gson;

	private RegistrationServiceImpl registrationService;

	private ConnectionThread connectionThread;

	private OutputStream output;
	private PrintWriter writer;

	private HttpServletRequest currentReq;
	private HttpServletResponse currentResp;

	private final ObjectMapper mapper;

	private static final Logger LOG = LoggerFactory.getLogger(CloudClientServlet.class);

	

	public SocketThread(Socket socket, RegistrationHandler registrationHandler, LeshanServer server,
			ConnectionThread connectionThread) throws IOException {
		this.socket = socket;
		this.registrationHandler = registrationHandler;
		this.server = server;
		this.connectionThread = connectionThread;
		this.output = socket.getOutputStream();
		this.writer = new PrintWriter(output, true);

		mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Registration.class, new JacksonRegistrationSerializer(server.getPresenceService()));
        module.addSerializer(LwM2mResponse.class, new JacksonResponseSerializer());
        module.addSerializer(LwM2mNode.class, new JacksonLwM2mNodeSerializer());
        module.addDeserializer(LwM2mNode.class, new JacksonLwM2mNodeDeserializer());
        mapper.registerModule(module);

		this.gson = new Gson();
		this.registrationService = (RegistrationServiceImpl) server.getRegistrationService();
	}

	public void run() {
		try {
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
						case name:
							// Get name of server
							ArrayList<String> edgeNames = connectionThread.getEdgeNames();
							long nameCount = edgeNames.stream().filter(name -> line.equals(name)).count();
							if (nameCount > 0) {
								edgeName = line + (nameCount + 1);
							} else {
								edgeName = line;
							}
							edgeNames.add(line);
							connectionThread.setEdgeNames(edgeNames);
							recieveMode = RecieveModes.request;
							break;
						case request:
							// Convert from json
							RegistrationRequestObject requestObject = gson.fromJson(line,
									RegistrationRequestObject.class);
							Registration registration = requestObject.getRegistration();
							// Reconstruct identity of the endpoint using an unsecure identity (will need to
							// consider secure Identities)
							Identity identity = Identity
									.unsecure(Inet4Address.getByAddress(requestObject.getHostName(),
											requestObject.getAddr()), requestObject.getPort());

							String requestType = requestObject.getRequestType();
							switch (requestType) {
								case "register":
									// Reconstruct registration with new modified endpoint name to illustrate the
									// respective edge server
									Registration.Builder builder = new Registration.Builder(registration.getId(),
											edgeName + " - " + registration.getEndpoint(), identity);

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
									// Convert recieved registration to UpdateRequest
									UpdateRequest updateRequest = new UpdateRequest(registration.getId(),
											registration.getLifeTimeInSec(), registration.getSmsNumber(),
											registration.getBindingMode(), registration.getObjectLinks(),
											registration.getAdditionalRegistrationAttributes());
									final SendableResponse<UpdateResponse> updateResponse = registrationHandler
											.update(identity, updateRequest);
									break;
								case "deregister":
									// Convert recieved registration to DeregisterRequest
									DeregisterRequest deregisterRequest = new DeregisterRequest(registration.getId());
									registrationHandler.deregister(identity, deregisterRequest);
									break;
							}

							break;
							case response:
								processDeviceResponse(currentReq, currentResp, line);
								recieveMode = RecieveModes.request;
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void sendWriteRequestAttributes(WriteRequestAttributes attributes, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		final Gson gson = new Gson();
		String json = gson.toJson(attributes, WriteRequestAttributes.class);
		writer.println(json);
		currentReq = req;
		currentResp = resp;
		recieveMode = RecieveModes.response;
	}

	public String getEdgeName() {
		return edgeName;
	}

	private void processDeviceResponse(HttpServletRequest req, HttpServletResponse resp, String cResponse)
			throws IOException {
		if (cResponse == null) {
			LOG.warn(String.format("Request %s%s timed out.", req.getServletPath(), req.getPathInfo()));
			resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			resp.getWriter().append("Request timeout").flush();
		} else {
			String response = mapper.writeValueAsString(cResponse);
			resp.setContentType("application/json");
			resp.getOutputStream().write(response.getBytes());
			resp.setStatus(HttpServletResponse.SC_OK);
		}
	}


}
