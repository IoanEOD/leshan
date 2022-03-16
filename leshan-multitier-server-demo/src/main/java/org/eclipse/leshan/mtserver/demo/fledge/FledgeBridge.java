package org.eclipse.leshan.mtserver.demo.fledge;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.json.Json;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.observation.CompositeObservation;
import org.eclipse.leshan.core.observation.SingleObservation;
import org.eclipse.leshan.core.response.ObserveCompositeResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;

public class FledgeBridge {

        private final ObservationListener observationListener = new ObservationListener() {
                @Override
                public void onResponse(SingleObservation observation, Registration registration,
                                ObserveResponse response) {

                        String time = currentTimeAsIso();
                        
                        //TODO: Iterate over all id's and add to json payload
                        String path = registration.getEndpoint() + observation.getPath().toString();

                        LwM2mSingleResource node = (LwM2mSingleResource) response.getContent();

                        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                                        .add("timestamp", time
                                        )
                                        .add("asset", path);

                        switch (node.getValue().getClass().getSimpleName()) {
                                case "String":
                                        jsonBuilder.add("readings", Json.createObjectBuilder()
                                                        .add("value", node.getValue().toString()));
                                        break;
                                case "Double":
                                        jsonBuilder.add("readings", Json.createObjectBuilder()
                                                        .add("value", (Double) node.getValue()));
                                        break;
                                case "Integer":
                                        jsonBuilder.add("readings", Json.createObjectBuilder()
                                                        .add("value", (Integer) node.getValue()));
                                        break;
                                // TODO: Implement other data types
                                default:
                                        jsonBuilder.add("readings", Json.createObjectBuilder()
                                                        .add("value", node.getValue().toString()));
                                        break;
                        }

                        JsonObject jsonObject = jsonBuilder.build();
                        String jsonPayload = "[" + jsonObject.toString() + "]";

                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest httpRequest = HttpRequest.newBuilder()
                                        .uri(URI.create("http://localhost:6683/sensor-reading"))
                                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                                        .build();

                        HttpResponse<String> httpResponse;
                        try {
                                httpResponse = client.send(httpRequest,
                                                HttpResponse.BodyHandlers.ofString());

                                System.out.println(httpResponse.body());
                        } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }

                        String y = "f";

                };

                @Override
                public void onResponse(CompositeObservation observation, Registration registration,
                                ObserveCompositeResponse response) {
                        
                        System.out.println("x");
                        // TODO Composite HTTP Post to Fledge
                };

                @Override
                public void onError(org.eclipse.leshan.core.observation.Observation observation,
                                Registration registration,
                                Exception error) {
                };

                @Override
                public void cancelled(org.eclipse.leshan.core.observation.Observation observation) {
                };

                @Override
                public void newObservation(org.eclipse.leshan.core.observation.Observation observation,
                                Registration registration) {
                };
        };

        public FledgeBridge(LeshanServer server) {
                server.getObservationService().addListener(this.observationListener);
        }

        private String currentTimeAsIso() {
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                df.setTimeZone(tz);
                return df.format(new Date());
        }
}
