package io.toontong.where;

import java.util.Map;

import io.toontong.where.poi.Callbacker;
import io.toontong.where.poi.PoiInfoList;
import io.toontong.where.poi.BaiduPoiClient.RoleInfo;
import io.toontong.where.push.Utils;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.frontia.FrontiaUser;
import com.baidu.mapapi.SDKInitializer;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = "where.Main";

	private TextView mResultTextView;
	private ListView mUserListView;  
	private WhereApplication app;

	
	private boolean isWaitingNetwork;
	
	private void showText(String msg) {
		Log.e(TAG, msg);
		mResultTextView.setText(msg);
	}

	private void toastMsg(String msg, int duration) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
				String err = "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置";
				toastMsg(err);
				showText(err);
			} else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				String err = "网络出错";
				toastMsg(err);
				showText(err);
			}
		}
	}

	private SDKReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		app = (WhereApplication)getApplication();
		mResultTextView = (TextView) findViewById(R.id.textViewResult);
		mUserListView = (ListView) findViewById(R.id.userListView);
		if(mUserListView ==null){
			toastMsg("mUserListView is null");
		}
		setupViews();

		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);

		app.setMainActivity(this);
		app.startlAlarm();
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
				switchMapActivity();
			}
		});

		Button createPoiBtn = (Button) findViewById(R.id.createPoiBtn);
		createPoiBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					FrontiaUser user = app.getUser();
					if( user != null){
						switchCreateRoleActivity();
					}else{
						mResultTextView.setText("请先登录!");
					}
				}
			});

		Button myInformationBtnBtn = (Button) findViewById(R.id.myInformationBtn);
		myInformationBtnBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FrontiaUser user = app.getUser();
				RoleInfo roleInfo = app.getRoleInfoFromLocal();
				if (user != null || null != roleInfo) {
					showMyInformation(user, roleInfo);
				} else {
					mResultTextView.setText("请先登录!");
				}
			}
		});
	}

	private void switchLoginActivity(){
		Intent intent = new Intent(MainActivity.this, SocialActivity.class);
		startActivity(intent);
	}

	public void switchCreateRoleActivity(){
		Intent intent = new Intent(MainActivity.this, CreateRoleActivity.class);
		startActivity(intent);
	}
	
	private void switchMapActivity(){
		int span = 2; // default values
		Intent intent = new Intent(MainActivity.this, MapActivity.class);
		intent.putExtra("span", span);
		startActivity(intent);
	}
	
	public void switchSettingsActivity(){
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
	}
	/*
	 * 程序主流程:
	 * 1). 未登录的 --> 先登录
	 * 2). 登录成功 --> 获取最后POI --> 判断是否为已注册用户 --> 有POI是旧用户
	 * 3). 已注册的 --> POI中判断是否有加入组-->没有-->创建或加入一个组
	 * 4). 完成以上三步,进入主流程:
	 *     <1>.启动Push + 启动定位
	 *     <2>.位置发生变化或在设定的时间段内每个时间间隔上报位置(即更新POI)
	 *     <3>.接入到Push消息处理
	 *     <4>. 
	 */
	private void mainProcess() {
		FrontiaUser user = app.getUser();
		if (null == user) {
			this.switchLoginActivity();
			return;
		} 
		
		RoleInfo roleInfo = app.getRoleInfoFromLocal();
		if (roleInfo == null){
			this.switchCreateRoleActivity();
			return;
		}
		
		if(user.getExpiresIn() * 1000 <= System.currentTimeMillis()){
			Log.w(TAG, "accessToken Expires, pls Login.");
			toastMsg("登录信息过期,请重新登录!");
			this.switchLoginActivity();
			return;
		}

		app.startPush();
		showMyInformation(user, roleInfo);
	}

	private void showMyInformation(final FrontiaUser user, final RoleInfo roleInfo){
		if(isWaitingNetwork)
			return;
		isWaitingNetwork = true;
		
		Log.d(TAG, "showMyInformation");
		StringBuffer sb = new StringBuffer();
		
		String userid = Utils.getPushUserId(this);
		String channelId = Utils.getPushChannelId(this);
		sb.append("uid[");
		sb.append(userid);
		sb.append("]chnanelId[");
		sb.append(channelId);
		sb.append("],来自[");
		sb.append(user.getPlatform());
		sb.append("]的登录用户("+app.getPushCount()+"):\n");
		sb.append(user.getName());
		sb.append("\n欢迎使用[Where]位置共享 App.\n");
		mResultTextView.setText(sb.toString());

		app.getRoleMembers(roleInfo.role, new Callbacker<PoiInfoList>(){
			@Override
			public void onSuccess(PoiInfoList result){
				isWaitingNetwork = false;
				if (user != app.getUser()){
					//if user changed, do nothing
					return;
				}
				if (result.status != 0) {
					toastMsg("获取组员信息失败:" + result.message);
					return;
				}

				updateUserListView(roleInfo, result);
			}

			@Override
			public void onFail(Exception e) {
				isWaitingNetwork = false;
				PoiInfoList poiInfos = app.getRoleMembersFromLocal();
				if (poiInfos != null){
					Log.w(TAG, "getRoleMembersFromLocal() cache.");
					updateUserListView(roleInfo, poiInfos);
				} else{
					Log.e(TAG, "on getPoiByUserRole():" + e.toString());
					toastMsg("获取组员信息失败,请稍后再试!0x006", Toast.LENGTH_SHORT);
				}
			}
		});
	}
	
	private void updateUserListView(final RoleInfo roleInfo, PoiInfoList result) {
		StringBuffer sb = new StringBuffer();
		sb.append(mResultTextView.getText());
		sb.append("您所在组为[");
		sb.append(roleInfo.role);
		sb.append("],共有成员[" + result.size + "]人");
		mResultTextView.setText(sb.toString());
		app.saveRoleMembers(result);
		
		UserListViewAdapter uLstView = new UserListViewAdapter(MainActivity.this, result);
		mUserListView.setAdapter(uLstView);
		
		mUserListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView,
					View view, int position, long id) {

				@SuppressWarnings("unchecked")
				Map<String, Object> item = (Map<String, Object>) mUserListView
						.getItemAtPosition(position);

				toastMsg(item.get(UserListViewAdapter.Map_Key_Nickname)
						.toString());
				
				app.testPushMsg();
			}
		});
	}
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mainProcess();
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
			switchSettingsActivity();
			return true;
		}else if(id == R.id.action_switch_account){
			switchLoginActivity();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
