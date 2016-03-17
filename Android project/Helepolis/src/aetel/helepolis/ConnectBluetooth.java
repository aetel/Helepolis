/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aetel.helepolis;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;

/**
 * Thread that checks if Bluetooth is on at the beginning of the application. In case it is of, it will be turned on.
 * @author Javier Martínez Arrieta
 */
public class ConnectBluetooth extends Thread
{
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private Message message;
    private boolean execute=true;
    public static final int BLUETOOTH_IS_ON=1;
    
    /**
     * Main constructor of the class
     * @param handler - The thread's handler
     */
    public ConnectBluetooth(Handler handler)
    {
        this.handler=handler;
    }
    
    /**
     * Main method of the class, executed when the thread starts. Once Bluetooth is on, a notification will be sent and thread ends. 
     */
    @Override
    public void run()
    {
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.enable();
        }        
        while(execute)
        {
            if(bluetoothAdapter.isEnabled())
            {
                execute=false;
            }
        }
        message=handler.obtainMessage();
        message.arg1=ConnectBluetooth.BLUETOOTH_IS_ON;
        handler.sendMessage(message);
    }
}
