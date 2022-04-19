package org.eclipse.leshan.mtserver.demo.websocket;

import java.net.*;
import java.io.*;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.core.attributes.AttributeSet;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.WriteAttributesRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.WriteAttributesResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.mtserver.demo.model.RegistrationRequestObject;
import org.eclipse.leshan.mtserver.demo.model.WriteRequestAttributes;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonLwM2mNodeDeserializer;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonLwM2mNodeSerializer;
import org.eclipse.leshan.mtserver.demo.servlet.json.JacksonResponseSerializer;
import org.eclipse.leshan.mtserver.demo.websocket.WebSocketEdge;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.request.exception.InvalidRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class WebSocketEdge extends Thread {
    private Socket socket;
    private LeshanServer server;
    private OutputStream output;
    private BufferedReader inputStream;
    private PrintWriter writer;
    private String line;
    private final ObjectMapper mapper;

    private final RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void registered(Registration registration, Registration previousReg,
                Collection<org.eclipse.leshan.core.observation.Observation> previousObsersations) {
            // Send new registration to cloud
            RegistrationRequestObject registrationRequestObject = new RegistrationRequestObject("register",
                    registration);
            sendRegistrationRequestObject(registrationRequestObject);
        }

        @Override
        public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
            // Send updated registration to cloud
            RegistrationRequestObject registrationRequestObject = new RegistrationRequestObject("update", updatedReg);
            sendRegistrationRequestObject(registrationRequestObject);
        }

        @Override
        public void unregistered(Registration registration,
                Collection<org.eclipse.leshan.core.observation.Observation> observations, boolean expired,
                Registration newReg) {
            // Send de-registered registration to cloud
            RegistrationRequestObject registrationRequestObject = new RegistrationRequestObject("deregister",
                    registration);
            sendRegistrationRequestObject(registrationRequestObject);
        }
    };

    public WebSocketEdge(LeshanServer server, String address, int port) throws IOException {

        this.socket = new Socket(address, port);
        this.server = server;

        // Set up registration listner to listen for changes in registrations
        server.getRegistrationService().addListener(this.registrationListener);

        output = socket.getOutputStream();
        writer = new PrintWriter(output, true);

        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LwM2mResponse.class, new JacksonResponseSerializer());
        module.addSerializer(LwM2mNode.class, new JacksonLwM2mNodeSerializer());
        module.addDeserializer(LwM2mNode.class, new JacksonLwM2mNodeDeserializer());
        mapper.registerModule(module);
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
                    final Gson gson = new Gson();
                    WriteRequestAttributes request = gson.fromJson(line, WriteRequestAttributes.class);

                    String resp = proccessRequestWrapper(request);
                    writer.println(resp);

                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    // Used to send the name of the edge server as a string over websocket to cloud
    // server
    public void sendEdgeName() throws IOException {
        String localHost = InetAddress.getLocalHost().toString();
        writer.println(localHost.split("/")[0]);
    }

    // Serialize RegistrationRequest object and send as json via websocket
    public void sendRegistrationRequestObject(RegistrationRequestObject registrationRequestObject) {
        final Gson gson = new Gson();
        String json = gson.toJson(registrationRequestObject, RegistrationRequestObject.class);
        writer.println(json);
    }

    private String proccessRequestWrapper(WriteRequestAttributes request) {
        String[] path = StringUtils.split(request.getPathInfo(), '/');
        String clientEndpoint = path[0];
        String cResponseString;

        try {
            String target = StringUtils.removeStart(request.getPathInfo(), "/" + clientEndpoint);
            Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);


            if (registration != null) {
                if (path.length >= 3 && "attributes".equals(path[path.length - 1])) {
                    // create & process request WriteAttributes request
                    target = StringUtils.removeEnd(target, path[path.length - 1]);
                    AttributeSet attributes = AttributeSet.parse(request.getQueryString());
                    WriteAttributesRequest attributesRequest = new WriteAttributesRequest(target, attributes);
                    WriteAttributesResponse cResponse = server.send(registration, attributesRequest,
                            request.getTimeout());
                    cResponseString = this.mapper.writeValueAsString(cResponse);
                } else {
                    // get content format
                    String contentFormatParam = request.getContentFormatParam();
                    ContentFormat contentFormat = contentFormatParam != null
                            ? ContentFormat.fromName(contentFormatParam.toUpperCase())
                            : null;

                    // get replace parameter
                    String replaceParam = request.getReplaceParam();
                    boolean replace = true;
                    if (replaceParam != null)
                        replace = Boolean.valueOf(replaceParam);

                    // create & process request
                    LwM2mNode node = extractLwM2mNode(target, request, new LwM2mPath(target));
                    System.out.println(node);

                    WriteRequest writeRequest = new WriteRequest(replace ? Mode.REPLACE : Mode.UPDATE, contentFormat,
                            target,
                            node);
                    WriteResponse cResponse = server.send(registration, writeRequest, request.getTimeout());
                    cResponseString = this.mapper.writeValueAsString(cResponse);
                }
            } else {
                cResponseString = null;
                // resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // resp.getWriter().format("No registered client with id '%s'",
                // clientEndpoint).flush();
            }
        } catch (RuntimeException | InterruptedException | IOException e) {
            cResponseString = null;
            e.printStackTrace();
        }
        return cResponseString;

    }

    private LwM2mNode extractLwM2mNode(String target, WriteRequestAttributes request, LwM2mPath path) throws IOException {
        String contentType = StringUtils.substringBefore(request.getContentType(), ";");
        if ("application/json".equals(contentType)) {
            String content = request.getContent();
            LwM2mNode node;
            try {
                node = mapper.readValue(content, LwM2mNode.class);
            } catch (JsonProcessingException e) {
                throw new InvalidRequestException(e, "unable to parse json to tlv:%s", e.getMessage());
            }
            return node;
        } else if ("text/plain".equals(contentType)) {
            String content = request.getContent();
            int rscId = Integer.valueOf(target.substring(target.lastIndexOf("/") + 1));
            return LwM2mSingleResource.newStringResource(rscId, content);
        }
        throw new InvalidRequestException("content type %s not supported", request.getContentType());
    }
}
