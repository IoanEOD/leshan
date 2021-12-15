package org.eclipse.leshan.server.demo.websocket;

import java.net.*;
import java.io.*;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.leshan.server.californium.LeshanServer;

import org.eclipse.leshan.server.demo.websocket.WebSocketClient;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.leshan.server.demo.model.RegistrationRequestObject;
import com.google.gson.Gson;



public class WebSocketClient {
    private Socket socket;
    private LeshanServer server;
    private OutputStream output;
    private PrintWriter writer;

    private final RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void registered(Registration registration, Registration previousReg,
                Collection<org.eclipse.leshan.core.observation.Observation> previousObsersations) {
                    System.out.println("registered");

                    RegistrationRequestObject registrationRequestObject = new RegistrationRequestObject("register", registration);
                    sendRegistrationRequestObject(registrationRequestObject);
        }

        @Override
        public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
            System.out.println("updated");

            RegistrationRequestObject registrationRequestObject = new RegistrationRequestObject("update", updatedReg);
            sendRegistrationRequestObject(registrationRequestObject);
        }

        @Override
        public void unregistered(Registration registration,
                Collection<org.eclipse.leshan.core.observation.Observation> observations, boolean expired,
                Registration newReg) {
                    System.out.println("unregistered");

                    RegistrationRequestObject registrationRequestObject = new RegistrationRequestObject("deregister", registration);
                    sendRegistrationRequestObject(registrationRequestObject);
        }
    };

    public WebSocketClient(LeshanServer server, String address, int port) throws IOException {

        this.socket = new Socket(address, port);
        this.server = server;

        server.getRegistrationService().addListener(this.registrationListener);
        output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
    }

    public void sendEdgeName() throws IOException {
        String localHost = InetAddress.getLocalHost().toString();
        writer.println(localHost.split("/")[0]);
    }

    public void sendRegistrationRequestObject(RegistrationRequestObject registrationRequestObject) {
        final Gson gson = new Gson();
        String json = gson.toJson(registrationRequestObject, RegistrationRequestObject.class);
        writer.println(json);
    }


    public String getInput() throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }
}

