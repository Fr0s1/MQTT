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
import org.bson.json.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mongodb.client.model.Sorts.descending;

import server.ServerAPI;

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
            } catch (Exception e) {
                System.out.println("Disconnected");
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
    int connectedDeviceType;

    enum State {
        HANDSHAKE,
        WAIT_APPLICATION_DATA,
        WAIT_SENSOR_DATA,
    }

    enum ApplicationHandlerState {
        GET_SENSORS,
        SENSOR_SELECT
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
        String selectedSensorMAC = null; // Use for sensor's MAC address selected by application

        String connectedDeviceMAC = null;

        MongoCollection<Document> devicesCollection = db.getCollection("devices");

        while (true) {
            try {
                receive = dis.readUTF();
                System.out.println(receive.length());
                System.out.println("receive from client: " + receive);

                if (state == State.HANDSHAKE) {

                    Document newDevice = Document.parse(receive);

                    String deviceMacAddress = newDevice.getString("MAC"); // Get new device MAC address
                    connectedDeviceMAC = deviceMacAddress;

                    Bson filter = Filters.eq("MAC", deviceMacAddress);

                    MongoCursor<Document> devices = devicesCollection.find(filter).iterator();

                    // Check if the device connected to server before
                    if (devices.hasNext()) {
                        // If has connected, update socketId
                        Bson update = Updates.set("socketId", s.hashCode());
                        devicesCollection.findOneAndUpdate(filter, update);
                    } else {
                        newDevice.append("socketId", s.hashCode());
                        devicesCollection.insertOne(newDevice);
                    }

                    int deviceType = newDevice.getInteger("sensor");
                    this.connectedDeviceType = deviceType;
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
                    try {
                        JSONObject message = new JSONObject(receive);
                        if (applicationHandlerState == ApplicationHandlerState.GET_SENSORS) {
                            String location = message.getString("location");

                            String sensorList = ServerAPI.getDevicesByLocation(location);

                            dos.writeUTF(sensorList);

                            applicationHandlerState = ApplicationHandlerState.SENSOR_SELECT;
                        } else if (applicationHandlerState == ApplicationHandlerState.SENSOR_SELECT) {
                            MongoCollection<Document> devices = db.getCollection("devices");

                            String sensorMac = message.getString("MAC");
                            Bson filter = Filters.eq("MAC", sensorMac);
                            selectedSensorMAC = sensorMac;

                            Bson update = Updates.push("subscribe_sockets", s.hashCode());
                            devices.findOneAndUpdate(filter, update);

//                            dos.writeUTF("Sensor selected");

                            MongoCollection<Document> sensor_data = db.getCollection("sensor_data");

                            // Send latest data in database
                            Bson macFilter = Filters.eq("MAC", selectedSensorMAC);
                            Bson projection = Projections.exclude("_id");
                            MongoCursor<Document> selectedSensorData = sensor_data.find(macFilter).projection(projection).sort(descending("sent_time")).limit(2).iterator();

                            JSONObject result = new JSONObject();
                            while (selectedSensorData.hasNext()) {
                                String data = selectedSensorData.next().toJson();

                                result.append("data", data);
                            }

                            System.out.println(result);
                            dos.writeUTF(result.toString());
                        }
                    } catch (JSONException ignored) {

                    }

                } else if (state == State.WAIT_SENSOR_DATA) {
                    try {
                        Document newDoc = Document.parse(receive);
                        newDoc.append("MAC", connectedDeviceMAC);

                        dos.writeUTF(receive + " OK!");

                        // Get sensor data collection
                        MongoCollection<Document> sensorData = db.getCollection("sensor_data");
                        MongoCollection<Document> devices = db.getCollection("devices");


                        // Save sensor data to collection
                        sensorData.insertOne(newDoc);

                        // When receive new data from sensor, find subscribers to push data
                        Bson filter = Filters.eq("socketId", s.hashCode());
                        Bson projectionFields = Projections.fields(
                                Projections.include("subscribe_sockets"),
                                Projections.excludeId());
                        MongoCursor<Document> cursor = devices.find(filter).projection(projectionFields).iterator();

                        while (cursor.hasNext()) {
                            String device = cursor.next().toJson();
                            JSONObject oj = new JSONObject(device);
                            JSONArray subscribeSockets = null;

                            subscribeSockets = oj.getJSONArray("subscribe_sockets");
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
                    } catch (JsonParseException e) {
                        dos.writeUTF("Invalid data format, please send JSON");

                    } catch (JSONException ignored) {

                    }
                }
            } catch (IOException e) {
                Bson macAddressFilter = Filters.eq("MAC", connectedDeviceMAC);

                // Remove sensor's socket number in database
                Bson removeSocketId = Updates.unset("socketId");
                devicesCollection.findOneAndUpdate(macAddressFilter, removeSocketId);

                if (this.connectedDeviceType == 1) {
                    // Log to terminal to know that sensor has disconnected
                    System.out.println("Sensor with MAC " + connectedDeviceMAC + " has disconnected!");

                    Bson removeSubscribeSocket = Updates.unset("subscribe_sockets");
                    devicesCollection.findOneAndUpdate(macAddressFilter, removeSubscribeSocket);
                } else {
                    System.out.println("Application with MAC " + connectedDeviceMAC + " has disconnected!");
                    System.out.println(selectedSensorMAC);

                    Bson removeApplicationFromSensorSubscribeList = Updates.pull("subscribe_sockets", s.hashCode());

                    // Update sensor which this application subscribes to
                    Bson findSubscribedSensor = Filters.eq("MAC", selectedSensorMAC);

                    // Delete application socket Id from sensor's subscribe list
                    devicesCollection.findOneAndUpdate(findSubscribedSensor, removeApplicationFromSensorSubscribeList);
                }

                synchronized (this) {
                    Server.sockets.remove(this.s);
                }

                break;
            }
        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

