package server;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class ServerDuc {
    public final static int DEFAULT_PORT = 14;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String receive;
        String pre = "no";
        String sent;
        try {
            ServerSocket server = new ServerSocket(port);
            Socket connection = null;
            System.out.println("Waiting connection....");
            while (true) {
                connection = server.accept();
                try {
                    while (true) {
                        BufferedReader recBuff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        DataOutputStream sentBuff = new DataOutputStream(connection.getOutputStream());
                        receive = recBuff.readLine();
                        System.out.println("receive from client: " + receive);
                        if (pre.equalsIgnoreCase("no")) {
                            System.out.println("sent to client: Identifying..." + '\n');
                            sentBuff.writeBytes("Accept sensor" + '\n');
                            pre = "yes";
                        } else {
                            sent = receive + " OK!";
                            System.out.println("sent to client:" + Integer.parseInt(receive));
                            sentBuff.writeBytes(sent + '\n');
                        }
                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                } finally {
                    try {
                        if (connection != null) connection.close();
                    } catch (IOException ex) {
                        System.err.println(ex);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}

