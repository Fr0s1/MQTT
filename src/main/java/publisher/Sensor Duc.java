package publisher;
import org.json.JSONObject;
public class Sensor {
    public static void main(String[] args) {
        JSONObject jo = new JSONObject("{ \"abc\" : \"def\" }");
        System.out.println(jo.toString());
    }
}