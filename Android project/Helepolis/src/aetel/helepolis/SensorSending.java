/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aetel.helepolis;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.math.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread whose task is to process accelerometer's data as well as communicating with the Arduino device
 * @author Javier Martínez Arrieta
 */
public class SensorSending extends Thread implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int contador=0;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private String currentMove;
    private ByteArrayOutputStream byte2;
    private boolean execute=true,connected=true,functionWasCalled=false,canSound=true,showingToast=false,stop=false;
    private float x,y,z;
    private Context context;
    TextView anglesText;
    int direction=FORWARD;
    int speed,rightWheelSpeed,leftWheelSpeed;
    SeekBar barRedLeft,barRedRight,barGreenLeft,barGreenRight,barBlueLeft,barBlueRight;
    int rightRGBMode=LED_AUTOMATIC,leftRGBMode=LED_AUTOMATIC,valueRedLeft,valueRedRight,valueGreenLeft,valueGreenRight,valueBlueLeft,valueBlueRight=0,frontLedsMode=LED_AUTOMATIC,backLedsMode=LED_AUTOMATIC;
    Button frontLedsButton,backLedsButton,leftRGBButton,rightRGBButton;
    int received=51;
    int robotMode;
    
    OutputStream oStream;
    InputStream iStream;
    
    public static final int LED_AUTOMATIC=2;
    public static final int LED_MANUAL_OFF=0;
    public static final int LED_MANUAL_ON=1;
    
    public static final int FORWARD=1;
    public static final int BACKWARDS=0;
    public static final int STOP=2;
    
	/* Movement constants */
	private static final int MIN_SPEED = 0;
	private static final int MAX_SPEED = 255;
	private static final int MAX_PITCH = 60;
	private static final int MAX_ROLL = 65;
	private static final int MIN_ROLL = -65;
    
    /**
     * Main constructor of the class
     * @param context - The application context
     * @param device - The Bluetooth device
     * @param robotMode - Mode to be used when controlling the robot
     * @param anglesText - Text to be filled with rolled and pitch values
     * @param barRedLeft - Red color intensity for left RGB
     * @param barRedRight - Red color intensity for right RGB
     * @param barGreenLeft - Green color intensity for left RGB
     * @param barGreenRight - Green color intensity for right RGB
     * @param barBlueLeft - Blue color intensity for left RGB
     * @param barBlueRight - Blue color intensity for right RGB
     * @param frontLedsButton - Button that controls the mode of the front leds
     * @param backLedsButton - Button that controls the mode of the back leds
     * @param leftRGBButton - Button that controls the mode of the left RGB led
     * @param rightRGBButton - Button that controls the mode of the right RGB led
     */
    public SensorSending(Context context,BluetoothDevice device,int robotMode,TextView anglesText,SeekBar barRedLeft,SeekBar barRedRight,SeekBar barGreenLeft,SeekBar barGreenRight,SeekBar barBlueLeft,SeekBar barBlueRight,Button frontLedsButton,Button backLedsButton,Button leftRGBButton,Button rightRGBButton)
    {
    	this.anglesText=anglesText;
        this.context=context;
        this.device=device;
        this.robotMode=robotMode;
        this.barRedLeft=barRedLeft;
        this.barRedRight=barRedRight;
        this.barGreenLeft=barGreenLeft;
        this.barGreenRight=barGreenRight;
        this.barBlueLeft=barBlueLeft;
        this.barBlueRight=barBlueRight;
        this.frontLedsButton=frontLedsButton;
        this.backLedsButton=backLedsButton;
        this.leftRGBButton=leftRGBButton;
        this.rightRGBButton=rightRGBButton;
        seekBars(barRedLeft,barRedRight,barGreenLeft,barGreenRight,barBlueLeft,barBlueRight);
        buttons(frontLedsButton,backLedsButton,leftRGBButton,rightRGBButton);
        sensorManager=(SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensores2=sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensores2.size()>0)
        {
            accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        sensorManager.registerListener(SensorSending.this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        
    }
    
    /**
     * Method to detect and handle events related with the buttons available for the user to change settings
     * @param frontLedsButton - Button for front leds control
     * @param backLedsButton - Button for back leds control
     * @param leftRGBButton - Button for left RGB led control
     * @param rightRGBButton - Button for right RGB led control
     */
    public void buttons(Button frontLedsButton,Button backLedsButton,Button leftRGBButton,Button rightRGBButton)
    {
    	frontLedsButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View arg0)
        	{
        		Button b=(Button) arg0;
        		if(b.getText().equals(context.getResources().getString(R.string.front_led_text_automatic)))
        		{
        			b.setText(R.string.front_led_text_manual_on);
        			frontLedsMode=LED_MANUAL_ON;
        		}
        		else
        		{
        			if(b.getText().equals(context.getResources().getString(R.string.front_led_text_manual_on)))
            		{
        				b.setText(R.string.front_led_text_manual_off);
        				frontLedsMode=LED_MANUAL_OFF;
            		}
        			else
        			{
        				b.setText(R.string.front_led_text_automatic);
        				frontLedsMode=LED_AUTOMATIC;
        			}
        		}
        	}
        });
        backLedsButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View arg0)
        	{
        		Button b=(Button) arg0;
        		if(b.getText().equals(context.getResources().getString(R.string.back_led_text_automatic)))
        		{
        			b.setText(R.string.back_led_text_manual_on);
        			backLedsMode=LED_MANUAL_ON;
        		}
        		else
        		{
        			if(b.getText().equals(context.getResources().getString(R.string.back_led_text_manual_on)))
            		{
        				b.setText(R.string.back_led_text_manual_off);
        				backLedsMode=LED_MANUAL_OFF;
            		}
        			else
        			{
        				b.setText(R.string.back_led_text_automatic);
        				backLedsMode=LED_AUTOMATIC;
        			}
        		}
        	}
        });
        
        leftRGBButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View arg0)
        	{
        		Button b=(Button) arg0;
        		if(b.getText().equals(context.getResources().getString(R.string.text_left_RGB_automatic)))
        		{
        			b.setText(R.string.text_left_RGB_manual);
        			rgbLeftLedsOn();
        			leftRGBMode=LED_MANUAL_ON;
        		}
        		else
        		{
        			b.setText(R.string.text_left_RGB_automatic);
        			rgbLeftLedsOff();
        			leftRGBMode=LED_AUTOMATIC;
        		}
        	}
        });
        
        rightRGBButton.setOnClickListener(new OnClickListener()
        {
        	public void onClick(View arg0)
        	{
        		Button b=(Button) arg0;
        		if(b.getText().equals(context.getResources().getString(R.string.text_right_RGB_automatic)))
        		{
        			b.setText(R.string.text_right_RGB_manual);
        			rgbRightLedsOn();
        			rightRGBMode=LED_MANUAL_ON;
        		}
        		else
        		{
        			b.setText(R.string.text_right_RGB_automatic);
        			rgbRightLedsOff();
        			rightRGBMode=LED_AUTOMATIC;
        		}
        	}
        });
    }
    
    /**
     * If for left RGB manual mode is selected, the bars to control each color level will become visible to the user
     */
    public void rgbLeftLedsOn()
    {
    	barRedLeft.setVisibility(SeekBar.VISIBLE);
    	barGreenLeft.setVisibility(SeekBar.VISIBLE);
    	barBlueLeft.setVisibility(SeekBar.VISIBLE);
    }
    
    /**
     * If for left RGB automatic mode is selected, the bars to control each color level will become invisible to the user
     */
    public void rgbLeftLedsOff()
    {
    	barRedLeft.setVisibility(SeekBar.GONE);
    	barGreenLeft.setVisibility(SeekBar.GONE);
    	barBlueLeft.setVisibility(SeekBar.GONE);    	
    }
    
    /**
     * If for right RGB manual mode is selected, the bars to control each color level will become visible to the user
     */
    public void rgbRightLedsOn()
    {
    	barRedRight.setVisibility(SeekBar.VISIBLE);
    	barGreenRight.setVisibility(SeekBar.VISIBLE);
    	barBlueRight.setVisibility(SeekBar.VISIBLE);
    }
    
    /**
     * If for right RGB automatic mode is selected, the bars to control each color level will become invisible to the user
     */
    public void rgbRightLedsOff()
    {
    	barRedRight.setVisibility(SeekBar.GONE);
    	barGreenRight.setVisibility(SeekBar.GONE);
    	barBlueRight.setVisibility(SeekBar.GONE);
    }
    
    /**
     * Method to detect an event in any of the seek bars so as to change the corresponding color value
     * @param barRedLeft - Red color, left RGB
     * @param barRedRight - Red color, right RGB
     * @param barGreenLeft - Green color, left RGB
     * @param barGreenRight - Green color, right RGB
     * @param barBlueLeft - Blue color, left RGB
     * @param barBlueRight - Blue color, right RGB
     */
    public void seekBars(SeekBar barRedLeft,SeekBar barRedRight,SeekBar barGreenLeft,SeekBar barGreenRight,SeekBar barBlueLeft,SeekBar barBlueRight)
    {
    	barRedLeft.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				valueRedLeft=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

        });
        
        barRedRight.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				valueRedRight=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

        });
        
        barGreenLeft.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				valueGreenLeft=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

        });
        
        barGreenRight.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				valueGreenRight=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

        });
        
        barBlueLeft.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				valueBlueLeft=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

        });
        
        barBlueRight.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				valueBlueRight=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

        });
    }
    
    /**
     * This is the main part of the thread, where a socket is created to send the moves to the device, via the socket created
     * In case that any error happens, the thread is stopped
     */
    @Override
    public void run()
    {
    	while(connected)
        {
            try 
            {
                //Creates the socket connection
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                socket = (BluetoothSocket) m.invoke(device,1);
                socket.connect();
                if(socket!=null)
                {
	                while(!socket.isConnected());
	                //Gets the input and output streams created in the socket
	                oStream=socket.getOutputStream();
	                iStream=socket.getInputStream();
	                oStream.write((byte) robotMode);
	                while(iStream!=null&&iStream.available()<=0);
	                while(execute)
	                {
	                	/*Calculates direction and the speed for both wheels*/
	                    synchronized(SensorSending.this)
	                    {
	                        //Sends the next move to the car
	                    	byte2=new ByteArrayOutputStream();
	                    	computeMovement(getRoll(),getPitch());
	                    	System.out.println("pitch: "+getPitch());
	                    	if(getPitch()<-5||getPitch()>5)
	                    	{
	                    	byte2.write((byte) direction);
	                    	byte2.write((byte) leftWheelSpeed);
	                    	byte2.write((byte) rightWheelSpeed);
	                    	}
	                    	else
	                    	{
	                    		/*Makes the robot stop without ending the communication*/
	                    		byte2.write((byte) STOP);
	                        	byte2.write((byte) 0);
	                        	byte2.write((byte) 0);
	                    	}
	                    	byte2.write((byte) frontLedsMode);
	                    	byte2.write((byte) backLedsMode);
	                    	byte2.write((byte) leftRGBMode);
	                    	byte2.write((byte) valueRedLeft);
	                    	byte2.write((byte) valueGreenLeft);
	                    	byte2.write((byte) valueBlueLeft);
	                    	byte2.write((byte) rightRGBMode);
	                    	byte2.write((byte) valueRedRight);
	                    	byte2.write((byte) valueGreenRight);
	                    	byte2.write((byte) valueBlueRight);
	                    	if(oStream!=null)
	                    	{
	                    		oStream.write(byte2.toByteArray());
	                        }
	                        if(currentMove!=null&&stop)
	                        {
	                            execute=false;
	                            connected=false;
	                            stop=false;
	                            if(functionWasCalled)
	                            {
	                                sensorManager.unregisterListener(SensorSending.this,accelerometer);
	                                functionWasCalled=false;
	                            }
	                            stopSending();  
	                        }
	                        while(iStream!=null&&iStream.available()<=0);
	                        try
	                        {
	                            this.wait(100);
	                        }
	                        catch(InterruptedException e)
	                        {
	                        	stopSending();
	                        }
	                    }
	                }
                }
            }
            catch (IOException ex)
            {
            	stopSending();
            }
            catch(NoSuchMethodException e)
            {
            	stopSending();
            }
            catch(IllegalAccessException e)
            {
            	stopSending();
            }
            catch(InvocationTargetException e)
            {
            	stopSending();
            }
        }
    }
    
    /**
     * According to the values of roll and pitch received, it calculates de direction and speed of each wheel
     * @param roll - Roll angle (left and right movements)
     * @param pitch - Pitch angle (forward and backwards movements)
     */
    public void computeMovement(int roll,int pitch) 
    {	
    	/* Set direction */
		if(pitch >= 0) { direction = FORWARD; }
		else { direction = BACKWARDS; }
		
		/*Compute general speed*/
		speed = (int) ((MAX_SPEED / Math.sqrt(MAX_PITCH)) * Math.sqrt(Math.abs(pitch)));
		if(speed > MAX_SPEED) speed = (int) MAX_SPEED;
		else if(speed < MIN_SPEED) speed = (int) MIN_SPEED;
		
		/*Compute each wheel speed according to roll angle*/
		if(roll > MAX_ROLL) roll = MAX_ROLL;
		else if(roll < MIN_ROLL) roll = MIN_ROLL;
		
		/*Right wheel*/
		if(roll > 5) 
		{
			rightWheelSpeed = 255-(int) Math.abs(3.9615*roll);
			leftWheelSpeed = 255-rightWheelSpeed;			
		}
		/*Left wheel*/
		else {
			if(roll < -5)
			{
				leftWheelSpeed = 255-(int) Math.abs(3.9615*roll)/*-(int) Math.abs(0.5*pitch)*/;//(int) ((MAX_SPEED / MAX_ROLL) * Math.sqrt(Math.abs(roll)));//(int) (2 * (roll / MAX_ROLL) * 100);
				rightWheelSpeed = 255-leftWheelSpeed/*-(int) Math.abs(0.5*pitch)*/;				
			}
			else
			{
				leftWheelSpeed = speed;
				rightWheelSpeed = speed;				
			}
		}
		if(rightWheelSpeed>MAX_SPEED)
		{
			rightWheelSpeed = MAX_SPEED;
		}
		else
		{
			if(rightWheelSpeed<MIN_SPEED)
			{
				rightWheelSpeed = MIN_SPEED;
			}
		}
		if(leftWheelSpeed>MAX_SPEED)
		{
			leftWheelSpeed = MAX_SPEED;
		}
		else
		{
			if(leftWheelSpeed<MIN_SPEED)
			{
				leftWheelSpeed = MIN_SPEED;
			}
		}
		System.out.println("Direction[" + direction + "]" + "S[" + speed + "] : L[" + leftWheelSpeed + "] : R[" + rightWheelSpeed + "]");
	}
   
    /**
     * Returns the roll value
     * @return roll - The value of roll angle
     */
    public int getRoll()
    {
    	double angle;
    	angle=(360/(2*Math.PI))*Math.asin((y)/Math.sqrt(Math.pow(x,2)+Math.pow(y,2)));
    	return (int) angle;
    }
    
    /**
     * Returns the pitch value
     * @return pitch - The value of pitch angle
     */
    public int getPitch()
    {
    	double pitch=0;
    	pitch= (360/(2*Math.PI))*Math.asin((z)/Math.sqrt(Math.pow(x,2)+Math.pow(z,2)));
    	return (int) pitch;
    }
    
    /**
     * Method called when and event occurred in the accelerometer sensor
     * @param event - The event that happened
     */
    public void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER&&(currentMove!=null&&stop||currentMove==null))
        {
            
            final float alpha = (float) 0.8;
            float gravity[] = new float[3],linear_acceleration[] = new float[3];
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];
            x=linear_acceleration[0];
            y=linear_acceleration[1];
            z=linear_acceleration[2];
        
            x=event.values[0];
            y=event.values[1];
            z=event.values[2];
        	 
            anglesText.setText("Roll: "+String.valueOf(getRoll())+"\nPitch: "+String.valueOf(getPitch()));
            
            if(contador==0)
            {
                contador++;
            }
            else
            {
            	if(stop&&!showingToast)
            	{
            		Toast.makeText(context,"Stopping...",Toast.LENGTH_LONG).show();
            		showingToast=true;
            	}		
            }
         }
    }

    /**
     * Method empty as it is required due to the implemented interface but not used within this application
     */
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        
    }
    
    /**
     * Method called when it is desired to end the communication with the robot
     */
    public void setExecutionState()
    {
        sensorManager.unregisterListener(SensorSending.this,accelerometer);
        functionWasCalled=true;
        execute=false;
    }
    
    
    /**
     * Closes the communication with the Arduino device, sending stop command and closing the streams and the socket
     */
    public void stopSending()
    {
        stop=true;
        execute=false;
        connected=false;
        if(byte2!=null&&oStream!=null&&iStream!=null)
        {
        	byte2.write((byte) STOP);
	    	byte2.write((byte) 0);
        	byte2.write((byte) 0);
	    	byte2.write((byte) LED_MANUAL_OFF);
	    	byte2.write((byte) LED_MANUAL_OFF);
	    	byte2.write((byte) LED_MANUAL_ON);
	    	byte2.write((byte) 255);
	    	byte2.write((byte) 0);
	    	byte2.write((byte) 0);
	    	byte2.write((byte) LED_MANUAL_ON);
	    	byte2.write((byte) 255);
	    	byte2.write((byte) 0);
	    	byte2.write((byte) 0);
	    	try{
	    	oStream.write(byte2.toByteArray());
			while(iStream.available()==0);	
			} catch (IOException e1) {System.out.println("Se produce error");
			}
        }
    	if (iStream != null) 
	    {
	     	try 
	        {
	           	iStream.close();
	        }
	        catch (Exception e) 
	        {
	               	
	        }
	        iStream = null;
	    }
    	if (oStream != null)
    	{
    		try 
    		{
    			oStream.close();
	        }
    		catch (Exception e) 
    		{
	            	
    		}
    		oStream = null;
	    }
    	if (socket != null)
    	{
    		try
    		{
    			socket.close();
	        }
	        catch (Exception e)
	        {
	            	
	        }
	        socket = null;
	    }
    }
}
