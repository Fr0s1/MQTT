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
public class ListSensor extends javax.swing.JFrame {
    //   protected ListArea listArea;
    protected String data;
    public final static int sensor = 0;
    public final static String MAC = "22:33:44:55:66:77";
    public String receive;
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
        System.out.println("FROM SERVER: " + receive);

        initComponents();
    }

    public void sendMessage(String MAC_Address) throws IOException {
        String result = "";
        if (AplicationState.state == AplicationState.State.SELECT_SENSOR) {
            JSONObject obj = new JSONObject();
            obj.put("MAC", MAC_Address);
            String jsonText = obj.toString();
            System.out.println(jsonText);
            AplicationState.sentBuff.writeUTF(jsonText);
            receive = AplicationState.recBuff.readUTF();
            AplicationState.state = AplicationState.State.WAIT_DATA;
//            result = receive;
            System.out.println("FROM SERVER: " + receive);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        setSize(600, 500);
        setLayout(new GridLayout((size+1), 1, 5, 5));
        JLabel jLabel = new JLabel("Example");
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        add(jLabel);

        for(int i=0; i<jButtons.size(); i++) {
            String s = macs.get(i);
            jButtons.get(i).setPreferredSize(new Dimension(40, 40));
            jButtons.get(i).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        sendMessage(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            add(jButtons.get(i));
        }
//        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }// </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ListSensor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ListSensor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ListSensor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ListSensor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ListSensor().setVisible(true);
            }
        });
//        new Test();
    }

    // Variables declaration - do not modify

    // End of variables declaration
}