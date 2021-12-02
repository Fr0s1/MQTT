package subscriber;

import org.json.JSONObject;

import java.util.Random;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Application {
    public final static int sensor = 0;
    public final static String MAC = "22:33:44:55:66:77";

    enum State {
        HANDSHAKE,
        GET_SENSORS,
        GET_DATA,
    }

    public static void main(String[] args) {
        String receive;
        Scanner sc = new Scanner(System.in);
        System.out.print("Input from client - HOSTNAME: ");
        String HOSTNAME = sc.nextLine();
        System.out.print("Input from client - PORT: ");
        int PORT = sc.nextInt();
        try {
            Socket connection = new Socket(HOSTNAME, PORT);
            DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
            DataInputStream recBuff = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            State state = State.HANDSHAKE;
            while (true) {
                if (state == State.HANDSHAKE) {
                    JSONObject jo = new JSONObject();
                    jo.put("sensor", sensor);
                    jo.put("MAC", MAC);
                    sentBuff.writeUTF(jo.toString());
                    state = State.GET_SENSORS;
                } else if (state == State.GET_SENSORS) {
                    receive = recBuff.readUTF();
                    System.out.println("FROM SERVER: " + receive);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
