package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


// MongoDB import
import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Server {
    private static final int SERVER_PORT = 8080;
    private static final String mongodb_host = "localhost";
    private static final int mongodb_port = 27017;
    private static final String mongodb_database = "mqtt";
    private static final String mongodb_uri = String.format("mongodb://%s:%d/%s", mongodb_host, mongodb_port, mongodb_database);

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(SERVER_PORT);
        MongoClient mongoClient = MongoClients.create(mongodb_uri);

        while (true) {
            Socket s = null;

            try {
                System.out.println("Wating for connection...");
                s = ss.accept();
                System.out.println("Socket id " + s.hashCode());
                System.out.println("A new client is connected: " + s);

                DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                Thread t = new ClientHandler(s, dis, dos);

                t.start();
            } catch (IOException e) {
                s.close();
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    static final int BUFFER_SIZE = 4096;

    // Contructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        String received;

        while (true) {
            try {
                received = dis.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

                File f = new File(received.trim());

                if (!f.exists()) {
                    dos.writeUTF("File does not exist. Please re-enter: ");
                } else {
                    long fileLength = f.length();
                    dos.writeUTF("FILE=" + f.getName() + ";FILE_SIZE=" + fileLength);
                    InputStream inputStream = new FileInputStream(received);

                    byte[] buffer = new byte[BUFFER_SIZE];

                    long totalBytesRead = 0;
                    int byteReads;
                    while (totalBytesRead < fileLength) {
                        byteReads = inputStream.read(buffer, 0, BUFFER_SIZE);
                        dos.write(buffer, 0, byteReads);
                        totalBytesRead += byteReads;
                        System.out.println("Sending " + byteReads + " bytes of data");
                    }
                    System.out.println("Sending completed: " + totalBytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}