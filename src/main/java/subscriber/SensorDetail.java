/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subscriber;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.bson.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import subscriber.AplicationState;

import javax.swing.*;

/**
 *
 * @author ADMIN
 */
public class SensorDetail {
    public   String data;
    public String receive;
    public JFrame jFrame;
    public String MAC_Address;
    public Thread thread;
    int x_1 = 80;
    int y_1 = 80;
    int x_2 = 410;
    int y_2 = 80;
    int y_value = 180;
    int x_3 = 730;
    int y_3 = 80;
    public int data_length;
    /**
     * Creates new form DataDetailSensor
     */

    public SensorDetail() throws IOException {
        System.out.println(AplicationState.state);
        initComponents();


    }

    public SensorDetail(String MAC_Address) throws IOException {
        this.MAC_Address = MAC_Address;
        initComponents();
    }

    public String recMessage() throws IOException {
        String result = "";
        if (AplicationState.state == AplicationState.State.WAIT_DATA) {
            receive = AplicationState.recBuff.readUTF();
            result = receive;
        }

        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() throws IOException {

        jFrame = new JFrame("Sensor Detail");
        jFrame.setSize(1000,1000);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.addWindowListener (new WindowAdapter() {
            public void windowClosing (WindowEvent e) {
                try {
                    AplicationState.connection.close();
                    AplicationState.sentBuff.close();
                    AplicationState.recBuff.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                thread.stop();
                jFrame.dispose();

            }
        });

        JLabel jLabel_header = new JLabel("Du lieu cua thiet bi");
        jLabel_header.setBounds(500,20,150,50);
        jFrame.add(jLabel_header, BorderLayout.NORTH);
        JLabel jLabel_no_data = new JLabel();
        if (AplicationState.state == AplicationState.State.SELECT_SENSOR) {
            JSONObject obj = new JSONObject();
            obj.put("MAC", MAC_Address);
            String jsonText = obj.toString();
            AplicationState.sentBuff.writeUTF(jsonText);
            AplicationState.state = AplicationState.State.WAIT_DATA;
            receive = AplicationState.recBuff.readUTF();
            if(receive.equals("{}")) {
                data = "No data";
//                JLabel jLabel_no_data = new JLabel(data);
                jLabel_no_data.setText(data);
                jLabel_no_data.setBounds(500,80,150,50);
                jFrame.add(jLabel_no_data, BorderLayout.NORTH);
            }
            else {

//                Container parent = jLabel_no_data.getParent();
//                parent.remove(jLabel_no_data);
//                parent.validate();
//                parent.repaint();
                data = receive;
                ArrayList<JLabel> jLabels = new ArrayList<JLabel>();
                JSONObject myjson = new JSONObject(data);
                JSONArray the_json_array = myjson.getJSONArray("data");
                int size = the_json_array.length();
                data_length = size;
                for (int i=0; i<size; i++) {
                    String s = the_json_array.getString(i);
                    JSONObject MACS = new JSONObject(s);
                    Iterator<String> keys = MACS.keys();
                    JLabel jl = new JLabel();
                    String textLabel = "<html>";
                    for(String key : MACS.keySet()) {
                        textLabel += key + " :" + MACS.getString(key) + "<br>";
                        System.out.println(textLabel);
                    }
                    jl.setText(textLabel);
                    jLabels.add(jl);
                }

                for(int i=0; i<jLabels.size(); i++) {
                    jLabels.get(i).setHorizontalAlignment(SwingConstants.CENTER);
                    jFrame.add(jLabels.get(i), BorderLayout.CENTER);
                    if(i%3==0) {
                        y_1 = y_1+y_value*(i/3);
                        jLabels.get(i).setBounds(x_1,y_1,240,180);
                    }
                    else if(i%3==1) {
                        y_2 = y_2+y_value*(i/3);
                        jLabels.get(i).setBounds(x_2,y_2,240,180);
                    }
                    else {
                        y_3 = y_3+y_value*(i/3);
                        jLabels.get(i).setBounds(x_3,y_3,240,180);
                    }
                }
            }

        }



        thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                jFrame.remove(jLabel_no_data);
//                jFrame.repaint();
                while (true) {
                    try {
                        String s = recMessage();
                        System.out.println(s);
                        jFrame.remove(jLabel_no_data);
                        jFrame.repaint();
                        JSONObject data_good_time = new JSONObject(s);
                        JLabel jl = new JLabel();

                        jl.setHorizontalAlignment(SwingConstants.CENTER);
                        String textLabel_goo_time = "<html>";
                        for(String key : data_good_time.keySet()) {
                            textLabel_goo_time += key + " :" + data_good_time.getString(key) + "<br>";
                            System.out.println(textLabel_goo_time);
                        }
                        jl.setText(textLabel_goo_time);
                        jFrame.add(jl, BorderLayout.CENTER);
                        if(data_length%3==0) {
                            y_1 = y_1+y_value*(data_length/3);
                            jl.setBounds(x_1,y_1,240,180);
                        }
                        else if(data_length%3==1) {
                            y_2 = y_2+y_value*(data_length/3);
                            jl.setBounds(x_2,y_2,240,180);
                        }
                        else {
                            y_3 = y_3+y_value*(data_length/3);
                            jl.setBounds(x_3,y_3,240,180);
                        }
                        data_length++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    jFrame.setVisible(true);
                }
            }
        });
        thread.start();

        jFrame.setLayout(null);
        jFrame.setVisible(true);


    }// </editor-fold>



    /**
     * @param args the command line arguments
     */

}
