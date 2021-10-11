package dev.roanh.gmark.util;

import java.util.Iterator;
import java.util.function.Supplier;

public class RangeList<T> implements Iterable<T>{
	private T[] data;
	
	public RangeList(int size){
		this(size, null);
	}
	
	@SuppressWarnings("unchecked")
	public RangeList(int size, Supplier<T> init){
		data = (T[])new Object[size];
		if(init != null){
			for(int i = 0; i < data.length; i++){
				data[i] = init.get();
			}
		}
	}
	
	public T get(IDable index){
		return get(index.getID());
	}

	public T get(int index){
		return data[index];
	}
	
	public void set(int index, T value){
		data[index] = value;
	}
	
	public void set(IDable index, T value){
		set(index.getID(), value);
	}
	
	public int size(){
		return data.length;
	}

	@Override
	public Iterator<T> iterator(){
		return new Iterator<T>(){
			private int index = 0;

			@Override
			public boolean hasNext(){
				return index < data.length;
			}

			@Override
			public T next(){
				return data[index++];
			}
		};
	}
}
