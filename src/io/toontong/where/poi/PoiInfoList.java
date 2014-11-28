package io.toontong.where.poi;

import java.util.List;

public class PoiInfoList{
	public static class PoiInfo{
		public long id;
		public String title;
		public int geotable_id;

		//double[] location; //latitude and longitude
		public List<Double> location; //latitude and longitude
		public String create_time;
		public String modify_time;
		public String tags;
		public String city;
		public int city_id;
		public String province;
		public String district;

		// 用户字段
		public long userid;
		public String role;
		public String role_acl;
		public String nickname;
		public String platform;
	}
	public int status;
	public int size;
	public int total;
	public String message;   //响应的信息,对status的中文描述
	public List<PoiInfo> pois;
}
