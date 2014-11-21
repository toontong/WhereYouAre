package io.toontong.where;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.api.FrontiaAuthorization;
import com.baidu.frontia.api.FrontiaAuthorization.MediaType;
import com.baidu.frontia.api.FrontiaAuthorizationListener.AuthorizationListener;

import io.toontong.where.R;

public class SocialActivity extends Activity{
	private static final String TAG = "Social"; 
	

	
	private FrontiaAuthorization mAuthorization;
	private AuthorizationListener mAuthListener;
	
	private Button backBtn;
	private Button sinaBtn;
	private Button qqBtn;
	private Button baiduBtn;
	
	private TextView mResultTextView;
	
	protected void showText(String msg){
		Log.e(TAG, msg);
		mResultTextView.setText(msg);
	}
	
	private void toastMsg(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_social);
		
		mResultTextView = (TextView)findViewById(R.id.textViewResult);
		if (null == mResultTextView){
			return;
		}

		backBtn = (Button)findViewById(R.id.backBtn);
		qqBtn = (Button)findViewById(R.id.qqBtn);
		sinaBtn = (Button)findViewById(R.id.sinaBtn);
		baiduBtn = (Button)findViewById(R.id.baiduBtn);

		mAuthorization = Frontia.getAuthorization();
		if (mAuthorization == null){
			String err = "Frontia.getAuthorization() -> null. can not login.";
			showText(err);
			return;
		}
		setButtonEvents();
		backBtn.setVisibility(View.INVISIBLE);
	}
	
	private void onLoginSuccess(FrontiaUser user){
	    Frontia.setCurrentAccount(user);
		
		mResultTextView.setText(
		"social id: " + user.getId() + "\n"
		+ "social name: " + user.getName() + "\n"
		+ "token: " + user.getAccessToken() + "\n"
		+ "expired: " + user.getExpiresIn());

		Config cfg = new Config(this);
		cfg.saveUser(user.getId(), user.getName());
		
		backBtn.setVisibility(View.VISIBLE);
		qqBtn.setVisibility(View.INVISIBLE);
		sinaBtn.setVisibility(View.INVISIBLE);
		baiduBtn.setVisibility(View.INVISIBLE);

		toastMsg("登录成功!");
	}
	
	protected void setButtonEvents(){
		mAuthListener = new AuthorizationListener(){
			@Override
			public void onSuccess(FrontiaUser result) {
				onLoginSuccess(result);
			}

			@Override
			public void onFailure(int errCode, String errMsg) {
				if (null != mResultTextView) {
					mResultTextView.setText("errCode:" + errCode
					+ ", errMsg:" + errMsg);
				}
			}

			@Override
			public void onCancel() {
				if (null != mResultTextView) {
					mResultTextView.setText("cancel");
				}
			}
		};
		
		
		sinaBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startSinaLogin();
			}	
		});
		
		
		qqBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startQQZone();
			}	
		});
		
		baiduBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startBaidu();
			}	
		});
	}
	
	private void startSinaLogin(){
		mAuthorization.enableSSO(MediaType.SINAWEIBO.toString(), 
				ApiKeyConf.SINA_APP_KEY);
		mAuthorization.authorize(this,
				FrontiaAuthorization.MediaType.SINAWEIBO.toString(),
				mAuthListener);
	}
	private void startQQZone() {
		mAuthorization.authorize(this, FrontiaAuthorization.MediaType.QZONE.toString(),
				mAuthListener);
	}
	protected void startBaidu() {
		String Scope_Basic = "basic";
//		String Scope_Netdisk = "netdisk";
		ArrayList<String> scope = new ArrayList<String>();
    	scope.add(Scope_Basic);
//    	scope.add(Scope_Netdisk);
		mAuthorization.authorize(this,FrontiaAuthorization.MediaType.BAIDU.toString(),
				scope, mAuthListener);
		}
}
