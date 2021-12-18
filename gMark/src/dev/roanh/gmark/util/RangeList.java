package dev.roanh.gmark.util;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A list implementation backed by a fixed size array.
 * @author Roan
 * @param <T> The data type stored in this list.
 */
public class RangeList<T> implements Iterable<T>{
	/**
	 * The data stored in this list.
	 */
	private T[] data;
	
	/**
	 * Constructs a new range list with the given size.
	 * @param size The size of the range list.
	 */
	public RangeList(int size){
		this(size, null);
	}
	
	/**
	 * Constructs a new range list with the given size
	 * and initialising every list element with the
	 * given supplier.
	 * @param size The size of the range list.
	 * @param init The supplier to initialise all
	 *        list elements, possibly <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public RangeList(int size, Supplier<T> init){
		data = (T[])new Object[size];
		if(init != null){
			for(int i = 0; i < data.length; i++){
				data[i] = init.get();
			}
		}
	}
	
	/**
	 * Gets the element at the given index.
	 * @param index The index to get.
	 * @return The element at the given index.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 * @see IDable
	 */
	public T get(IDable index){
		return get(index.getID());
	}

	/**
	 * Gets the element at the given index.
	 * @param index The index to get.
	 * @return The element at the given index.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public T get(int index){
		return data[index];
	}
	
	/**
	 * Sets the element at the given index to the given value.
	 * @param index The index to set.
	 * @param value The new data to store.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 */
	public void set(int index, T value){
		data[index] = value;
	}
	
	/**
	 * Sets the element at the given index to the given value.
	 * @param index The index to set.
	 * @param value The new data to store.
	 * @throws ArrayIndexOutOfBoundsException When the given index
	 *         is out of bounds.
	 * @see IDable
	 */
	public void set(IDable index, T value){
		set(index.getID(), value);
	}
	
	/**
	 * Gets the size of this list.
	 * @return The size of this list.
	 */
	public int size(){
		return data.length;
	}

	@Override
	public Iterator<T> iterator(){
		return new Iterator<T>(){
			/**
			 * The index of the next element to return.
			 */
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
