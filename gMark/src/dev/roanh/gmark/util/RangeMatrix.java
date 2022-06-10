/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.util;

import java.util.function.Supplier;

/**
 * A matrix implementation backed by a
 * fixed size 2 dimensional array.
 * @author Roan
 * @param <T> The dta type stored in the matrix.
 */
public class RangeMatrix<T>{
	/**
	 * The data stored in this matrix.
	 */
	private T[][] data;
	/**
	 * The number of rows in this matrix.
	 */
	private int rows;
	/**
	 * The number of columns in this matrix.
	 */
	private int cols;
	
	/**
	 * Constructs a new square range matrix with the
	 * given size.
	 * @param size The size of the matrix.
	 */
	public RangeMatrix(int size){
		this(size, size, null);
	}
	
	/**
	 * Constructs a new square range matrix with the
	 * given dimensions.
	 * @param rows The number of rows in the matrix.
	 * @param cols The number of columns in the matrix.
	 */
	public RangeMatrix(int rows, int cols){
		this(rows, cols, null);
	}
	
	/**
	 * Constructs a new square range matrix with the
	 * given size and initialising every matrix element
	 * with the given supplier.
	 * @param size The size of the matrix.
	 * @param init The supplier to initialise all
	 *        matrix elements, possibly <code>null</code>.
	 */
	public RangeMatrix(int size, Supplier<T> init){
		this(size, size, init);
	}
	
	/**
	 * Constructs a new range matrix with the given
	 * dimensions and initialising every matrix element
	 * with the given supplier.
	 * @param rows The number of rows in the matrix.
	 * @param cols The number of columns in the matrix.
	 * @param init The supplier to initialise all
	 *        matrix elements, possibly <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public RangeMatrix(int rows, int cols, Supplier<T> init){
		this.rows = rows;
		this.cols = cols;
		data = (T[][])new Object[rows][cols];
		if(init != null){
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					data[i][j] = init.get();
				}
			}
		}
	}
	
	/**
	 * Gets the element stored in the given matrix cell.
	 * @param row The row of the cell to get.
	 * @param col The column of the cell to get.
	 * @return The element stored in the requested matrix cell.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 * @see IDable
	 */
	public T get(IDable row, IDable col){
		return get(row.getID(), col.getID());
	}
	
	/**
	 * Gets the element stored in the given matrix cell.
	 * @param row The row of the cell to get.
	 * @param col The column of the cell to get.
	 * @return The element stored in the requested matrix cell.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 * @see IDable
	 */
	public T get(int row, IDable col){
		return get(row, col.getID());
	}
	
	/**
	 * Gets the element stored in the given matrix cell.
	 * @param row The row of the cell to get.
	 * @param col The column of the cell to get.
	 * @return The element stored in the requested matrix cell.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 * @see IDable
	 */
	public T get(IDable row, int col){
		return get(row.getID(), col);
	}
	
	/**
	 * Gets the element stored in the given matrix cell.
	 * @param row The row of the cell to get.
	 * @param col The column of the cell to get.
	 * @return The element stored in the requested matrix cell.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public T get(int row, int col){
		return data[row][col];
	}

	/**
	 * Sets the element at the given matrix cell to the given value.
	 * @param row The row of the cell to set.
	 * @param col The column of the cell to set.
	 * @param value The new data to store.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public void set(IDable row, IDable col, T value){
		set(row.getID(), col.getID(), value);
	}

	/**
	 * Sets the element at the given matrix cell to the given value.
	 * @param row The row of the cell to set.
	 * @param col The column of the cell to set.
	 * @param value The new data to store.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public void set(int row, IDable col, T value){
		set(row, col.getID(), value);
	}
	
	/**
	 * Sets the element at the given matrix cell to the given value.
	 * @param row The row of the cell to set.
	 * @param col The column of the cell to set.
	 * @param value The new data to store.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public void set(IDable row, int col, T value){
		set(row.getID(), col, value);
	}
	
	/**
	 * Sets the element at the given matrix cell to the given value.
	 * @param row The row of the cell to set.
	 * @param col The column of the cell to set.
	 * @param value The new data to store.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public void set(int row, int col, T value){
		data[row][col] = value;
	}
	
	/**
	 * Gets the number of rows in this matrix.
	 * @return The number of rows in this matrix.
	 */
	public int getRowCount(){
		return rows;
	}
	
	/**
	 * Gets the number of columns in this matrix.
	 * @return The number of columns in this matrix.
	 */
	public int getColumnCount(){
		return cols;
	}
}
