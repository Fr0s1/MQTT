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
        SELECT_SENSOR,
        WAIT_DATA,
    }

    public static void main(String[] args) {
        String receive;
        Scanner sc = new Scanner(System.in);
        String sent;

        try {
            Socket connection = new Socket("localhost", 8080);
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
                    System.out.print("Input from client - location: ");
                    sent = sc.nextLine();

                    if (sent.length() != 0) {
                        sentBuff.writeUTF(sent);
                    }

                    state = State.SELECT_SENSOR;
                } else if (state == State.SELECT_SENSOR) {
                    System.out.print("Input from client - select sensor: ");
                    sent = sc.nextLine();

                    if (sent.length() != 0) {
                        sentBuff.writeUTF(sent);
                    }

                    state = State.WAIT_DATA;
                }


                receive = recBuff.readUTF();

                System.out.println("FROM SERVER: " + receive);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}