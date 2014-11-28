package io.toontong.where;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import io.toontong.where.R;

/**
 * 显示百度地图与个人位置 本代码大部分来自Baidu的官方demo
 * */
public class MapActivity extends Activity {
	
	// 定位相关
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	public boolean isStartedLocation;

	// UI相关
	private Button requestLocButton;
	private boolean isFirstLoc = true;// 是否首次定位

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		int span = bundle.getInt("span"); // 传过来的单位是second

		
		requestLocButton = (Button) findViewById(R.id.modeBtn);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");

		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = ((WhereApplication)getApplication()).getLocClient();
		mLocClient.registerLocationListener(myListener);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型, 国测局经纬度坐标系：gcj02；百度墨卡托坐标系：bd09;
										// 百度经纬度坐标系：bd09ll

		option.setScanSpan(span * 1000); // 每 (n)ms定位一次

		mLocClient.setLocOption(option);
		mLocClient.requestLocation();
		mLocClient.start();
		isStartedLocation = true;

		// 传入null则，表示使用默认位置图标(蓝色小点)
		mCurrentMarker = null;
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, null));
		
//		MapStatusUpdate update = MapStatusUpdateFactory.zoomBy(10);
//		mBaiduMap.setMapStatus(update);
	}

	public class MyLocationListenner implements BDLocationListener {
		/**
		 * 定位SDK监听函数
		 */
		private static final String TAG = "where.Map.lis"; 
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null) {
				Log.w(TAG, "location == null|| mMapView == null");
				return;
			}

			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					// .direction(100).
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();

			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			Log.d(TAG, "localtion callback." + locData.toString());
		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			Log.w(TAG, "onReceivePoi callback." + poiLocation.toString());
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		mLocClient.unRegisterLocationListener(myListener);
		isStartedLocation = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		mLocClient.registerLocationListener(myListener);
		isStartedLocation = false;
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		isStartedLocation = false;
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mLocClient.unRegisterLocationListener(myListener);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

}
