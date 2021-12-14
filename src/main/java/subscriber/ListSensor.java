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
 *
 * @author ADMIN
 */
public class ListSensor {
    //   protected ListArea listArea;
    protected String data;
    public final static int sensor = 0;
    public final static String MAC = "22:33:44:55:66:77";
    public String receive;
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
    public ListSensor() {
        AplicationState.state = AplicationState.State.SELECT_SENSOR;
        initComponents();
    }

    public ListSensor(String data)  {
        this.data = data;
        AplicationState.state = AplicationState.State.SELECT_SENSOR;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        frame = new JFrame("List Sensor");
        frame.setSize(600,1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener (new WindowAdapter() {
            public void windowClosing (WindowEvent e) {
//                System.out.println("close");
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
        ArrayList<String> macs = new ArrayList<String>();
        JSONObject myjson = new JSONObject(this.data);
        JSONArray the_json_array = myjson.getJSONArray("devices");
        int size = the_json_array.length();
        for (int i=0; i<size; i++) {
            String s = the_json_array.getString(i);
            JSONObject MACS = new JSONObject(s);
            String mac = MACS.getString("MAC");
            macs.add(mac);
            jButtons.add(new JButton(mac));
        }
        JLabel jLabel = new JLabel("Cac thiet bi tren dia ban");
        jLabel.setBounds(230,20,150,50);
        frame.add(jLabel, BorderLayout.NORTH);
       int x_chan = 80;
       int y_chan = 80;
       int x_le = 300;
       int y_le = 80;
       int y_value = 60;
        for(int i=0; i<jButtons.size(); i++) {
            String s = macs.get(i);
            jButtons.get(i).setHorizontalAlignment(SwingConstants.CENTER);
            frame.add(jButtons.get(i), BorderLayout.CENTER);
            if(i%2==0) {
                y_chan = y_chan+y_value*(i/2);
                jButtons.get(i).setBounds(x_chan,y_chan,170,50);
            }
            else {
                y_le = y_le+y_value*(i/2);
                jButtons.get(i).setBounds(x_le,y_le,170,50);
            }

            jButtons.get(i).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    try {
//                        String data = sendMessage(s);
                        new Test(s).jFrame.setVisible(true);
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
