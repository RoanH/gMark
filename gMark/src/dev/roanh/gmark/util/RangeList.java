package dev.roanh.gmark.util;

import java.util.function.Supplier;

public class RangeList<T>{
	
	public RangeList(int size, Supplier<T> init){
		
	}
	
	public T get(IDable index){
		return get(index.getID());
	}

	public T get(int index){
		return null;//TODO
	}
	
	public void set(int index, T value){
		//TODO
	}
	
	public void set(IDable index, T value){
		set(index.getID(), value);
	}
}
