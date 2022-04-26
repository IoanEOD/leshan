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
import java.util.Map;
import java.util.TimeZone;

import javax.json.Json;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
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

                        LwM2mPath path = observation.getPath();
                        // TODO: Iterate over all id's and add to json payload

                        String objectPath = registration.getEndpoint() + "/" + path.getObjectId() + "/"
                                        + path.getObjectInstanceId();

                        JsonObjectBuilder readingsBuilder = Json.createObjectBuilder();

                        
                        if (observation.getPath().isObjectInstance()) {
                                // Observation for entire object
                                LwM2mObjectInstance lwM2mObjectInstance = (LwM2mObjectInstance) response.getContent();
                                Map<Integer, LwM2mResource> resources = lwM2mObjectInstance.getResources();

                                for (Map.Entry<Integer, LwM2mResource> resource : resources.entrySet()) {
                                        addValueToBuilder(readingsBuilder, Integer.toString(resource.getValue().getId()),
                                        resource.getValue().getValue());
                                    }
                        } else if (observation.getPath().isResource()) {
                                // Observation for single resource of an object
                                LwM2mResource resource = (LwM2mResource) response.getContent();
                                addValueToBuilder(readingsBuilder, Integer.toString(resource.getId()), resource.getValue());
                        }

                        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                                        .add("timestamp", time)
                                        .add("asset", objectPath);

                        payloadBuilder.add("readings", readingsBuilder);

                        String jsonPayload = "[" + payloadBuilder.build().toString() + "]";

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

                };

                @Override
                public void onResponse(CompositeObservation observation, Registration registration,
                                ObserveCompositeResponse response) {

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

        private void addValueToBuilder(JsonObjectBuilder builder, String asset, Object value) {
                switch (value.getClass().getSimpleName()) {
                        case "Double":
                                builder.add(asset, (Double) value);
                                break;
                        case "Integer":
                                builder.add(asset, (Integer) value);
                                break;
                        // TODO: Implement other data types (Supported by Fledge)
                        default:
                                builder.add(asset, value.toString());
                                break;
                }
        }
}
