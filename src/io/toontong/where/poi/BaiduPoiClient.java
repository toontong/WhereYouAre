package io.toontong.where.poi;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.RestAdapter;

/*
 * 调用 http://developer.baidu.com/map/index.php?title=lbscloud/api/geodata
 * 创建POI数据
 * 每个用户只有一条POI数据,表示最后位置.
 * use libs:http://square.github.io/retrofit/
 */
public class BaiduPoiClient {
	private static final String API_HOST = "http://api.map.baidu.com";
	private static final String ACCESS_KEY = "pdpWGMFvwh0Y6ojGudSi7d14"; //应用类型为服务端的access-key,已创建好,固定不变
	private static final String mGeoTableID = "85636"; //where_user表,已创建好,固定不变 
	
	private static BaiduPoi mInstance;

	public static final String ROLE_ACL_CREATOR = "creator";
	public static final String ROLE_ACL_MEMBER = "member";

	private interface BaiduPoi{
		@Multipart
		@POST("/geodata/v3/poi/create")
		void createPoi(
			@Part("ak") String ak,                   // 用户的访问权限key	string(50)	必选。
			@Part("title") String title,             // poi名称  string(256)	可选 。
			//@Part("address") String address,         // 地址     string(256)	可选 。
			@Part("latitude") String latitude,       // 用户上传的纬度	double	必选 。
			@Part("longitude") String longitude,     // 用户上传的经度	double	必选 。
			@Part("coord_type") String coord_type,   // 用户上传的坐标的类型 uint32；1：GPS经纬度坐标	；2：国测局加密经纬度坐标;3：百度加密经纬度坐标 4：百度加密墨卡托坐标 必选
			@Part("geotable_id") String geotable_id, // 记录关联的geotable的标识	string(50)	必选，加密后的id 。
			//下面是用户自定义列
			@Part("userid") String userid,           // 百度Social ID:FrontiaUser.getId()值 string 必选 。
			@Part("role") String role,               // 百度Role组名,string,同一组的人能看到位置
			@Part("role_acl") String role_acl,       // 百度Role组中的acl, string 枚举:creator,member;
			@Part("nickname") String nickname,       // 用户昵称
			@Part("platform") String platform,       //c

			Callback<CreatePoiResult> callback
		);
		
		@Headers("Cache-Control: no-cache")
		@GET("/geodata/v3/poi/list")
		void getPoiByUserId(
			@Query("ak") String ak,                   // 用户的访问权限key	string(50)	必选。
			@Query("geotable_id") String geotable_id, // 记录关联的geotable的标识	string(50)	必选，加密后的id 。
//			@Query("page_index") String page_index,   // 分页索引	uint32	默认为0，最大为9
//			@Query("page_size") String page_size,     // 分页数目	uint32	默认为10，上限为200
			//下面是用户自定义列
			@Query("userid") String userid,           // 百度Social ID:FrontiaUser.getId()值 string 必选 。
			Callback<PoiListResult> callback
		);

		@Headers("Cache-Control: no-cache")
		@GET("/geodata/v3/poi/list")
		void getPoiByRole(
			@Query("ak") String ak,                   // 用户的访问权限key	string(50)	必选。
			@Query("geotable_id") String geotable_id, // 记录关联的geotable的标识	string(50)	必选，加密后的id 。
//			@Query("page_index") String page_index,   // 分页索引	uint32	默认为0，最大为9
//			@Query("page_size") String page_size,     // 分页数目	uint32	默认为10，上限为200
			//下面是用户自定义列
			@Query("role") String role,               // 百度Role组名,string,同一组的人能看到位置
			Callback<PoiListResult> callback
		);
	}
	
	private static BaiduPoi getInstance() {
		if (null == mInstance){
			RestAdapter.Builder builder = new RestAdapter.Builder();
			builder = builder.setEndpoint(API_HOST);
			RestAdapter restAdapter;
			restAdapter = builder.build();
			mInstance = restAdapter.create(BaiduPoi.class);
		}
		return mInstance;
	}

	/**
	 * @param latitude 用户上传的纬度	double	必选 。
	 * @param longitude 用户上传的经度	double	必选 。
	 * @param coord_type 用户上传的坐标的类型 uint32；1：GPS经纬度坐标	；2：国测局加密经纬度坐标;3：百度加密经纬度坐标 4：百度加密墨卡托坐标 必选
	 * @param userid (用户自定义列)百度Social ID:FrontiaUser.getId()值 string 必选 。
	 * @param role   (用户自定义列)百度Role组名,string,同一组的人能看到位置
	 * @param role_acl (用户自定义列)百度Role组中的acl, string 枚举:creator,member;
	 * @param nickname (用户自定义列)用户昵称
	 * @param platform (用户自定义列)第三方登录平台名字,FrontiaUser.getPlatform()值
	 * @param callback
	 */
	public static void createPoi(
			double latitude,
			double longitude,
			int coord_type,
			String userid,         // 百度Social ID:FrontiaUser.getId()值 string 必选 。
			String role,           // 百度Role组名,string,同一组的人能看到位置
			String role_acl,       // 百度Role组中的acl, string 枚举:create,member;
			String nickname,       // 用户昵称
			String platform,       // 第三方登录平台名字,FrontiaUser.getPlatform()值
			final Callbacker<CreatePoiResult> callback){

		Callback<CreatePoiResult> cb = new Callback<CreatePoiResult>(){
			@Override
			public void success(CreatePoiResult result, Response response) {
				callback.onSuccess(result);
			}
			@Override
			public void failure(RetrofitError retrofitError) {
				callback.onFail(retrofitError);
			}
		};
		getInstance().createPoi(
			ACCESS_KEY,
			nickname + " 的最新位置",
			Double.toString(latitude),
			Double.toString(longitude),
			String.valueOf(coord_type),
			mGeoTableID,
			userid, role, role_acl,nickname, platform,
			cb);
	}

	public static void getPoiByUserId(
			String userid,  // 百度Social ID:FrontiaUser.getId()值 string 必选 。
			final Callbacker<PoiListResult> callback){
		
		Callback<PoiListResult> cb = new Callback<PoiListResult>(){
			@Override
			public void success(PoiListResult result, Response response) {
				callback.onSuccess(result);
			}
			@Override
			public void failure(RetrofitError retrofitError) {
				callback.onFail(retrofitError);
			}
		};
		getInstance().getPoiByUserId(
			ACCESS_KEY,
			mGeoTableID,
			String.format("%s,%s", userid, userid),
			cb);
		
	}
	
	public static  void getPoiByUserRole(
		@Query("role") String role,               // 百度Role组名,string,同一组的人能看到位置
		final Callbacker<PoiListResult> callback){
		Callback<PoiListResult> cb = new Callback<PoiListResult>(){
			@Override
			public void success(PoiListResult result, Response response) {
				callback.onSuccess(result);
			}
			@Override
			public void failure(RetrofitError retrofitError) {
				callback.onFail(retrofitError);
			}
		};
		getInstance().getPoiByUserId(
				ACCESS_KEY,
				mGeoTableID,
				role,
				cb);
	}
}
