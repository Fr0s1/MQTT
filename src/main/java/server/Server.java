package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// MongoDB import
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;
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
        WAIT_DATA,
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
        String pre = "no";
        String sent;
        State state = State.HANDSHAKE;

        while (true) {
            try {

                receive = dis.readUTF();
                System.out.println("receive from client: " + receive);

                if (state == State.HANDSHAKE) {
                    MongoCollection sensorCollection = db.getCollection("devices");

                    Document newDevice = Document.parse(receive);

                    newDevice.append("socketId", s.hashCode());

                    sensorCollection.insertOne(newDevice);

                    state = State.WAIT_DATA;
                    System.out.println("sent to client: Identifying..." + '\n');
                    dos.writeBytes("Accept sensor" + '\n');
                    pre = "yes";
                } else if (state == State.WAIT_DATA) {
//                    JSONObject data = new JSONObject(receive);
//
//                    String deviceType = data.getString("device_type");
//
//                    if (deviceType.equals("sensor")) {
//                        // Get sensor data collection
//                        MongoCollection sensorData = db.getCollection("sensor_data");
//                        MongoCollection devices = db.getCollection("devices");
//                        Document newDoc = Document.parse(receive);
//
//                        // Save sensor data to collection
//                        sensorData.insertOne(newDoc);
//
//                        // When receive new data from sensor, find subscribers to push data
//                        Bson filter = Filters.eq("socketId", s.hashCode());
//                        Bson projectionFields = Projections.fields(
//                                Projections.include("subscribe_sockets"),
//                                Projections.excludeId());
//                        MongoCursor<Document> cursor = devices.find(filter).projection(projectionFields).iterator();
//
//                        try {
//                            while (cursor.hasNext()) {
//                                String device = cursor.next().toJson();
//                                JSONObject oj = new JSONObject(device);
//
//                                JSONArray subscribeSockets = oj.getJSONArray("subscribe_sockets");
//
//                                for (int i = 0; i < subscribeSockets.length(); i++) {
//                                    int applicationSocketIndex = Server.sockets.indexOf(subscribeSockets.get(i));
//
//                                    Socket applicationSocket = Server.sockets.get(applicationSocketIndex);
//
//                                    DataOutputStream dos = new DataOutputStream(applicationSocket.getOutputStream());
//
//                                    dos.writeUTF(receive);
//                                }
//                            }
//                        } finally {
//                            cursor.close();
//                        }
//                    }
                    sent = receive + " OK!";
                    System.out.println("sent to client:" + Integer.parseInt(receive));
                    dos.writeBytes(sent + '\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

