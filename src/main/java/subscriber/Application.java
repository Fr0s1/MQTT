package subscriber;

import org.json.JSONObject;

import java.util.Random;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Application {
    public final static int sensor = 0;
    public final static String MAC = "22:33:44:55:66:77";

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
        try {
            Socket connection = new Socket(HOSTNAME, PORT);
            DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
            BufferedReader recBuff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (true) {
                if (pre.equalsIgnoreCase("no")) {
                    JSONObject jo = new JSONObject();
                    jo.put("sensor", sensor);
                    jo.put("MAC", MAC);
                    sentBuff.writeUTF(jo.toString());
                    pre = "yes";
                } else {
                    System.out.print("Input from client: ");
                    Random rd = new Random();
                    int sent = rd.nextInt(60);
                    //sentBuff.writeBytes( String.valueOf(sent) + '\n');
                }
                receive = recBuff.readLine();
                System.out.println("FROM SERVER: " + receive);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
