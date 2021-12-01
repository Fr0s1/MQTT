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
        String pre = "no";
        Scanner sc = new Scanner(System.in);
        System.out.print("Input from client - location: ");
        String location = sc.nextLine();
        System.out.print("Input from client - HOSTNAME: ");
        String HOSTNAME = sc.nextLine();
        System.out.print("Input from client - PORT: ");
        int PORT = sc.nextInt();

        State state = State.HANDSHAKE;

        try {
            Socket connection = new Socket(HOSTNAME, PORT);
            DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
            BufferedReader recBuff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (true) {
                if (state == State.HANDSHAKE) {
                    JSONObject jo = new JSONObject();
                    jo.put("sensor", sensor);
                    jo.put("MAC", MAC);
                    jo.put("type", type);
                    jo.put("location", location);
                    sentBuff.writeUTF(jo.toString());
                    pre = "yes";
                    state = State.PUBLISH_DATA;
                } else {
                    System.out.print("Input from client: ");
                    int sent = sc.nextInt();
                    sentBuff.writeUTF( String.valueOf(sent));
                }
                receive = recBuff.readLine();
                System.out.println("FROM SERVER: " + receive);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
