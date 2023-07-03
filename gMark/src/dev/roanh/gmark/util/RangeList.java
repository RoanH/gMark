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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
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
		return data[index.getID()];
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
	
	/**
	 * Similar to {@link #forEach(Consumer)} this method
	 * will iterate over all elements in this list and
	 * pass them to the given consumer. However, null
	 * elements will be skipped instead of forwarded.
	 * @param fun The consumer to pass elements to.
	 */
	public void forEachNonNull(Consumer<T> fun){
		for(T val : data){
			if(val != null){
				fun.accept(val);
			}
		}
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
				if(hasNext()){
					return data[index++];
				}else{
					throw new NoSuchElementException();
				}
			}
		};
	}
}
