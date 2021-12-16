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
    public final static int sensor = 1; //Application = 0, Sensor = 1
    // Set State for Sensor
    enum State {
        HANDSHAKE,
        PUBLISH_DATA
    }
    public static void main(String[] args) throws FileNotFoundException {
        String receive;
        String resourceName = System.getProperty("user.dir") + "\\src\\main\\java\\publisher\\csvjson.json";// Set location of json data
        Random rd = new Random();
        JSONTokener tokener = new JSONTokener(new FileReader(resourceName));
        JSONArray array = new JSONArray(tokener);
        Scanner sc = new Scanner(System.in);
        System.out.print("Input from client - MAC Address: ");
        String MAC = sc.nextLine();
        System.out.print("Input from client - location: ");
        String location = sc.nextLine();
        State state = State.HANDSHAKE;

        try {
            Socket connection = new Socket("localhost", 8080);
            DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
            DataInputStream recBuff = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            while (true) {
                if (state == State.HANDSHAKE) {
                    // Check Status of sensor for Handshake
                    JSONObject jo = new JSONObject();
                    jo.put("sensor", sensor);
                    jo.put("MAC", MAC);
                    jo.put("location", location);
                    sentBuff.writeUTF(jo.toString());
                    state = State.PUBLISH_DATA;
                } else if (state == State.PUBLISH_DATA) {
                    // Check Status of sensor for Publish data
                    int n = rd.nextInt(array.length());
                    System.out.println("Random: " + n);// Get Random Data
                    JSONObject object1 = array.getJSONObject(n);
                    String time = (java.time.Clock.systemUTC().instant()).toString();
                    object1.put("sent_time", time);// Add time on json
                    sentBuff.writeUTF(object1.toString());//Sent data to Server
                    try {
                        Thread.sleep(5000);// Delay pushing data
                    } catch (InterruptedException ex) {
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
