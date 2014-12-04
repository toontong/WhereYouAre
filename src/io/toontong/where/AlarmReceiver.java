package io.toontong.where;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "where.alarm";
	private static long mLastAlarmTime = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		long now = System.currentTimeMillis();
		WhereApplication app = (WhereApplication)context.getApplicationContext();
		long span = app.getConfig().getAlarmSpan() * 1000;

		if(now - app.getLastUpdatePoiTime() < span){
			Log.d(TAG, "update poi just now .");
			return;
		}
		
		
		if ((now - mLastAlarmTime) < span){
			Log.d(TAG, "Alarm Recvier less min-span, do nothing.");
			return;
		}
		
		Log.d(TAG, "进入 AlarmReceiver.onReceive");

		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = SimpleDateFormat.getTimeInstance();
		String nows = dateFormat.format(calendar.getTime());

		mLastAlarmTime = System.currentTimeMillis(); 
		
		if(app  != null){
			app.onMessageReceived(nows);
		}
		
	}
}
