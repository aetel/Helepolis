/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aetel.helepolis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import java.util.List;

/**
 * Thread that scans for Bluetooth devices
 * @author Javier Martínez Arrieta
 */
public class DeviceScan extends Thread
{
	/*Bluetooth adapter*/
    private BluetoothAdapter bluetoothAdapter;
    
    /*Thread handler, which is in charge of sending messages*/
    private Handler handler;
    
    /*Flag to indicate that search finished*/
    public static int SEARCH_FINISHED=1;
    
    /**
     * Main constructor of the class
     * @param bluetoothAdapter - The device's Bluetooth adapter
     * @param handler - The thread handler
     */
    public DeviceScan(BluetoothAdapter bluetoothAdapter,Handler handler)
    {
        this.bluetoothAdapter=bluetoothAdapter;
        this.handler=handler;
    }
    
    /**
     * Method that scans for available Bluetooth devices
     */
    @Override
    public void run()
    {
        //while(bluetoothAdapter.getState()==BluetoothAdapter.STATE_TURNING_ON){};
        bluetoothAdapter.startDiscovery();
        //while(bluetoothAdapter.isDiscovering()){};
        bluetoothAdapter.cancelDiscovery();
        Message message=handler.obtainMessage();
        message.arg1=DeviceScan.SEARCH_FINISHED;
        handler.sendMessage(message);
    }
}
