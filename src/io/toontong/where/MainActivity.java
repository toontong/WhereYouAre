package io.toontong.where;

import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

//import retrofit.Callback;
//import retrofit.RetrofitError;
//import retrofit.client.Response;

import io.toontong.where.poi.BaiduPoiClient;
import io.toontong.where.poi.CreatePoiResult;
import io.toontong.where.poi.PoiListResult;
import io.toontong.where.poi.Callbacker;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = "Main";

	private TextView mResultTextView;
	private FrontiaUser mUser;
	private Config mConfig;

	private void showText(String msg) {
		Log.e(TAG, msg);
		mResultTextView.setText(msg);
	}

	private void toastMsg(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	/**
	 * 构造广播监听类，监听 SDK key 验证以及网络异常广播
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.d(TAG, "action: " + s);

			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				toastMsg("key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				toastMsg("网络出错");
			}
		}
	}

	private SDKReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConfig = new Config(this);
		mResultTextView = (TextView) findViewById(R.id.textViewResult);

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

	private void setupViews() {
		Button startBtn = (Button) findViewById(R.id.startLocalBtn);
		startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView frequence = (TextView) findViewById(R.id.frequence);
				int span = 3; // default values
				try {
					span = Integer.valueOf(frequence.getText().toString());
				} catch (Exception e) {
					//空输入, 使用默认值
				}

				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				intent.putExtra("span", span);
				startActivity(intent);
			}
		});
		
		
		Button createPoiBtn = (Button) findViewById(R.id.createPoiBtn);
		createPoiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					if( mUser != null){
						BaiduPoiClient.createPoi(114, 35, 1, 
							mUser.getId(), "role", BaiduPoiClient.ROLE_ACL_CREATOR, 
							mUser.getName(), mUser.getPlatform (), 
							new Callbacker<CreatePoiResult>(){
								@Override
								public void onSuccess(CreatePoiResult result){
									switch (result.status){
									case 0:
										mResultTextView.setText(
												"status:" + result.status +"\n"
												+ "message:" + result.message +"\n"
												+ "id:" + result.id);
										mConfig.saveUserPoiID(mUser, result.id);
										break;
									case 3002://唯一索引(userid)重复
										mResultTextView.setText("用户数据已创建．");
										break;
									default:
										mResultTextView.setText(
											"status:" + result.status +"\n"
											+ "message:" + result.message +"\n");
									}
								}

								@Override
								public void onFail(Exception error) {
									mResultTextView.setText(error.toString());
								}
							});
					}else{
						mResultTextView.setText("请先登录!");
					}
				}
			});

		Button lastLocaltionBtn = (Button) findViewById(R.id.myLastLocationBtn);
		lastLocaltionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUser != null) {
					BaiduPoiClient.getPoiByUserId(mUser.getId(),
							new Callbacker<PoiListResult>() {
								@Override
								public void onSuccess(PoiListResult result){
									if (result == null){
										mResultTextView.setText("list-Poi-API reutrn null resutl.");
										return;
									}
									
									mResultTextView.setText(
										"status:" + result.status +"\n"
										+ "message:" + result.message +"\n"
										+ "total:" + result.total + "\n"
										+ "size:" + result.size + "\n");
									if (result.size == 0 || result.pois == null || result.pois.isEmpty()){
										mResultTextView.setText(mResultTextView.getText()
												+ "poi isEmpty.");
									}else{
										PoiListResult.Poi poi = result.pois.get(0);
										
										mResultTextView.setText(mResultTextView.getText()
											+ "poi:[" + poi.location.get(0) + ","
											+ poi.location.get(1) + "]\n" 
											+ "userid:" + poi.userid + "\n"
											+ "role:" + poi.role + "\n"
											+ "role_acl:" + poi.role_acl);
									}
								}
								@Override
								public void onFail(Exception e) {
									mResultTextView.setText(e.toString());
								}
							});
				} else {
					mResultTextView.setText("请先登录!");
				}
			}
		});
	}

	private void showUserInfo() {
		mUser = mConfig.getUser();

		if (null == mUser) {
			Intent intent = new Intent(MainActivity.this, SocialActivity.class);
			startActivity(intent);
		} else {
			Log.d(TAG, "mUser not null");
			showText("Platform:" + mUser.getPlatform() + "\n" + "social id: "
					+ mUser.getId() + "\n" + "social name: " + mUser.getName()
					+ "\n");
		}
	}

	@Override
	protected void onStart() {
		showUserInfo();
		super.onResume();
	}

	@Override
	protected void onResume() {
		showUserInfo();
		super.onResume();
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
