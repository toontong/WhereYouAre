package io.toontong.where.push;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class Utils {
	public static final String TAG = "PushDemoActivity";

	public static String KEY_PUSH_USER_ID = "where.bind_push_userid";
	public static String KEY_PUSH_CHANNEL_ID = "where.bind_push_channel";

	// 用share preference来实现是否绑定的开关。在ionBind且成功时设置true，unBind且成功时设置false
	public static boolean hasBind(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		String userId = sp.getString(KEY_PUSH_USER_ID, "");
		return "".equalsIgnoreCase(userId);
	}

	public static void setBind(Context context, String userId, String channelId) {
		if (userId == null || channelId == null) {
			userId = "";
			channelId = "";
		}
		
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);

		Editor editor = sp.edit();
		editor.putString(KEY_PUSH_USER_ID, userId);
		editor.putString(KEY_PUSH_CHANNEL_ID, channelId);

		editor.commit();
	}

	public static List<String> getTagsList(String originalText) {
		if (originalText == null || originalText.equals("")) {
			return null;
		}
		List<String> tags = new ArrayList<String>();
		int indexOfComma = originalText.indexOf(',');
		String tag;
		while (indexOfComma != -1) {
			tag = originalText.substring(0, indexOfComma);
			tags.add(tag);

			originalText = originalText.substring(indexOfComma + 1);
			indexOfComma = originalText.indexOf(',');
		}

		tags.add(originalText);
		return tags;
	}

	public static String getLogText(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("log_text", "");
	}

	public static void setLogText(Context context, String text) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("log_text", text);
		editor.commit();
	}

}
