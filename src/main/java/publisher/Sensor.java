package publisher;

import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONTokener;

public class Sensor {
    public final static int sensor = 1;
    public final static String MAC = "11:22:33:44:55:66"; // Dịa chỉ MAC của Sensor
    // Dịnh nghĩa trạng thái
    enum State {
        HANDSHAKE,
        PUBLISH_DATA
    }
    public static void main(String[] args) throws FileNotFoundException {
        String receive;
        String resourceName = System.getProperty("user.dir") + "\\src\\main\\java\\publisher\\csvjson.json"; // Lấy bộ dữ liệu từ file json
        Random rd = new Random();
        JSONTokener tokener = new JSONTokener(new FileReader(resourceName)); // Dọc dữ liệu từ json
        JSONArray array = new JSONArray(tokener); // Ghi dữ liệu json vừa đọc vào mảng
        Scanner sc = new Scanner(System.in);
        System.out.print("Input from client - location: ");
        String location = sc.nextLine();
//        System.out.print("Input from client - HOSTNAME: ");
//        String HOSTNAME = sc.nextLine();
//        System.out.print("Input from client - PORT: ");
//        int PORT = sc.nextInt();
//        sc.nextLine();
        State state = State.HANDSHAKE;

        try { // Bắt đầu kết nối socket tới Server
            Socket connection = new Socket("localhost", 8080);
            DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
            DataInputStream recBuff = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            while (true) {
                if (state == State.HANDSHAKE) { // Bắt đầu xác thực sensor
                    JSONObject jo = new JSONObject();
                    jo.put("sensor", sensor);
                    jo.put("MAC", MAC);
                    jo.put("location", location);
                    sentBuff.writeUTF(jo.toString());
                    state = State.PUBLISH_DATA;
                } else if (state == State.PUBLISH_DATA) { //Bắt đầu gửi dữ liệu về server
                    int n = rd.nextInt(array.length()); // Lấy dữ liệu ngẫu nhiên từ bộ dữ liệu json
                    JSONObject object1 = array.getJSONObject(n);
                    String time = (java.time.Clock.systemUTC().instant()).toString();// Ghi thời gian sinh dữ liệu
                    object1.put("sent_time", time);
                    sentBuff.writeUTF(object1.toString());// Lưu dữ liệu vào luồng để gửi đến Server
                    try
                    {
                        Thread.sleep(5000); // Tạo delay giữa các lần sinh dữ liệu
                    }
                    catch(InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
                receive = recBuff.readUTF();// Ghi lại phản hồi từ Server
                System.out.println("FROM SERVER: " + receive);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}