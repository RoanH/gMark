package dev.roanh.gmark.util;

public class DataProxy<T>{
	private T data;
	
	public DataProxy(T data){
		this.data = data;
	}
	
	public T getData(){
		return data;
	}
}
