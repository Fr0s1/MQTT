package subscriber;

// Java implementation for a client
// Save file as Client.java

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class
public class Application {
    static final int SERVER_PORT = 8080;
    static final int BUFFER_SIZE = 4096;
    static String HOST_NAME = "localhost";
    private static String getFileName(String message) {
        return message.replace("FILE=", "");
    }

    private static long getFileSize(String message) {
        return Long.parseLong(message.replace("FILE_SIZE=", ""));
    }

    public static void main(String[] args) throws IOException {
        try {
            Scanner scn = new Scanner(System.in);

            // getting localhost ip
            InetAddress ip = InetAddress.getByName(HOST_NAME);

            // establish the connection with server port 8080
            Socket s = new Socket(ip, SERVER_PORT);
            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while (true) {
                System.out.println("Enter file name to download: ");
                String tosend = scn.nextLine();
                dos.writeUTF(tosend);

                // If client sends exit,close this connection
                // and then break from the while loop
                if (tosend.equals("Exit")) {
                    System.out.println("Closing this connection : " + s);
                    s.close();
                    System.out.println("Connection closed");
                    break;
                }

                // printing date or time as requested by client
                String serverResponse = dis.readUTF();
                System.out.println(serverResponse);

                if (serverResponse.contains("FILE")) {
                    String[] parts = serverResponse.split(";");

                    String fileName = getFileName(parts[0]);
                    long fileSize = getFileSize(parts[1]);

                    byte[] buffer = new byte[BUFFER_SIZE];

                    long totalsWrite = 0;
                    int bytesRead;
                    OutputStream outputStream = new FileOutputStream(fileName);
                    while (totalsWrite < fileSize) {
                        bytesRead = dis.read(buffer, 0, BUFFER_SIZE);
                        System.out.println("Downloaded " + bytesRead + " bytes.");
                        totalsWrite += bytesRead;
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    System.out.println("Writing completed: " + totalsWrite);
                }
            }

            // closing resources
            scn.close();
            dis.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
