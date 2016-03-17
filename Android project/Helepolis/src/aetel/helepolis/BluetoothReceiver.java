/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aetel.helepolis;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that scans for Bluetooth devices and add the found ones to the list that will be shown to the user
 * @author Javier Martínez Arrieta
 */
public class BluetoothReceiver extends BroadcastReceiver 
{
    private List<BluetoothDevice> devicesList;
    
    /**
     * Main constructor of the class
     * @param devicesList - List containing device information
     */
    public BluetoothReceiver(List devicesList)
    {
        this.devicesList=devicesList;
    }
    
    /**
     * Method that adds a found device to the list
     * @param context - The application context
     * @param intent - The intent associated with device scan
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action=intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            devicesList.add(device);
        }
    }
    
    /**
     * Returns a list that contains information of the found devices
     * @return devicesList - List containing device information
     */
    public List<BluetoothDevice> getDevicesList()
    {
        return this.devicesList;
    }
    
}
