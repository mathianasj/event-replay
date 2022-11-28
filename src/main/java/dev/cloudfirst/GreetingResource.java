package dev.cloudfirst;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Sorts;

@Path("/events")
public class GreetingResource {
    @Inject
    MongoClient mongoClient;

    @Inject
    CloudEventUtil cloudEventUtil;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public void replayEvents() {
        MongoCursor<Document> cursor = getCollection().find().sort(Sorts.ascending("headers.Ce-Knativearrivaltime")).iterator();

        while(cursor.hasNext()) {
            Document document = cursor.next();
            Object body = document.get("body");
            Object headers = document.get("headers");

            cloudEventUtil.sendEvent(body, (Document) headers);
        }
    }

    private MongoCollection<Document> getCollection() {
        return mongoClient.getDatabase("admin").getCollection("events");
    }
}