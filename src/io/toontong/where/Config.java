package io.toontong.where;

import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.baidu.frontia.FrontiaUser;

public class Config {
	private static final String KEY_ID = "Social_id";
	private static final String KEY_NAME = "Social_name";
	private static final String KEY_ACCESS_TOKEN = "accessToken";
	private static final String KEY_EXPIRES = "Social_Expires";
	private static final String KEY_PLATFORM = "Social_Platform";
	private static final String KEY_USER_POI_ID = "%s_poi_id";

	private SharedPreferences mSharePF;
	private Context mContext;

	public Config(Context c) {
		mContext = c;
		mSharePF = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public FrontiaUser getUser() {
		String id = mSharePF.getString(KEY_ID, "");
		if (id.equals("")) {
			return null;
		}

		FrontiaUser user = new FrontiaUser(id);
		user.setName(mSharePF.getString(KEY_NAME, ""));
		user.setPlatform(mSharePF.getString(KEY_PLATFORM, ""));
		user.setAccessToken(mSharePF.getString(KEY_ACCESS_TOKEN, ""));
		user.setExpiresIn(mSharePF.getLong(KEY_EXPIRES, 0));
		return user;
	}

	public void saveUser(FrontiaUser user) {
		Editor edit = mSharePF.edit();
		edit.putString(KEY_ID, user.getId());
		edit.putString(KEY_NAME, user.getName());
		edit.putString(KEY_PLATFORM, user.getPlatform());
		edit.putString(KEY_ACCESS_TOKEN, user.getAccessToken());
		edit.putLong(KEY_EXPIRES, user.getExpiresIn());
		edit.commit();
	}
	
	/**
	 * 用户调用 POST http://api.map.baidu.com/geodata/v3/poi/create 创建一条POI记录后, 记录到本地
	 * 每个用户最少有一来POI记录
	 * @param user
	 * @param poiId
	 */
	public void saveUserPoiID(FrontiaUser user, String poiId){
		Editor edit = mSharePF.edit();
		edit.putString(String.format(KEY_USER_POI_ID, user.getId()), poiId);
		edit.commit();
	}
	
	/**
	 * @return saveUserPoiID()创建到POI记录ID,已保存到百度LBS云,从本地记录中读取
	 */
	public String getUserPoiID(FrontiaUser user){
		return mSharePF.getString(
				String.format(KEY_USER_POI_ID, user.getId()), "");
	}
	
}
