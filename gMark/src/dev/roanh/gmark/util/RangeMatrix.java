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
