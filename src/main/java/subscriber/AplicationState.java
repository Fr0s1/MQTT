package subscriber;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AplicationState {
    public static State state = State.HANDSHAKE;
    public static Socket connection;
    public static DataOutputStream sentBuff;
    public static DataInputStream recBuff;
    static {
        try {
            connection = new Socket("localhost", 8080);
            sentBuff = new DataOutputStream(connection.getOutputStream());
            recBuff = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum State {
        HANDSHAKE,
        GET_SENSORS,
        SELECT_SENSOR,
        WAIT_DATA,
    }
}
