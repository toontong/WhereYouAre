package io.toontong.where.poi;

public interface Callbacker<T> {
	public void onSuccess(T result);
	public void onFail(Exception e);
}
