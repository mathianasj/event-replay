package dev.cloudfirst;

import java.net.URI;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import org.bson.Document;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkus.reactivemessaging.http.runtime.OutgoingHttpMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadataBuilder;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

@ApplicationScoped
public class CloudEventUtil {
    @Channel("default-broker")
    Emitter<Object> cloudEventEmitter;

    public void sendEvent(Object body, Document headers) {
        // Make it a cloud event
        OutgoingCloudEventMetadata<Object> cloudEventMetadata = new OutgoingCloudEventMetadataBuilder<Object>()
            .withSource(URI.create((String) headers.get("CamelCloudEventSource")))
            .withType((String) headers.get("CamelCloudEventType"))
            .withExtension("partitionkey", (String) headers.get("Ce-Partitionkey"))
            .withExtension("replay", "true")
            .build();
 
        // Make sure we have the right content type header
        OutgoingHttpMetadata httpMetaData = new OutgoingHttpMetadata.Builder().addHeader("content-type", MediaType.APPLICATION_JSON).build();

        // Setup the message with ack / nack to be passed on as reactive
        Message<Object> msg = ContextAwareMessage.of(body)
        .addMetadata(httpMetaData)
        .addMetadata(cloudEventMetadata);

        // emit the message
        cloudEventEmitter.send(msg);
    }
}
