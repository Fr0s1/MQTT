package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// MongoDB import
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import server.ServerAPI.*;


public class Server {
    private static final int SERVER_PORT = 8080;
    private static final String mongodb_host = "localhost";
    private static final int mongodb_port = 27017;
    private static final String mongodb_database = "mqtt";
    private static final String mongodb_uri = String.format("mongodb://%s:%d", mongodb_host, mongodb_port);
    static ArrayList<Socket> sockets = new ArrayList<>(); // Array contains all connected sockets

    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket(SERVER_PORT);
        MongoClient mongoClient = MongoClients.create(mongodb_uri);
        MongoDatabase database = mongoClient.getDatabase(mongodb_database);

        while (true) {
            Socket s = null;
            try {
                System.out.println("Wating for connection...");
                s = ss.accept();
                sockets.add(s);

                System.out.println("Socket id " + s.hashCode());
                System.out.println("A new client is connected: " + s);

                DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                Thread t = new ClientHandler(s, dis, dos, database);

                t.start();
            } catch (IOException e) {
                sockets.remove(s);
                s.close();
                e.printStackTrace();
            }
        }
    }
}


class ClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final MongoDatabase db;
    final Socket s;
    static final int BUFFER_SIZE = 4096;

    enum State {
        HANDSHAKE,
        WAIT_APPLICATION_DATA,
        WAIT_SENSOR_DATA,
    }

    enum ApplicationHandlerState {
        GET_SENSORS,
        SENSOR_SELECT,
        SEND_DATA,
    }

    // Contructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, MongoDatabase db) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.db = db;
    }

    @Override
    public void run() {
        String receive;
        String sent;
        State state = State.HANDSHAKE;
        ApplicationHandlerState applicationHandlerState = null;

        while (true) {
            try {
                receive = dis.readUTF();
                System.out.println(receive.length());
                System.out.println("receive from client: " + receive);

                if (state == State.HANDSHAKE) {
                    MongoCollection sensorCollection = db.getCollection("devices");

                    Document newDevice = Document.parse(receive);

                    newDevice.append("socketId", s.hashCode());
                    int deviceType = newDevice.getInteger("sensor");

                    sensorCollection.insertOne(newDevice);

                    System.out.println("sent to client: Identifying...");
                    if (deviceType == 1) {
                        dos.writeUTF("Accept sensor");
                        state = State.WAIT_SENSOR_DATA;

                    } else {
                        dos.writeUTF("Accept application");
                        state = State.WAIT_APPLICATION_DATA;
                        applicationHandlerState = ApplicationHandlerState.GET_SENSORS;
                    }
                } else if (state == State.WAIT_APPLICATION_DATA) {
                    JSONObject message = new JSONObject(receive);
                    if (applicationHandlerState == ApplicationHandlerState.GET_SENSORS) {
                        String location = message.getString("location");

                        String sensorList = ServerAPI.getDevicesByLocation(location);

                        dos.writeUTF(sensorList);

                        applicationHandlerState = ApplicationHandlerState.SENSOR_SELECT;
                    } else if (applicationHandlerState == ApplicationHandlerState.SENSOR_SELECT) {
                        MongoCollection devices = db.getCollection("devices");

                        String sensorMac = message.getString("MAC");
                        Bson filter = Filters.eq("MAC", sensorMac);
                        Bson update = Updates.push("subscribe_sockets", s.hashCode());
                        devices.findOneAndUpdate(filter, update);

                        applicationHandlerState = ApplicationHandlerState.SEND_DATA;

                        dos.writeUTF("Sensor selected");
                    }
                } else if (state == State.WAIT_SENSOR_DATA) {
                    dos.writeUTF(receive + " OK!");

                    // Get sensor data collection
                    MongoCollection sensorData = db.getCollection("sensor_data");
                    MongoCollection devices = db.getCollection("devices");
                    Document newDoc = Document.parse(receive);

                    // Save sensor data to collection
                    sensorData.insertOne(newDoc);

                    // When receive new data from sensor, find subscribers to push data
                    Bson filter = Filters.eq("socketId", s.hashCode());
                    Bson projectionFields = Projections.fields(
                            Projections.include("subscribe_sockets"),
                            Projections.excludeId());
                    MongoCursor<Document> cursor = devices.find(filter).projection(projectionFields).iterator();

                    try {
                        while (cursor.hasNext()) {
                            String device = cursor.next().toJson();
                            JSONObject oj = new JSONObject(device);

                            JSONArray subscribeSockets = oj.getJSONArray("subscribe_sockets");
                            for (int i = 0; i < subscribeSockets.length(); i++) {
                                int subscribeSocketHashCode = subscribeSockets.getInt(i);

                                String finalReceive = receive;
                                Server.sockets.forEach(socket -> {
                                   if (socket.hashCode() == subscribeSocketHashCode) {
                                       try {
                                           DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                                           dos.writeUTF(finalReceive);

                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }

                                   }
                               });
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

