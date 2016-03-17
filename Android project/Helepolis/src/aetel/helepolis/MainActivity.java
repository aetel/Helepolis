package aetel.helepolis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Main activity of the application that will allow to select the mode to be used to control the robot as well as seeing instructions and continue setup
 * @author Javier Martínez Arrieta
 */
public class MainActivity extends Activity
{
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    Button continueButton,findButton,helpButton;
    float x1=0, x2, y1=0, y2,dx=0,dy=0;
    List <BluetoothDevice> devicesList=new ArrayList<BluetoothDevice>();;
    ListView pairedListView,foundListView;
    ArrayAdapter<String> pairedAdapter,foundAdapter;
    ArrayAdapter<String> robotModeSpinnerAdapter;
    String deviceAddress;
    TextView findText,pairedText,adapterItemText;
    boolean bluetoothWasOn=true;
    Spinner robotModeSpinner;
    PowerManager.WakeLock wakeLock;
    List<String> robotOptionsList;
    ImageView imageMain,imageTitle,logo;
    static boolean alreadyStarted,showingMainScreen;
    
    private static final int COWARD_MODE=0;
    private static final int SERIAL_KILLER_MODE=1;
    
  /** 
   * Called when the activity is first created. 
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.start_layout);
      logo=(ImageView) findViewById(R.id.logo);
      if(!alreadyStarted)
      {
	      Handler mHandler = new Handler();
	      Runnable delay = new Runnable() {
	          public void run() 
	          {
	        	 startMainApp();
	        	 showingMainScreen=true;
	          }
	      };
	      mHandler.postDelayed(delay,5000);
	      alreadyStarted=true;
      }
      else
      {
    	  startMainApp();
      }
  }

  /**
   * Once the logo has been shown for a short time, sets layout of the main screen
   */
  public void startMainApp()
  {
      setContentView(R.layout.main);
      bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
      
      /*If a Bluetooth adapter exists, it checks if Bluetooth is already on. If not, it turns it on*/
      if(bluetoothAdapter!=null)
      {
          ConnectBluetooth connectBluetooth;
          final ProgressDialog progressDialog=ProgressDialog.show(this,"","Enabling bluetooth adapter...",true);
          Handler handler=new Handler()
          {
              @Override
              public void handleMessage(Message message)
              {
                  if(message.arg1==ConnectBluetooth.BLUETOOTH_IS_ON)
                  {
                      progressDialog.cancel();
                  }
              }
          };
          connectBluetooth=new ConnectBluetooth(handler);
          connectBluetooth.start();
      }
      imageTitle=(ImageView) findViewById(R.id.letras_titulo);
      imageMain=(ImageView) findViewById(R.id.hele2);
      
      robotModeSpinner=(Spinner) findViewById(R.id.robot_mode_spinner);   
      helpButton=(Button) findViewById(R.id.helpButton);
      helpButton.setOnClickListener(new OnClickListener() 
      {
	      public void onClick(View v)
	      {
	    	  Intent abrirActividadInstrucciones=new Intent(MainActivity.this, InstructionsActivity.class);
	          startActivity(abrirActividadInstrucciones);
	      }
      });
      robotOptionsList=new ArrayList<String>();
      String[] robotOptionsList2=robotOptionsList.toArray(getResources().getStringArray(R.array.robot_options));
      for(int i=0;i<robotOptionsList2.length;i++)
      {
    	  robotOptionsList.add(robotOptionsList2[i]);
      }
      robotModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
      {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
            	((TextView) arg0.getChildAt(0)).setTextColor(Color.WHITE);
            }

            public void onNothingSelected(AdapterView<?> arg0) 
            {

            }
      
      });
      
      //This checks the default Bluetooth adapter exists to continue or not the application
      bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
      if(bluetoothAdapter!=null)
      {
          continueButton=(Button) findViewById(R.id.button);
          continueButton.setOnClickListener(new OnClickListener()
          {
              public void onClick(View v)
              {
                  BluetoothClass.showingFirstScreen(false);
                  //Changes the layout and show the paired devices and the ones found if the users press the button
                  setContentView(R.layout.devices_layout);
                  alreadyStarted=true;
                  showingMainScreen=false;
                  pairedText=(TextView) findViewById(R.id.text);
                  pairedListView=(ListView) findViewById(R.id.pairedDevices);
                  pairedAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1);
                  pairedListView.setAdapter(pairedAdapter);
                  showPairedDevices();
                  findText=(TextView) findViewById(R.id.findText);
                  findButton=(Button) findViewById(R.id.findButton);
                  //This is the listener for the button "Find other devices"
                  findButton.setOnClickListener(new OnClickListener() 
                  {
                      public void onClick(View v) 
                      {
                          pairedText.setVisibility(TextView.GONE);
                          findButton.setVisibility(Button.GONE);
                          pairedListView.setVisibility(ListView.GONE);
                          findText.setVisibility(TextView.VISIBLE);
                          devicesList.removeAll(devicesList);
                          scanDevices();
                          //This is the event listener for the list of found devices
                          foundListView.setOnItemClickListener(new ListView.OnItemClickListener()
                          {
                                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                                {
                                	int robotMode;
                                    bluetoothAdapter.cancelDiscovery();
                                    BluetoothDevice device=devicesList.get(arg2);
                                    if(robotModeSpinner.getSelectedItemPosition()==1)
                                    {
                                    	//Coward mode
                                    	robotMode=COWARD_MODE;
                                    }
                                    else
                                    {
                                        //Serial killer mode
                                    	robotMode=SERIAL_KILLER_MODE;
                                    }
                                    PowerManager powerManager=(PowerManager) MainActivity.this.getSystemService(POWER_SERVICE);  
                                    wakeLock=powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"");
                                    openTilt(device,robotMode);
                                }
                          });
                      }
                  });
                  //This is the event listener for the list of paired devices
                  pairedListView.setOnItemClickListener(new ListView.OnItemClickListener()
                  {
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
                        {
                        	int robotMode;
                        	BluetoothDevice device=devicesList.get(arg2);
                            if(robotModeSpinner.getSelectedItemPosition()==1)
                            {
                                //Coward mode
                            	robotMode=COWARD_MODE;
                            }
                            else
                            {
                                //Serial killer mode
                            	robotMode=SERIAL_KILLER_MODE;
                            }
                            PowerManager powerManager=(PowerManager) MainActivity.this.getSystemService(POWER_SERVICE);  
                            wakeLock=powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"");
                            openTilt(device,robotMode);
                        }
                  });
              }
          });
        }       
        else
        {
            //This is how the application tells the user that his device does not support bluetooth
            AlertDialog alertDialog=new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Sorry. Your device does not support bluetooth. Click the button to close the application");
            alertDialog.setButton("Close",new Dialog.OnClickListener()
            {
                public void onClick(DialogInterface arg0, int arg1)
                {
                    onDestroy();
                }
            });
            alertDialog.show();
        }
  }
  
  /**
   * When the main activity is being created, establishes the adapter where the control mode will be set
   * @param savedInstanceState - Saved state of the application
   */
  public void onActivityCreated(Bundle savedInstanceState)
  {
      robotModeSpinnerAdapter=new ArrayAdapter<String>(getApplicationContext(),R.layout.item,robotOptionsList);
      robotModeSpinner.setAdapter(robotModeSpinnerAdapter);
	}
  
  /**
   * Begins the device scanning if the user clicked "Find other devices" button
   */
  public void scanDevices()
  {
      foundListView=(ListView) findViewById(R.id.foundDevices);
      foundAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1);
      foundListView.setAdapter(foundAdapter);
      IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
      registerReceiver(broadcastReceiver, filter);
      while(bluetoothAdapter.getState()==BluetoothAdapter.STATE_TURNING_ON){};
      bluetoothAdapter.startDiscovery();   
  }
  
  
    /**
    * Its function is to add found devices if the user press the button "Find other devices"
    */
    private final BroadcastReceiver broadcastReceiver=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action=intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devicesList.add(device);
                foundAdapter.add(device.getName()+"\n"+device.getAddress());
                foundAdapter.notifyDataSetChanged();
            }
        }
    };
    
    /**
     * Adds the paired devices to the adapter to be shown
     */
    public void showPairedDevices()
    {
        pairedDevices=bluetoothAdapter.getBondedDevices();
        if(pairedDevices.isEmpty())
        {
            pairedAdapter.add("No paired devices");
            pairedAdapter.notifyDataSetChanged();
        }
        else
        {
            Iterator<BluetoothDevice> iterator=pairedDevices.iterator();
            while(iterator.hasNext())
            {
                BluetoothDevice device=iterator.next();
                devicesList.add(device);
                pairedAdapter.add(device.getName()+"\n"+device.getAddress());
                pairedAdapter.notifyDataSetChanged();
                
            }
        }
    }
    
    /**
     * This method is called when the activity is being destroyed. If so, wakeLock and broadcastReceiver must be released
     */
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if(wakeLock!=null&&wakeLock.isHeld())
    	{
    		wakeLock.release();
    	}
        if(broadcastReceiver.isOrderedBroadcast())
		{
			unregisterReceiver(broadcastReceiver);
		}
        if(!BluetoothClass.getBluetoothWasOn())
        {
            bluetoothAdapter.disable();
        }
    }
    
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if(keyCode==KeyEvent.KEYCODE_BACK&&alreadyStarted&&!showingMainScreen)
    	{
    		startMainApp();
    		showingMainScreen=true;
    	}
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Opens the activity that allows the robot control. It also sends required date to know which mode will be used and which device will be connected
     * @param device - Bluetooth device
     * @param robotMode - Robot control mode
     */
    private void openTilt(BluetoothDevice device,int robotMode)
    {
        Intent openSensorActivity=new Intent(MainActivity.this,SensorActivity.class);
        BluetoothClass.setDevice(device);
        BluetoothClass.setBluetoothAdapter(bluetoothAdapter);
        Parcelable[] value=new Parcelable[2];
        value[0]=device;
        openSensorActivity.putExtra("device", value);
        openSensorActivity.putExtra("robot_mode", robotMode);
        startActivity(openSensorActivity);
    }
    
}