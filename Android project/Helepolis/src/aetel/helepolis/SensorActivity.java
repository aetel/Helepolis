/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aetel.helepolis;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Activity that connects and communicates with the Arduino device.
 * Leds, movements and communication are controlled within this activity
 * @author Javier Martinez Arrieta
 */
public class SensorActivity extends Activity implements Serializable
{
    private BluetoothDevice device;
    private SensorSending sensorSending;
    PowerManager.WakeLock wakeLock;
    Button boton,frontLeds,backLeds,leftRGB,rightRGB;
    ImageView stopButton;
    TextView anglesText;
    SeekBar barRedLeft,barRedRight,barGreenLeft,barGreenRight,barBlueLeft,barBlueRight;
	int frontLedMode;
	int backLedMode;
	int RGBMode;
    int robotMode;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        
        /*Receives data needed from previous activity*/
        Parcelable[] value=this.getIntent().getParcelableArrayExtra("device");
        device=(BluetoothDevice) value[0];
        robotMode=this.getIntent().getIntExtra("robot_mode",0);
        
        /*Removes title part from the activity*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        /*Sets activity's view*/
        setContentView(R.layout.sensor_layout);
        setLayout();
        
        /*Disallows the possibility of locking the screen, avoiding Bluetooth disconnection*/
        PowerManager powerManager=(PowerManager) this.getSystemService(POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"");
        wakeLock.acquire();
        
        /*Creates a new object which is a thread that will communicate*/
        sensorSending=new SensorSending(this, device,robotMode, anglesText,barRedLeft,barRedRight,barGreenLeft,barGreenRight,barBlueLeft,barBlueRight,frontLeds,backLeds,leftRGB,rightRGB);
        
        /*Starts the thread*/
        sensorSending.start();
        
        /*Detects stopButton clicked event and orders the communication to be stopped*/
        stopButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View arg0) 
        	{
        		sensorSending.setExecutionState();
        		sensorSending.stopSending();
        		Toast.makeText(getApplicationContext(), "Stopping...",Toast.LENGTH_LONG).show();
        	}
        });
    }
    
    /**
     * This method initializes the GUI of the activity
     */
    public void setLayout()
    {
    	anglesText=(TextView) findViewById(R.id.valueText);
        barRedLeft=(SeekBar) findViewById(R.id.manual_left_rgb_red);
        barRedRight=(SeekBar) findViewById(R.id.manual_right_rgb_red);
        barGreenLeft=(SeekBar) findViewById(R.id.manual_left_rgb_green);
        barGreenRight=(SeekBar) findViewById(R.id.manual_right_rgb_green);
        barBlueLeft=(SeekBar) findViewById(R.id.manual_left_rgb_blue);
        barBlueRight=(SeekBar) findViewById(R.id.manual_right_rgb_blue);
        stopButton=(ImageView) findViewById(R.id.image_stop_button);
        frontLeds=(Button) findViewById(R.id.front_led);
        backLeds=(Button) findViewById(R.id.back_led);
        leftRGB=(Button) findViewById(R.id.left_rgb_led);
        rightRGB=(Button) findViewById(R.id.right_rgb_led);
    }
    
    /**
     * Disallows screen being always on and orders Bluetooth communication to stop
     * @param keyCode - Code of the key pressed
     * @param event - Event occurred 
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            wakeLock.release();
            sensorSending.setExecutionState();
            sensorSending.stopSending();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Disallows screen being always on and orders Bluetooth communication to stop
     */
    @Override
    public void onDestroy()
    {
    	if(wakeLock.isHeld())
    	{
    		wakeLock.release();
    	}
        sensorSending.setExecutionState();
    	sensorSending.stopSending();
        super.onDestroy();   
    }
}
