package server;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

public class ServerAPI {
    private static final String mongodb_host = "localhost";
    private static final int mongodb_port = 27017;
    private static final String mongodb_database = "mqtt";
    private static final String mongodb_uri = String.format("mongodb://%s:%d", mongodb_host, mongodb_port);
    static MongoClient mongoClient = MongoClients.create(mongodb_uri);
    static MongoDatabase database = mongoClient.getDatabase(mongodb_database);

    static String getDevicesByLocation(String district) {
        MongoCollection devices = database.getCollection("devices");
        Bson projectionFields = Projections.fields(
                Projections.include("type", "MAC"),
                Projections.excludeId());

        Bson filter = Filters.and(Filters.eq("location", district));
        MongoCursor<Document> cursor = devices.find(filter).projection(projectionFields).iterator();

        JSONObject data = new JSONObject();

        try {
            while (cursor.hasNext()) {
                String device = cursor.next().toJson();
                System.out.println(device);
                data.append("devices", device);
            }
        } finally {
            cursor.close();
        }

        return data.toString();
    }

    static void addToSensorSubscribeList(int applicationSocketID, String sensorMacAddress) {
        MongoCollection sensorSubscribeList = database.getCollection("devices");

        Bson filter = Filters.and(Filters.eq("MAC", sensorMacAddress));

        Bson update = Updates.push("subscribe_sockets", applicationSocketID);
        sensorSubscribeList.findOneAndUpdate(filter, update);
    }
}
