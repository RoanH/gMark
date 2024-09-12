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

/**
 * Class that can be used as a wrapper around some
 * other object. DataProxy instances are never equal
 * to other DataProxy instances unless they are exactly
 * the same object. This class is intended for use with
 * the {@link UniqueGraph} class so that multiple nodes can
 * be added with the same data. 
 * @author Roan
 * @param <T> The data type to proxy.
 */
public class DataProxy<T>{
	/**
	 * The stored proxy data.
	 */
	private T data;
	
	/**
	 * Constructs a new DataProxy instances
	 * with the given data to proxy.
	 * @param data The data to store.
	 */
	public DataProxy(T data){
		this.data = data;
	}
	
	/**
	 * Gets the data stored at this data.
	 * @return The stored data.
	 */
	public T getData(){
		return data;
	}
	
	@Override
	public String toString(){
		return data.toString();
	}
}
