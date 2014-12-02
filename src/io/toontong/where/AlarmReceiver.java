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
		WhereApplication app = (WhereApplication)context.getApplicationContext();
		
		if (System.currentTimeMillis() - mLastAlarmTime < app.getConfig().getAlarmSpan()){
			Log.d(TAG, "Alarm Recvier less min-span, do nothing.");
			return;
		}
		
		Log.d(TAG, "进入 AlarmReceiver.onReceive");

		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = SimpleDateFormat.getTimeInstance();
		String now = dateFormat.format(calendar.getTime());

		mLastAlarmTime = System.currentTimeMillis(); 
		
		if(app  != null){
			app.onMessageReceived(now);
		}
		
	}
}
