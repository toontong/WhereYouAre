package io.toontong.where;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.frontia.Frontia;
import com.baidu.frontia.FrontiaApplication;
import com.baidu.frontia.FrontiaUser;
import com.baidu.frontia.api.FrontiaPush;

import io.toontong.where.poi.BaiduPoiClient;
import io.toontong.where.poi.BaiduPoiClient.EnumRole;
import io.toontong.where.poi.BaiduPoiClient.RoleInfo;
import io.toontong.where.poi.Callbacker;
import io.toontong.where.poi.CreatePoiResult;
import io.toontong.where.poi.PoiInfoList;
import io.toontong.where.poi.PoiInfoList.PoiInfo;
import io.toontong.where.poi.UpdatePoiResult;


public class WhereApplication extends FrontiaApplication {
	private static final String TAG = "Wher.App";
	
	private FrontiaUser mUser;
	private Config mConfig;
	
	private BaiduPoiClient mBDPoiCli;
	private MainActivity mMainActivity;
	private MapActivity mMapActivity;
	private CreateRoleActivity mRoleActivity; 

	// 定位相关
	private LocationClient mLocClient;
	private boolean mGetPushMsg;
	
	private PoiInfo mLastPoi;
	private long mLastUpdatePoiTime;
	private static final long Update_Span = 30 * 1000; // 30 second
	private RoleInfo mRoleInfo;

