/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subscriber;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import subscriber.AplicationState;

/**
 * @author ADMIN
 */
public class ListSensor {
    protected String data;
    public JFrame frame;

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    /**
     * Creates new form ListSensor
     */
    public ListSensor(String data) {
        this.data = data;
        AplicationState.state = AplicationState.State.SELECT_SENSOR;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        frame = new JFrame("List Sensor");
        frame.setSize(600, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    AplicationState.connection.close();
                    AplicationState.sentBuff.close();
                    AplicationState.recBuff.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                frame.dispose();
            }
        });
        ArrayList<JButton> jButtons = new ArrayList<JButton>();
        ArrayList<String> mac_addresses = new ArrayList<String>();
        JSONObject sensor_list_data = new JSONObject(this.data);
        JSONArray sensors = sensor_list_data.getJSONArray("devices");
        int size = sensors.length();
        for (int i = 0; i < size; i++) {
            String s = sensors.getString(i);
            JSONObject MACS = new JSONObject(s);
            String mac = MACS.getString("MAC");
            mac_addresses.add(mac);
            jButtons.add(new JButton(mac));
        }
        JLabel jLabel = new JLabel("Cac thiet bi tren dia ban");
        jLabel.setBounds(230, 20, 150, 50);
        frame.add(jLabel, BorderLayout.NORTH);
        int x_even = 80;
        int y_even = 80;
        int x_odd = 300;
        int y_odd = 80;
        int y_value = 60;
        for (int i = 0; i < jButtons.size(); i++) {
            String mac_address = mac_addresses.get(i);
            jButtons.get(i).setHorizontalAlignment(SwingConstants.CENTER);
            frame.add(jButtons.get(i), BorderLayout.CENTER);
            if (i % 2 == 0) {
                y_even = y_even + y_value * (i / 2);
                jButtons.get(i).setBounds(x_even, y_even, 170, 50);
            } else {
                y_odd = y_odd + y_value * (i / 2);
                jButtons.get(i).setBounds(x_odd, y_odd, 170, 50);
            }

            jButtons.get(i).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    try {
//                        String data = sendMessage(s);
                        new SensorDetail(mac_address).jFrame.setVisible(true);
                        frame.dispose();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        frame.setLayout(null);
        frame.setVisible(true);
    }// </editor-fold>

    /**
     * @param args the command line arguments
     */
}
