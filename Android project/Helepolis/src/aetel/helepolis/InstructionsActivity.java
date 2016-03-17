package aetel.helepolis;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Instructions activity
 * This activity shows the user how to control the vehicle movements
 * @author Javier Martínez Arrieta
 */
public class InstructionsActivity extends Activity
{
	ImageView turnImage,pauseImage,forwardImage,backwardsImage;
	
	/**
	 * Method called when activity is first created
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.instructions_layout);
		turnImage=(ImageView) findViewById(R.id.screen_1);
		pauseImage=(ImageView) findViewById(R.id.screen_2);
		forwardImage=(ImageView) findViewById(R.id.screen_3);
		backwardsImage=(ImageView) findViewById(R.id.screen_4);
	}
}
