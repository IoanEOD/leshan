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
import org.eclipse.leshan.core.request.Identity;
import org.eclipse.leshan.core.request.RegisterRequest;
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
                    final Inet4Address addr = (Inet4Address) registration.getIdentity().getPeerAddress().getAddress();
                    final byte[] ip = addr.getAddress();
                    final String hostName = addr.getHostName();
                    final int port = registration.getIdentity().getPeerAddress().getPort();
                    RegistrationRequestObject r = new RegistrationRequestObject(registration, ip, hostName, port);

                    final Gson gson = new Gson();
                    String json = gson.toJson(r, RegistrationRequestObject.class);
                    writer.println(json);

        }

        @Override
        public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
            System.out.println("updated");

            final Inet4Address addr = (Inet4Address) updatedReg.getIdentity().getPeerAddress().getAddress();
            final byte[] ip = addr.getAddress();
            final String hostName = addr.getHostName();
            final int port = updatedReg.getIdentity().getPeerAddress().getPort();
            RegistrationRequestObject r = new RegistrationRequestObject(updatedReg, ip, hostName, port);

            final Gson gson = new Gson();
            String json = gson.toJson(r, RegistrationRequestObject.class);
            writer.println(json);
        }

        @Override
        public void unregistered(Registration registration,
                Collection<org.eclipse.leshan.core.observation.Observation> observations, boolean expired,
                Registration newReg) {
                    System.out.println("unregistered");
        }
    };

    public WebSocketClient(LeshanServer server, String address, int port) throws IOException {

        this.socket = new Socket(address, port);
        this.server = server;

        server.getRegistrationService().addListener(this.registrationListener);
        output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
    }

    public void sendIPAddress() throws IOException {
        String localHost = InetAddress.getLocalHost().toString();
        writer.println(localHost.split("/")[1]);
    }


    public String getInput() throws IOException {
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        return line;
    }
}

