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
	private NumberPicker numPickerHour;
	private NumberPicker numPickerMin;
	private NumberPicker numPickerSecond;

	private void setNumberPickerVisiable(boolean isVisiable){
		int visiable = isVisiable ? View.VISIBLE :View.INVISIBLE;
		numPickerHour.setVisibility(visiable);
		numPickerMin.setVisibility(visiable);
		numPickerSecond.setVisibility(visiable);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		app = (WhereApplication)getApplication();
		switchGPS = (Switch)findViewById(R.id.switch_gps);
		switchAlarm = (Switch)findViewById(R.id.switch_alarm);

		numPickerHour = (NumberPicker)findViewById(R.id.numberPicker_hour);
		numPickerMin = (NumberPicker)findViewById(R.id.numberPicker_min);
		numPickerSecond = (NumberPicker)findViewById(R.id.numberPicker_second);
		
		numPickerHour.setMaxValue(23);
		numPickerMin.setMaxValue(59);
		numPickerSecond.setMaxValue(59);

		int spanSecond  =(int)app.getConfig().getAlarmSpan();
		numPickerHour.setValue(spanSecond / 3600);
		numPickerMin.setValue((spanSecond % 3600 ) / 60);
		numPickerSecond.setValue(spanSecond % 3600 % 60);
		
		setNumberPickerVisiable(app.getConfig().isAlarmOpen());
		
		switchGPS.setChecked(app.getConfig().isGpsOpen());
		switchAlarm.setChecked(app.getConfig().isAlarmOpen());

		OnValueChangeListener numberChangeListener = new OnValueChangeListener(){
			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				int hour = numPickerHour.getValue();
				int min = numPickerMin.getValue();
				int sec  = numPickerSecond.getValue();
				app.getConfig().saveAlarmSpan(hour * 3600 + min *60 + sec);
			}
		};
		
		numPickerHour.setOnValueChangedListener(numberChangeListener);
		numPickerMin.setOnValueChangedListener(numberChangeListener);
		numPickerSecond.setOnValueChangedListener(numberChangeListener);

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
					app.startlAlarm(10);
				else 
					app.stopAlarm();
				setNumberPickerVisiable(isChecked);
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
