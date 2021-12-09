package server;

import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mongodb.client.model.Sorts.descending;


public class Test {
    private static final int SERVER_PORT = 8080;
    private static final String mongodb_host = "localhost";
    private static final int mongodb_port = 27017;
    private static final String mongodb_database = "mqtt";
    private static final String mongodb_uri = String.format("mongodb://%s:%d", mongodb_host, mongodb_port);


    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create(mongodb_uri);
        MongoDatabase database = mongoClient.getDatabase(mongodb_database);
//
//        MongoCollection devices = database.getCollection("devices");
        MongoCollection<Document> sensor_data = database.getCollection("sensor_data");

        Bson filter = Filters.eq("MAC", "11:22:33:44:55:66");
        Bson projection = Projections.exclude("_id");
        MongoCursor<Document> selectedSensorData = sensor_data.find(filter).projection(projection).sort(descending("sent_time")).limit(4).iterator();

        JSONObject result = new JSONObject();
        while (selectedSensorData.hasNext()) {
            String data = selectedSensorData.next().toJson();

            System.out.println(data);

            result.append("data", data);

        }

        System.out.println(result);
        //        Bson projectionFields = Projections.fields(
//                Projections.include("type", "MAC"),
//                Projections.excludeId());
//
//        Bson filter = Filters.and(Filters.eq("location", "Cau Giay"));
//        MongoCursor<Document> cursor = devices.find(filter).projection(projectionFields).iterator();
//
//        JSONObject data = new JSONObject();
//
//        try {
//            while(cursor.hasNext()) {
//                String device = cursor.next().toJson();
//                System.out.println(device);
//                data.append("devices", device);
//            }
//        } finally {
//            cursor.close();
//        }
//
//        System.out.println(data.toString());
//        String data = "{\"sensor\":1,\"location\":\"Cau Giay\",\"type\":\"temperature\",\"MAC\":\"11:22:33:44:55:66\"}";
//
//        JSONObject jo = new JSONObject(data);
//        int deviceType = jo.getInt("sensor");
//
//        System.out.println(deviceType);

//        Bson filter = Filters.eq("socketId", 873610597);
//        Bson projectionFields = Projections.fields(
//                Projections.include("subscribe_sockets"),
//                Projections.excludeId());
//        MongoCursor<Document> cursor = devices.find(filter).projection(projectionFields).iterator();
//
//        try {
//            while (cursor.hasNext()) {
//                String device = cursor.next().toJson();
//                JSONObject oj = new JSONObject(device);
//
//
//                JSONArray subscribeSockets = oj.getJSONArray("subscribe_sockets");
//
//                for (int i = 0; i <subscribeSockets.length(); i++) {
//                    System.out.println(subscribeSockets.get(i));
//                }
////                System.out.println(device);
//            }
//        } finally {
//            cursor.close();
//        }
    }
}
