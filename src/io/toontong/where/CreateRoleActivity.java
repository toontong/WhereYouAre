package io.toontong.where;

import io.toontong.where.poi.BaiduPoiClient.EnumRole;

import com.baidu.frontia.FrontiaUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CreateRoleActivity extends Activity{
	private static final String TAG = "where.role";
	private TextView mResultTextView;
	private TextView mEditorRoleName;
	private WhereApplication app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_role);
		
		app = (WhereApplication)getApplication();
		mResultTextView = (TextView)findViewById(R.id.textViewInfomation);
		mEditorRoleName = (TextView)findViewById(R.id.roleNameInput);
		
		app.setCreateRoleActivity(this);
		setupButtonEvents();
	}
	
	public void showText(String txt){
		Log.d(TAG, txt);
		mResultTextView.setText(txt);
	}

	private void toastMsg(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private String getRoleName() {
		String role = mEditorRoleName.getText().toString();
//		String role = edit.toString();
		if(role.isEmpty()){
			showText("组名不能为空.");
			return "";
		}
		if(role.length()<4){
			showText("组名要求少4个字符.");
			return "";
		}
		return role;
	}
	
	void setupButtonEvents(){
		Button createRoleBtn = (Button) findViewById(R.id.createRoleButton);
		createRoleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					String role = getRoleName();
					if(role.equals(""))
						return;
					
					FrontiaUser user = app.getUser();
					if( user != null){
						showText("正在尝试创建...");
						app.createUser(role, EnumRole.Creator);
						
					}else{
						toastMsg("请先登录!0x003");
						switchLoginActivity();
					}
				}

			});
		
		Button joinRoleBtn = (Button) findViewById(R.id.joinRoleBtn);
		joinRoleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String role = getRoleName();
				if(role.equals(""))
					return;
				
				FrontiaUser user = app.getUser();
				if( user != null){
					app.createUser(role, EnumRole.Member);
					showText("正在尝试加入...");
				}else{
					toastMsg("请先登录!0x004");
					switchLoginActivity();
				}
			}
		});
		Button openMapBtn = (Button) findViewById(R.id.openMapBtn);
		openMapBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				switchMapActivity();

				FrontiaUser user = app.getUser();
				if( user == null){
					toastMsg("未登录用户,位置只能自己看到!0x005");
				}
			}
		});
	}

	private void switchLoginActivity(){
		Intent intent = new Intent(CreateRoleActivity.this,
				SocialActivity.class);
		startActivity(intent);
	}
	
	private void switchMapActivity(){

		int span = 3; // default values
		Intent intent = new Intent(CreateRoleActivity.this, MapActivity.class);
		intent.putExtra("span", span);
		startActivity(intent);
	}
	public void switchMainActivity(){
		Intent intent = new Intent(CreateRoleActivity.this, MainActivity.class);
		startActivity(intent);
	}
}
