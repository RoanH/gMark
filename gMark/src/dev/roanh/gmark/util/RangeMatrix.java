package dev.roanh.gmark.util;

import java.util.function.Supplier;

public class RangeMatrix<T>{
	private T[][] data;
	private int rows;
	private int cols;
	
	//square with element init
	public RangeMatrix(int size, Supplier<T> init){
		this(size, size, init);
	}
	
	@SuppressWarnings("unchecked")
	public RangeMatrix(int rows, int cols, Supplier<T> init){
		this.rows = rows;
		this.cols = cols;
		data = (T[][])new Object[rows][cols];
		for(int i = 0; i < rows; i++){
			for(int j = 0; j < cols; j++){
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
	
	public int getRowCount(){
		return rows;
	}
	
	public int getColumnCount(){
		return cols;
	}
}
