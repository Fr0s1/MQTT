package publisher;

import org.json.JSONObject;

import java.util.Random;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Sensor {
    public final static int sensor = 1;
    public final static String type = "temperature";
    public final static String MAC = "11:22:33:44:55:66";

    enum State {
        HANDSHAKE,
        PUBLISH_DATA
    }

    public static void main(String[] args) {
        String receive;
        Scanner sc = new Scanner(System.in);
        System.out.print("Input from client - location: ");
        String location = sc.nextLine();
//        System.out.print("Input from client - HOSTNAME: ");
//        String HOSTNAME = sc.nextLine();
//        System.out.print("Input from client - PORT: ");
//        int PORT = sc.nextInt();
//        sc.nextLine();
        String sent;
        State state = State.HANDSHAKE;

        try {
            Socket connection = new Socket("localhost", 8080);
            DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
            DataInputStream recBuff = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            while (true) {
                if (state == State.HANDSHAKE) {
                    JSONObject jo = new JSONObject();
                    jo.put("sensor", sensor);
                    jo.put("MAC", MAC);
                    jo.put("type", type);
                    jo.put("location", location);
                    sentBuff.writeUTF(jo.toString());
                    state = State.PUBLISH_DATA;
                } else if (state == State.PUBLISH_DATA) {
                    System.out.print("Input from client: ");

                    sent = sc.nextLine();
                    if (sent.length() != 0) {
                        sentBuff.writeUTF(sent);
                    }
                }
                receive = recBuff.readUTF();
                System.out.println("FROM SERVER: " + receive);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
