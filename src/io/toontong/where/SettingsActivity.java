package io.toontong.where;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Switch;

public class SettingsActivity extends Activity{
	private static final String TAG = "where.settings";
	private WhereApplication app;
	private Switch switchGPS;
	private Switch switchAlarm;
	private NumberPicker numPicker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		app = (WhereApplication)getApplication();
		switchGPS = (Switch)findViewById(R.id.switch_gps);
		switchAlarm = (Switch)findViewById(R.id.switch_alarm);
		numPicker = (NumberPicker)findViewById(R.id.numberPicker_Alarm);
		
		numPicker.setMaxValue(65535);
		numPicker.setValue((int)app.getConfig().getAlarmSpan() / 1000);
		switchGPS.setChecked(app.getConfig().isGpsOpen());
		switchAlarm.setChecked(app.getConfig().isAlarmOpen());

		switchGPS.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				app.getConfig().saveGps(isChecked);
				if(!isChecked)
					app.stopLocation();
			}
		});
	
		switchAlarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				app.getConfig().saveAlarm(isChecked);
				if(isChecked) 
					app.startlAlarm();
				else 
					app.stopAlarm();
			}
		});

		numPicker.setOnValueChangedListener(new OnValueChangeListener(){
			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				Log.i(TAG, "NumberPicker old(" + oldVal + "),new[" + newVal + "].");
				app.getConfig().saveAlarmSpan(newVal * 1000);
			}
		});

		Button backBtn = (Button)findViewById(R.id.button2);
		backBtn.setVisibility(View.INVISIBLE);
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this,
						MainActivity.class);
				startActivity(intent);
			}
		});
		
	}
}