	private boolean mIsGettingPoi; // 当调用网络API-Get-Poi时,可能比较耗时,防止并发

	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);

		boolean isInit = Frontia.init(this.getApplicationContext(),
				ApiKeyConf.BAIDU_APIKEY);

		if (!isInit) {
			String err = "Baidu-Frontia.init()->false.";
			Log.e(TAG, err);
			toastMsg(err);
		}
		mIsGettingPoi = false;
		mConfig = new Config(this);
		mLocClient = new LocationClient(this);
		mBDPoiCli = new BaiduPoiClient(ApiKeyConf.BAIDU_ACCESS_KEY, ApiKeyConf.BAIDU_GEO_TABLE_ID);
		mLastUpdatePoiTime = 0;

		mUser = mConfig.getUser(); // maybe return null

		MyLocationListenner myListener = new MyLocationListenner();
		mLocClient.registerLocationListener(myListener);
		
		CrashHandler handler = CrashHandler.getInstance();
		handler.init(getApplicationContext());
	}
	
	private void toastMsg(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	public void onLoginSuccess(FrontiaUser user){
		mUser = user;
		mConfig.saveUser(user);
		getUserPoi(new Callbacker<PoiInfo>() {
			@Override
			public void onSuccess(PoiInfo result){
				//TODO:;
			}
			
			@Override
			public void onFail(Exception e) {
				//TODO:;
				Log.e(TAG, "on getUserPoiByUserId:" + e.toString());
			}
		});
	}

	/**
	 * @return null if user did no Login-Auth.
	 */
	public FrontiaUser getUser(){
		return mUser;
	}

	/**
	 * 尝试从本地持久化数据获取
	 * @return
	 */
	public RoleInfo getRoleInfoFromLocal(){
		if(null == mUser)
			return null;
		mRoleInfo = mConfig.getRoleInfo(Long.valueOf(mUser.getId()));
		return mRoleInfo;
	}

	public LocationClient getLocClient(){
		// 给map-activity用
		return mLocClient;
	}
	
	public void setMainActivity(MainActivity acvitity){
		mMainActivity = acvitity;
	}
	public void setCreateRoleActivity(CreateRoleActivity acvitity){
		mRoleActivity = acvitity;
	}
	
	/**
	 * 登录成功后尝试从LBS云中获取用户的POI
	 */
	private void getUserPoi(final Callbacker<PoiInfo> callback){
		if(mIsGettingPoi){
			return;
		}

		if (null == mBDPoiCli){
			Log.w(TAG, "mBDPoiCli is null. on getUserPoi().");
			toastMsg("mBDPoiCli is null.");
			return;
		}
		if (mUser == null){
			Log.w(TAG, "mUser is null on getUserPoi().");
			toastMsg("请先登录.0x001");
			return;
		}
		mIsGettingPoi = true;
		mBDPoiCli.getPoiByUserId(mUser.getId(),
			new Callbacker<PoiInfoList>() {
				@Override
				public void onSuccess(PoiInfoList result){
					mIsGettingPoi = false;
					if (result == null || result.total <= 0 ||
						result.pois == null || result.pois.isEmpty()){
						mMainActivity.switchCreateRoleActivity();
						toastMsg("请先创建或加入一个组织.");
						return;
					}
					if (result.total >= 0 && result.size <= 0){
						Log.e(TAG, "肯定是百度LBC云出错了.不可能的事阿... >_<");
					}
					if (result.status != 0){
						Log.e(TAG, "百度LBC云返回出来.非零的sttus没说明什么错... >_<");
						toastMsg("从云端获取地理信息失败,可能网络不后,请稍后再试.");
						return;
					}

					// 取最新一条数据
					mLastPoi = result.pois.get(result.size - 1);
					if (mLastPoi == null || mUser == null)return;

					// 持久化到本地先.
					mRoleInfo = mConfig.saveRoleInfo(mLastPoi.userid, mLastPoi.id, 
							mLastPoi.role, mLastPoi.role_acl);
					callback.onSuccess(mLastPoi);
				}
				@Override
				public void onFail(Exception e) {
					mIsGettingPoi = false;
					Log.e(TAG, e.toString());
					toastMsg("网络不可,获取Poi失败,原因:" + e.toString());
					callback.onFail(e);
				}
		});
	}
	
	/**
	 * 用户第一次登录成功后,在LBS云中创建一条记录,同时相当于新注册用户
	 * @param poiID
	 * @return
	 */
	public boolean createUser(String role, EnumRole acl){
		if (mUser == null){
			Log.w(TAG, "mUser is null on createUser().");
			toastMsg("请先登录.0x002");
			return false;
		}
		if (null == mBDPoiCli){
			Log.w(TAG, "mBDPoiCli is null. on createUser().");
			toastMsg("mBDPoiCli is null.");
			return false;
		}

		double lat, lng; 
		if (null != mLastPoi && mLastPoi.location.size() == 2){
			lat = mLastPoi.location.get(0);
			lng = mLastPoi.location.get(1);
		} else{
			lat = lng = 0.0;
		}
		
		mBDPoiCli.createPoi(lat, lng, 1, //使用一个未知经伟度 
				mUser.getId(), role, acl, 
				mUser.getName(), mUser.getPlatform (), 
				new Callbacker<CreatePoiResult>(){
					@Override
					public void onSuccess(CreatePoiResult result){
						if (mUser == null){
							//用户可能在这段时间内退出了,TODO:重新登录怎么办?
							return;
						}
						switch (result.status){
						case 0: //success
							mConfig.saveUserPoiID(Long.valueOf(mUser.getId()), result.id);
							mRoleActivity.switchMainActivity();
							break;
						case 3002://唯一索引(userid)重复"用户数据已创建．0x002"

							mRoleActivity.showText("尝试获取Poi数据.");

							getUserPoi(new Callbacker<PoiInfo>() {
								@Override
								public void onSuccess(PoiInfo poi){
									mRoleActivity.showText("获取成功,现在你您可以与组[" 
										+ poi.role + "]共享位置了.");
								}
								@Override
								public void onFail(Exception e) {
									mRoleActivity.showText(e.toString());
								}
							});
							break;
						default:
							Log.e(TAG, "unknow error on createPoi,status:" + result.status
									+ "message:" + result.message);
							toastMsg("网络不可,创建失败,请稍后再试.0x002");
						}
					}

					@Override
					public void onFail(Exception e) {
						Log.e(TAG, e.toString());
						toastMsg("网络不可,创建失败,原因:" + e.toString());
					}
				});
		
		return true;
	}
	
	public void onMessageReceived(String msg){
		// 收到push的消息
		Log.d(TAG, msg);
		
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		mLocClient.start();
		mGetPushMsg = true;
		
	}

	private class Config {
		//只有app才知道持久化配置信息 
		private static final String KEY_ID = "Social_id";
		private static final String KEY_NAME = "Social_name";
		private static final String KEY_ACCESS_TOKEN = "accessToken";
		private static final String KEY_EXPIRES = "Social_Expires";
		private static final String KEY_PLATFORM = "Social_Platform";
		private static final String KEY_USER_POI_ID = "%d_poi_id";
		private static final String KEY_USER_ROLE = "%d_role";
		private static final String KEY_USER_ROLE_ACL = "%d_role_acl";
	
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
		
		public RoleInfo saveRoleInfo(
				long userid, long poiId,     // 对一个用户,这两个值是不变的
				String role, String roleAcl){ // 用户可以修改组或创建新组
			Editor edit = mSharePF.edit();
			edit.putLong(String.format(KEY_USER_POI_ID, userid), poiId);
			edit.putString(String.format(KEY_USER_ROLE, userid), role);
			edit.putString(String.format(KEY_USER_ROLE_ACL, userid), roleAcl);
			edit.commit();
			return new RoleInfo(userid, poiId, role, EnumRole.toValue(roleAcl));
		}
		
		public RoleInfo getRoleInfo(long userid){
			long poiId = mSharePF.getLong(String.format(KEY_USER_POI_ID, userid), 0);
			
			if (0 == poiId){
				return null;
			}
			
			String role = mSharePF.getString(String.format(KEY_USER_ROLE, userid), "");
			String acl = mSharePF.getString(String.format(KEY_USER_ROLE_ACL, userid), "");
			return new RoleInfo(userid, poiId, role, EnumRole.toValue(acl));
		}

		/**
		 * 用户调用 POST http://api.map.baidu.com/geodata/v3/poi/create 创建一条POI记录后, 记录到本地
		 * 每个用户最少有一来POI记录
		 * @param user
		 * @param poiId
		 */
		public void saveUserPoiID(long userid, long poiId){
			Editor edit = mSharePF.edit();
			edit.putLong(String.format(KEY_USER_POI_ID, userid), poiId);
			edit.commit();
		}
 
	}
	
	public void startPush() {
		FrontiaPush bdPush =  Frontia.getPush();

		if (null == mUser){
			PushManager.startWork(getApplicationContext(),
	                PushConstants.LOGIN_TYPE_API_KEY,
	                ApiKeyConf.BAIDU_APIKEY);
			Log.d(TAG, "start Push with API-KEY.");
		} else {
			bdPush.start(mUser.getAccessToken());
			Log.d(TAG, "start Push with userAccessToken.");
		}
		if(!bdPush.isPushWorking()){
			bdPush.resume();
		}
	}
	public void onLocation(final BDLocation location){
		if (System.currentTimeMillis() - mLastUpdatePoiTime < Update_Span){
			return;
		}
		if (mUser == null){
			toastMsg("先登录.0x005");
			return;
		}

		if (mRoleInfo == null){
			toastMsg("mRoleInfo is null, pls call getRoleInfo() or getUserLastPoiData()");
			return;
		}
		String role = mRoleInfo.role;
		String roleAcl = EnumRole.toString(mRoleInfo.roleAcl);

		mBDPoiCli.updatePoi(
				location.getLatitude(),
				location.getLongitude(),
				1, //Coor_Type
				mUser.getId(),
				role,
				roleAcl,
				mUser.getName(),
				new Callbacker<UpdatePoiResult>() {
					@Override
					public void onSuccess(UpdatePoiResult result){
						//TODO:;
						if (result.status == 0){
							mLastUpdatePoiTime = System.currentTimeMillis();
							if(mLastPoi == null){
								mLastPoi = new PoiInfo();
								mLastPoi.id = result.id;
								mLastPoi.location = new ArrayList<Double>();
							}
							
							mLastPoi.location.add(0, location.getLatitude());
							mLastPoi.location.add(1, location.getLongitude());
							
							if(mGetPushMsg){
								mGetPushMsg = false;
								if(!mMapActivity.isStartedLocation){
									mLocClient.stop();
								}
							}

						}else{
							Log.e(TAG, "unknow error on updatePoi():" + result.status
									+ "message:" + result.message);
							toastMsg("更新位置失败:" + result.message);
						}
					}
					
					@Override
					public void onFail(Exception e) {
						//TODO:;
						Log.e(TAG, "on updatePoi():" + e.toString());
						toastMsg("updatePoi().onFail 更新位置失败:" + e.toString());
					}
				});
		Log.d(TAG, "end app.onLocation()");
	}
	

	private class MyLocationListenner implements BDLocationListener {
		
		
		/**
		 * 定位SDK监听函数
		 */
		@Override
		public void onReceiveLocation(BDLocation location) {
			LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
	
			Log.e(TAG, "LocationListenner callback." + ll.toString());

			WhereApplication.this.onLocation(location);
		}
	
		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			Log.e(TAG, "onReceivePoi callback." + poiLocation.toString());
		}
	}
	
}
