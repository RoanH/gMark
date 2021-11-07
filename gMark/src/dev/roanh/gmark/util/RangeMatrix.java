package dev.roanh.gmark.util;

import java.util.function.Supplier;

public class RangeMatrix<T>{
	private T[][] data;
	private int size;
	
	//square with element init
	@SuppressWarnings("unchecked")
	public RangeMatrix(int size, Supplier<T> init){
		data = (T[][])new Object[size];
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				data[i][j] = init.get();
			}
		}
	}
	
	public T get(IDable row, IDable col){
		return get(row.getID(), col.getID());
	}
	
	public T get(int row, int col){
		return data[row][col];
	}
	
	public void set(int row, int col, T value){
		data[row][col] = value;
	}
	
	public int getSize(){
		return size;
	}
}
