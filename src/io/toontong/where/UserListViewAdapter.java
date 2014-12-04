package io.toontong.where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.toontong.where.poi.PoiInfoList;
import io.toontong.where.poi.PoiInfoList.PoiInfo;

public class UserListViewAdapter extends BaseAdapter {
	private static final String TAG = "where.adapter";
	
	private List<Map<String, Object>> mListItems = new ArrayList<Map<String, Object>>(); // 数据集合
	public static final String Map_Key_Image = "image";
	public static final String Map_Key_Nickname = "nickname";
	public static final String Map_Key_Info = "info";
	public static final String Map_Key_Userid = "userid";
	public static final String Map_Key_PoiId = "poiid";
	
	private LayoutInflater mListContainer; // 视图容器

	public final class ListItemView { // 自定义控件集合
		public ImageView imgHead;
		public TextView title;
		public TextView info;
	}

	public UserListViewAdapter(Context context,
			PoiInfoList poiInfos){

		mListContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文

		setUserPoiInfos(poiInfos);
	}

	private List<Map<String, Object>> setUserPoiInfos(PoiInfoList poiInfos) {
		for (int i = 0; i < poiInfos.size; i++) {
			Map<String, Object> item = new HashMap<String, Object>();

			PoiInfo poi = poiInfos.pois.get(i);
			if (null == poi){
				Log.e(TAG, "get(" + i + ") poi is null.");
				continue;
			}

			item.put(Map_Key_Image, R.drawable.head_00 + (int)(poi.userid % 10));
			item.put(Map_Key_Nickname, poi.nickname);
			item.put(Map_Key_Info,  poi.modify_time + " at " +  poi.district);
			item.put(Map_Key_Userid, poi.userid);
			item.put(Map_Key_PoiId, poi.id);

			mListItems.add(item);
		}
		return mListItems;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// 自定义视图
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取list_item布局文件的视图
			convertView = mListContainer.inflate(R.layout.userlistview, null);
			// 获取控件对象
			listItemView.imgHead = (ImageView) convertView
					.findViewById(R.id.imageHead);
			listItemView.title = (TextView) convertView
					.findViewById(R.id.nickname);
			listItemView.info = (TextView) convertView.findViewById(R.id.info);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置文字和图片
		Map<String, Object> item = mListItems.get(position);

		RoundCornerImage.setRoundCornerImage(listItemView.imgHead,
				(Integer) item.get(Map_Key_Image), 16.0f);
		listItemView.title.setText((String) item.get(Map_Key_Nickname));
		listItemView.info.setText((String) item.get(Map_Key_Info));

		return convertView;
	}

	
	@Override
	public int getCount() {
		return mListItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
