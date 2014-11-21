
package io.toontong.where;

import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaApplication;


public class WhereApplication extends FrontiaApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
		
		boolean isInit = Frontia.init(this.getApplicationContext(), ApiKeyConf.BAIDU_APIKEY);
		if (!isInit){
			String err = "Baidu-Frontia.init()->false.";
			Log.e("App", err);
			Toast.makeText(this, err, Toast.LENGTH_LONG).show();
		}
	}
}
