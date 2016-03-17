/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aetel.helepolis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * This class is used to store data relatid with Bluetooth such as if Bluetooth was active before starting the application
 * @author Javier Martínez Arrieta
 */
public class BluetoothClass 
{
    private static BluetoothDevice device;
    private static BluetoothAdapter adapter;
    private static boolean showingMyView,bluetoothWasOn,showingFirstScreen;
    
    /**
     * Sets the Bluetooth device
     * @param device - Bluetooth device
     */
    public static void setDevice(BluetoothDevice device)
    {
        BluetoothClass.device=device;
    }
    
    /**
     * Returns the Bluetooth device
     * @return device - Bluetooth device
     */
    public static BluetoothDevice getBluetoothDevice()
    {
        return BluetoothClass.device;
    }
    
    
    public static void setMyViewState(boolean showingMyView)
    {
        BluetoothClass.showingMyView=showingMyView;
    }
    
    /**
     * Returns if Bluetooth was on before application start
     * @return - true if Bluetooth was on, false otherwise
     */
    public static boolean getMyViewState()
    {
        return BluetoothClass.showingMyView;
    }
    
    /**
     * Returns if Bluetooth was on before application start
     * @return - true if Bluetooth was on, false otherwise
     */
    public static boolean getBluetoothWasOn()
    {
        return BluetoothClass.bluetoothWasOn;
    }
    
    /**
     * Sets if Bluetooth was on before application start
     * @param bluetoothWasOn - true if Bluetooth was on, false otherwise
     */
    public static void setBluetoothWasOn(boolean bluetoothWasOn)
    {
        BluetoothClass.bluetoothWasOn=bluetoothWasOn;
    }
    
    /**
     * Sets Bluetooth adapter
     * @param adapter - Bluetooth adapter
     */
    public static void setBluetoothAdapter(BluetoothAdapter adapter)
    {
        BluetoothClass.adapter=adapter;
    }
    
    /**
     * Disables Bluetooth adapter
     */
    public static void disableBluetoothAdapter()
    {
        BluetoothClass.adapter.disable();
    }
    
    /**
     * Sets if first view is shown
     * @param showingFirstScreen - True if showing main screen, false otherwise
     */
    public static void showingFirstScreen(boolean showingFirstScreen)
    {
        BluetoothClass.showingFirstScreen=showingFirstScreen;
    }
    
    /**
     * Returns if first screen is shown
     * @return showingFirstScreen - True if showing main screen, false otherwise
     */
    public static boolean isShowingFirstScreen()
    {
        return BluetoothClass.showingFirstScreen;
    }
    
}
