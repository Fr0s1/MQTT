package publisher;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONTokener;

public class Sensor {
    public final static int sensor = 1;
    public final static String type = "temperature";
    public final static String MAC = "11:22:33:44:55:66";

    enum State {
        HANDSHAKE,
        PUBLISH_DATA
    }

    public static void main(String[] args) throws FileNotFoundException {
        String receive;
        String resourceName = System.getProperty("user.dir") + "\\src\\main\\java\\publisher\\csvjson.json";
        Random rd = new Random();
        JSONTokener tokener = new JSONTokener(new FileReader(resourceName));
        JSONArray array = new JSONArray(tokener);
        Scanner sc = new Scanner(System.in);
        System.out.print("Input from client - location: ");
        String location = sc.nextLine();
//        System.out.print("Input from client - HOSTNAME: ");
//        String HOSTNAME = sc.nextLine();
//        System.out.print("Input from client - PORT: ");
//        int PORT = sc.nextInt();
//        sc.nextLine();
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
                    jo.put("location", location);
                    sentBuff.writeUTF(jo.toString());
                    state = State.PUBLISH_DATA;
                } else if (state == State.PUBLISH_DATA) {
                    int n = rd.nextInt(array.length());
                    System.out.println("Random: "+ n );
                    JSONObject object1 = array.getJSONObject(n);
                    String time = (java.time.Clock.systemUTC().instant()).toString();
                    object1.put("sent_time", time);
                    sentBuff.writeUTF(object1.toString());
                    try
                    {
                        Thread.sleep(1500);
                    }
                    catch(InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
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
