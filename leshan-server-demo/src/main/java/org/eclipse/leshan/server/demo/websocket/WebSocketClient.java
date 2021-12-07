package org.eclipse.leshan.server.demo.websocket;


import java.net.*;
import java.io.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.californium.core.observe.Observation;
import org.eclipse.leshan.server.californium.LeshanServer;


import org.eclipse.leshan.server.demo.websocket.WebSocketClient;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;






public class WebSocketClient {
    private Socket socket;
    private ObjectMapper mapper;
    private LeshanServer server;
    private OutputStream output;
    private PrintWriter writer;

    


    private final RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void registered(Registration registration, Registration previousReg,
                Collection<org.eclipse.leshan.core.observation.Observation> previousObsersations) {
                    System.out.println("registered");
                    try {
                        sendRegistrations();
                    }
                    catch(IOException e) {
                        throw new RuntimeException(e);
                    }
        }

        @Override
        public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
            System.out.println("updated");
            try {
                sendRegistrations();
            }
            catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void unregistered(Registration registration,
                Collection<org.eclipse.leshan.core.observation.Observation> observations, boolean expired,
                Registration newReg) {
                    System.out.println("unregistered");
                    try {
                        sendRegistrations();
                    }
                    catch(IOException e) {
                        throw new RuntimeException(e);
                    }
        }
    };

    public WebSocketClient(LeshanServer server, String address, int port) throws IOException {
        this.socket = new Socket(address,port);
        this.mapper = new ObjectMapper();
        this.server = server;

        server.getRegistrationService().addListener(this.registrationListener);
        output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
    }


    public void sendIPAddress() throws IOException {
            String localHost = InetAddress.getLocalHost().toString();
            writer.println(localHost.split("/")[1]);
    }

    public void sendRegistrations() throws IOException {
            Collection<Registration> registrations = new ArrayList<>();
            for (Iterator<Registration> iterator = server.getRegistrationService().getAllRegistrations(); iterator
                    .hasNext();) {
                registrations.add(iterator.next());
            }

            String json = this.mapper.writeValueAsString(registrations.toArray(new Registration[] {}));
            writer.println(json);
    }

    public String getInput() throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }
}



