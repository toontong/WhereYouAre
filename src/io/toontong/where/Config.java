package io.toontong.where;

import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.baidu.frontia.FrontiaUser;

public class Config {
	private static final String Key_ID = "Social_id";
	private static final String Key_Name = "Social_name";
	private SharedPreferences mSharePF;
	private Context mContext;
	//3875837010
	public Config(Context c){
		 mContext = c;
		 mSharePF = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	public FrontiaUser getUser(){
		String id = mSharePF.getString(Key_ID, "");
		if (id == ""){
			return null;
		}
		FrontiaUser user = new FrontiaUser(id);
		
		String name = mSharePF.getString(Key_Name, "");
		user.setName(name);
		
		return user;
	}	
	
	public void saveUser(String id, String name){
		Editor edit= mSharePF.edit();
		edit.putString(Key_ID, id);
		edit.putString(Key_Name, name);
		edit.commit();
	}
}
