package io.toontong.where.poi;

public class UpdatePoiResult {
	public int status;       //状态码,0代表成功，其它取值含义另行说
	public long id;        //修改后的POI数据的id
	public String message;   //响应的信息,对status的中/英文描述
}
