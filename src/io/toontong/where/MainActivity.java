package io.toontong.where;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.frontia.FrontiaUser;
import com.baidu.mapapi.SDKInitializer;


public class MainActivity extends ActionBarActivity {
	private static final String TAG = "Main"; 
	
	private TextView mResultTextView;
	private FrontiaUser mUser;
	private Button startBtn;
	
	private void showText(String msg){
		Log.e(TAG, msg);
		mResultTextView.setText(msg);
	}

	private void toastMsg(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	/**
	 * 构造广播监听类，监听 SDK key 验证以及网络异常广播
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.d(TAG, "action: " + s);
			TextView text = mResultTextView;
			text.setTextColor(Color.RED);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				text.setText("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
			} else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				text.setText("网络出错");
			}
		}
	}

	private SDKReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mResultTextView = (TextView)findViewById(R.id.textViewResult);

		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
		
		setupViews();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 取消监听 SDK 广播
		unregisterReceiver(mReceiver);
	}

	private void setupViews(){
		startBtn = (Button)findViewById(R.id.startLocalBtn);
		startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					TextView frequence = (TextView)findViewById(R.id.frequence);
					int span = 3;
					try {
						//UI define was second
						span = Integer.valueOf(frequence.getText().toString());
					} catch (Exception e) {
						span = 3; // default values
					}
					
					Intent intent = new Intent(MainActivity.this, MapActivity.class);
					intent.putExtra("span", span);
					startActivity(intent);
				}catch(Exception e) {
					e.printStackTrace();
					toastMsg(e.toString());
				}
			}
		});
		
		Config cfg = new Config(this);
		Log.d(TAG, "net Config()");
		mUser = cfg.getUser();
		
		if (null == mUser){
			Intent intent = new Intent(MainActivity.this, SocialActivity.class);
			startActivity(intent);
		} else {
			Log.d(TAG, "mUser not null");
			showText(
				"social id: " + mUser.getId() + "\n"
				+ "social name: " + mUser.getName() + "\n");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(MainActivity.this, SocialActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
