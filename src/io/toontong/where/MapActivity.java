package io.toontong.where;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import io.toontong.where.R;
import io.toontong.where.poi.PoiInfoList;
import io.toontong.where.poi.PoiInfoList.PoiInfo;

/**
 * 显示百度地图
 * */
public class MapActivity extends Activity {
	private static final String TAG = "where.map";
	// 定位相关
	private LocationClient mLocClient;
	private MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private WhereApplication app;

	// UI相关
	private LinearLayout mUserGallery;
	private Button requestLocButton;
	private boolean isFirstLoc = true;// 是否首次定位

	private static final BitmapDescriptor IconMarker = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_marker);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		app = ((WhereApplication)getApplication());
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		int span = bundle.getInt("span"); // 传过来的单位是second , 默认3秒

		mUserGallery = (LinearLayout) findViewById(R.id.userHoriLayout);
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
		
		mBaiduMap.setMyLocationEnabled(true);// 开启定位图层
		
		// 定位初始化
		mLocClient = app.getLocClient();
		mLocClient.registerLocationListener(myListener);
		app.startLocation(span);

		// 传入null则，表示使用默认位置图标(蓝色小点)
		mCurrentMarker = null;
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, null));
		
		createUserButton(app.getRoleMembersFromLocal());
		
		PoiInfo poi = app.getMapCenterPoi();
		isFirstLoc = (poi == null);
		setMapCenter(poi);
		//TODO: 定时更新所有人位置?还是点击时去取???
	}
	
	private void createUserButton(PoiInfoList poiInfos ){
		if(poiInfos == null)
			return;

		for (int i = 0; i < poiInfos.size; i++) {
			final PoiInfo poi = poiInfos.pois.get(i);
			if(null == poi) continue;

			final LatLng latlng = new LatLng(poi.location.get(1), 
					poi.location.get(0));
			LinearLayout layout = createUserView(poi);
			mUserGallery.addView(layout);
			
			layout.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					setMapCenter(poi);
					
					//TODO: get user newer PoiInfo from the LBS server.
				}
			});
			
			OverlayOptions op = new MarkerOptions().position(latlng).icon(IconMarker)
					.perspective(false).zIndex(i + 5);
			mBaiduMap.addOverlay(op);
			Log.i(TAG, "add user" + poi.userid);
		}
	}

	private LinearLayout createUserView(final PoiInfo poi){
		//TODO: 把头像改成圆角图
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setGravity(Gravity.CENTER);
		layout.setPadding(10, 0, 0, 0);
		layout.setOrientation(LinearLayout.VERTICAL);

		ImageView imageView = new ImageView(getApplicationContext());
		imageView.setMaxHeight(80);
		imageView.setMaxWidth(80);
		imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		imageView.setImageResource(R.drawable.head_00 + (int)(poi.userid % 10));
//		imageView.setBackgroundResource(R.drawable.head_00 + (int)(poi.userid % 10));

		TextView txtName = new TextView(getApplicationContext());
		txtName.setText(poi.nickname);
		txtName.setTextColor(Color.BLACK);
		
		layout.addView(imageView);
		layout.addView(txtName);
		return layout;
	}

	private void setMapCenter(final PoiInfo poi) {
		if(poi == null)return;
		LatLng latlng = new LatLng(poi.location.get(1), 
				poi.location.get(0));
		Log.i(TAG, "showInfoWindow " + poi.nickname + "at["
				+ poi.location.get(1) + " , " + poi.location.get(0));
		
		LinearLayout layout = createUserView(poi);
		
		TextView txtTime = new TextView(getApplicationContext());
		txtTime.setText(poi.modify_time.replace(' ', '\n'));
		txtTime.setTextColor(Color.BLACK);
		txtTime.setTextSize(12);
		layout.addView(txtTime);

		MapStatus status = mBaiduMap.getMapStatus();
		if(status != null && status.zoom < 16.0f){
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latlng, 16.0f);
			mBaiduMap.setMapStatus(msu);
		}else{
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
			mBaiduMap.setMapStatus(msu);
		}

		mBaiduMap.showInfoWindow(new InfoWindow(layout, latlng, -47));
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
		mLocClient.stop();
		mMapView.onPause();
		mLocClient.unRegisterLocationListener(myListener);
		super.onPause();
	}

	@Override
	protected void onResume() {
		mLocClient.stop();
		mLocClient.registerLocationListener(myListener);
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mLocClient.unRegisterLocationListener(myListener);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

}
